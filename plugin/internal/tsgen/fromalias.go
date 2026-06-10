package tsgen

import (
	"fmt"
	"strings"

	"github.com/jinganix/webpb/plugin/internal/core"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// FromAliasGenerator generates fromAlias registration files.
type FromAliasGenerator struct{}

// Generate generates FromAlias files.
func (g *FromAliasGenerator) Generate(ctx *GeneratorContext) (map[string]string, error) {
	engine, err := sharedTSEngine()
	if err != nil {
		return nil, err
	}
	fileMap := map[string]string{}
	for baseType, descriptors := range ctx.SubTypes {
		content, err := g.genContent(engine, ctx, baseType, descriptors)
		if err != nil {
			return nil, err
		}
		fileMap[baseType+"FromAlias.ts"] = content
	}
	return fileMap, nil
}

func (g *FromAliasGenerator) genContent(engine interface {
	Process(string, map[string]any) (string, error)
}, ctx *GeneratorContext, baseType string, descriptors []protoreflect.MessageDescriptor) (string, error) {
	imports := NewEmptyImports()
	baseDescriptor := ctx.BaseTypes[baseType]
	if baseDescriptor == nil {
		return "", fmt.Errorf("Base type not found: %s", baseType)
	}
	baseTypeRef := imports.ImportType(string(baseDescriptor.FullName()))
	var assignmentLines []string
	for _, descriptor := range descriptors {
		name := imports.ImportType(string(descriptor.FullName()))
		for _, subValue := range g.getSubValues(imports, descriptor) {
			assignmentLines = append(assignmentLines, fmt.Sprintf(
				"%s.fromAliases[%s] = %s.fromAlias;", baseTypeRef, subValue, name))
		}
	}
	importLines := imports.ToList()
	data := map[string]any{
		"imports":     strings.Join(importLines, "\n"),
		"hasImports":  len(importLines) > 0,
		"assignments": strings.Join(assignmentLines, "\n"),
	}
	content, err := engine.Process("from.alias", data)
	if err != nil {
		return "", err
	}
	return normalizeTsTemplateOutput(content), nil
}

func (g *FromAliasGenerator) getSubValues(imports *Imports, descriptor protoreflect.MessageDescriptor) []string {
	var values []string
	for _, subValue := range core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt().GetSubValues() {
		if valueDescriptor := core.ResolveEnumValue([]protoreflect.FileDescriptor{descriptor.ParentFile()}, subValue); valueDescriptor != nil {
			enum := valueDescriptor.Parent().(protoreflect.EnumDescriptor)
			fullName := string(enum.FullName()) + "." + string(valueDescriptor.Name())
			values = append(values, imports.ImportType(fullName))
			continue
		}
		values = append(values, `"`+subValue+`"`)
	}
	return values
}
