package javagen

import (
	"fmt"

	"github.com/jinganix/webpb/plugin/internal/core"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// EnumGenerator generates Java enum classes.
type EnumGenerator struct {
	fileDescriptor protoreflect.FileDescriptor
	imports        *Imports
	webpbOpts      interface {
		GetAnnotation() []string
	}
	fileOpts interface {
		GetAnnotation() []string
	}
}

// NewEnumGenerator creates an enum generator.
func NewEnumGenerator(fd protoreflect.FileDescriptor) (*EnumGenerator, error) {
	lookup, err := GetLookup(fd)
	if err != nil {
		return nil, err
	}
	return &EnumGenerator{
		fileDescriptor: fd,
		imports:        NewImports(GetJavaPackage(fd), lookup, fd),
		webpbOpts:      core.GetWebpbFileOpts(fd, core.HasFileJava).GetJava(),
		fileOpts:       core.GetFileOpts(fd, core.HasFileJava).GetJava(),
	}, nil
}

// Generate generates Java source for an enum.
func (g *EnumGenerator) Generate(enumDescriptor protoreflect.EnumDescriptor, tmpl string) (string, error) {
	engine, err := sharedJavaEngine()
	if err != nil {
		return "", err
	}
	annos, err := g.getAnnotations(enumDescriptor)
	if err != nil {
		return "", err
	}
	implements, err := g.getImplements(enumDescriptor)
	if err != nil {
		return "", err
	}
	data := map[string]any{
		"filename":   enumDescriptor.ParentFile().Path(),
		"package":    GetJavaPackage(g.fileDescriptor),
		"msgAnnos":   annos,
		"className":  string(enumDescriptor.Name()),
		"implements": implements,
		"enums":      g.getEnums(enumDescriptor),
		"valueType":  g.getValueType(enumDescriptor),
		"imports":    g.imports.ToList(),
	}
	content, err := engine.Process(tmpl, data)
	if err != nil {
		return "", err
	}
	return normalizeJavaTemplateOutput(content), nil
}

func (g *EnumGenerator) getValueType(enumDescriptor protoreflect.EnumDescriptor) string {
	if core.IsStringValue(enumDescriptor) {
		return "String"
	}
	return "Integer"
}

func (g *EnumGenerator) getAnnotations(enumDescriptor protoreflect.EnumDescriptor) ([]string, error) {
	var annos []string
	for _, src := range [][]string{
		g.webpbOpts.GetAnnotation(),
		g.fileOpts.GetAnnotation(),
		core.GetEnumOpts(enumDescriptor, core.HasEnumJava).GetJava().GetAnnotation(),
	} {
		for _, anno := range src {
			imported, err := g.imports.ImportAnnotation(anno)
			if err != nil {
				return nil, err
			}
			annos = append(annos, imported)
		}
	}
	return annos, nil
}

func (g *EnumGenerator) getImplements(enumDescriptor protoreflect.EnumDescriptor) ([]string, error) {
	enumOpts := core.GetEnumOpts(enumDescriptor, core.HasEnumJava).GetJava()
	valueType := g.getValueType(enumDescriptor)
	enumeration, err := g.imports.ImportClassOrInterface("Enumeration<" + valueType + ">")
	if err != nil {
		return nil, err
	}
	values := []string{enumeration}
	for _, impl := range enumOpts.GetImplements() {
		imported, err := g.imports.ImportClassOrInterface(impl)
		if err != nil {
			return nil, err
		}
		values = append(values, imported)
	}
	return values, nil
}

func (g *EnumGenerator) getEnums(enumDescriptor protoreflect.EnumDescriptor) []map[string]string {
	var enums []map[string]string
	values := enumDescriptor.Values()
	for i := 0; i < values.Len(); i++ {
		descriptor := values.Get(i)
		enums = append(enums, map[string]string{
			"name":  string(descriptor.Name()),
			"value": g.getEnumValue(enumDescriptor, descriptor),
		})
	}
	return enums
}

func (g *EnumGenerator) getEnumValue(enumDescriptor protoreflect.EnumDescriptor, descriptor protoreflect.EnumValueDescriptor) string {
	opts := core.GetEnumValueOpts(descriptor, core.HasEnumValueOpt).GetOpt()
	if opts.GetValue() == "" {
		if core.IsStringValue(enumDescriptor) {
			return `"` + string(descriptor.Name()) + `"`
		}
		return fmt.Sprintf("%d", int32(descriptor.Number()))
	}
	return `"` + opts.GetValue() + `"`
}
