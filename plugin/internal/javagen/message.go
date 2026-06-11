package javagen

import (
	"fmt"
	"regexp"
	"strings"

	"github.com/jinganix/webpb/plugin/internal/core"
	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"google.golang.org/protobuf/reflect/protoreflect"
)

var javaPrimitiveTypes = map[protoreflect.Kind]string{
	protoreflect.BoolKind:     "Boolean",
	protoreflect.BytesKind:    "byte[]",
	protoreflect.DoubleKind:   "Double",
	protoreflect.Fixed32Kind:  "Integer",
	protoreflect.Fixed64Kind:  "Long",
	protoreflect.FloatKind:    "Float",
	protoreflect.Int32Kind:    "Integer",
	protoreflect.Int64Kind:    "Long",
	protoreflect.Sfixed32Kind: "Integer",
	protoreflect.Sfixed64Kind: "Long",
	protoreflect.Sint32Kind:   "Integer",
	protoreflect.Sint64Kind:   "Long",
	protoreflect.StringKind:   "String",
	protoreflect.Uint32Kind:   "Integer",
	protoreflect.Uint64Kind:   "Long",
}

// MessageGenerator generates Java message classes.
type MessageGenerator struct {
	fileDescriptor protoreflect.FileDescriptor
	imports        *Imports
	webpbOpts      *webpb.JavaFileOpts
	fileOpts       *webpb.JavaFileOpts
}

// NewMessageGenerator creates a message generator.
func NewMessageGenerator(fd protoreflect.FileDescriptor) (*MessageGenerator, error) {
	lookup, err := GetLookup(fd)
	if err != nil {
		return nil, err
	}
	return &MessageGenerator{
		fileDescriptor: fd,
		imports:        NewImports(GetJavaPackage(fd), lookup, fd),
		webpbOpts:      core.GetWebpbFileOpts(fd, core.HasFileJava).GetJava(),
		fileOpts:       core.GetFileOpts(fd, core.HasFileJava).GetJava(),
	}, nil
}

// Generate generates Java source for a message.
func (g *MessageGenerator) Generate(descriptor protoreflect.MessageDescriptor) (string, error) {
	engine, err := sharedJavaEngine()
	if err != nil {
		return "", err
	}
	return g.generate(func() (map[string]any, error) {
		if err := core.CheckDuplicatedFields(descriptor); err != nil {
			return nil, err
		}
		data, err := g.getMessageData(descriptor, 0)
		if err != nil {
			return nil, err
		}
		data["filename"] = descriptor.ParentFile().Path()
		data["package"] = GetJavaPackage(g.fileDescriptor)
		data["imports"] = g.imports.ToList()
		return data, nil
	}, "message", 0, engine)
}

func (g *MessageGenerator) generate(supplier func() (map[string]any, error), tmpl string, level int, engine interface {
	Process(string, map[string]any) (string, error)
}) (string, error) {
	data, err := supplier()
	if err != nil {
		return "", err
	}
	content, err := engine.Process(tmpl, data)
	if err != nil {
		return "", err
	}
	content = normalizeJavaTemplateOutput(content)
	re := regexp.MustCompile(`(?m)^(.+)$`)
	indent := strings.Repeat(" ", level*2)
	return re.ReplaceAllString(content, indent+"$1"), nil
}

func normalizeJavaTemplateOutput(content string) string {
	// One blank line after imports before type declaration or annotations.
	content = regexp.MustCompile(`(?m)((?:^import .+;\n)+)\n{3,}((?:@|public ))`).ReplaceAllString(content, "$1\n\n$2")
	// One blank line before annotations and nested type declarations.
	content = regexp.MustCompile(`\n{3,}(  @)`).ReplaceAllString(content, "\n\n$1")
	// One blank line before top-level type declarations and members.
	content = regexp.MustCompile(`\n{3,}(public (?:class|enum))`).ReplaceAllString(content, "\n\n$1")
	content = regexp.MustCompile(`\n{3,}(  (?:public|private) )`).ReplaceAllString(content, "\n\n$1")
	for strings.Contains(content, "\n\n  }\n") {
		content = strings.ReplaceAll(content, "\n\n  }\n", "\n  }\n")
	}
	for strings.Contains(content, "\n\n}\n") {
		content = strings.ReplaceAll(content, "\n\n}\n", "\n}\n")
	}
	return content
}

func (g *MessageGenerator) getMessageData(descriptor protoreflect.MessageDescriptor, level int) (map[string]any, error) {
	opts := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
	annos, err := g.getMessageAnnotations(descriptor)
	if err != nil {
		return nil, err
	}
	extend, err := g.getExtend(descriptor)
	if err != nil {
		return nil, err
	}
	implements, err := g.getImplements(descriptor)
	if err != nil {
		return nil, err
	}
	webpbMeta, err := g.imports.ImportClassOrInterface("WebpbMeta")
	if err != nil {
		return nil, err
	}
	fields, err := g.getFields(descriptor)
	if err != nil {
		return nil, err
	}
	nestedMsgs, err := g.getNestedMessages(descriptor, level+1)
	if err != nil {
		return nil, err
	}
	return map[string]any{
		"msgAnnos":   annos,
		"className":  string(descriptor.Name()),
		"extend":     extend,
		"implements": implements,
		"method":     opts.GetMethod(),
		"context":    core.Normalize(opts.GetContext()),
		"path":       core.Normalize(opts.GetPath()),
		"webpbMeta":  webpbMeta,
		"fields":     fields,
		"nestedMsgs": nestedMsgs,
		"genSetter":  javaGenSetterOrDefault(g.webpbOpts),
		"genGetter":  javaGenGetterOrDefault(g.webpbOpts),
	}, nil
}

func (g *MessageGenerator) getMessageAnnotations(descriptor protoreflect.MessageDescriptor) ([]string, error) {
	filter := NewAnnotationDistinctFilter(g.imports, g.webpbOpts.GetRepeatableAnnotation())
	var annos []string
	for _, src := range [][]string{
		core.GetMessageOpts(descriptor, core.HasMessageJava).GetJava().GetAnnotation(),
		g.fileOpts.GetAnnotation(),
		g.webpbOpts.GetAnnotation(),
		g.getSubValueAnnotations(descriptor),
	} {
		for _, anno := range src {
			imported, err := g.imports.ImportAnnotation(anno)
			if err != nil {
				return nil, err
			}
			if filter.Allow(imported) {
				annos = append(annos, imported)
			}
		}
	}
	return annos, nil
}

func (g *MessageGenerator) getSubValueAnnotations(descriptor protoreflect.MessageDescriptor) []string {
	var result []string
	for _, subValue := range core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt().GetSubValues() {
		if !strings.Contains(subValue, ".") {
			result = append(result, `@WebpbSubValue("`+subValue+`")`)
			continue
		}
		valueDescriptor := core.ResolveEnumValue([]protoreflect.FileDescriptor{descriptor.ParentFile()}, subValue)
		if valueDescriptor == nil {
			result = append(result, `@WebpbSubValue("`+subValue+`")`)
			continue
		}
		result = append(result,
			fmt.Sprintf(`@WebpbSubValue("%d")`, int32(valueDescriptor.Number())),
			fmt.Sprintf(`@WebpbSubValue("%s")`, valueDescriptor.Name()),
		)
	}
	return result
}

func (g *MessageGenerator) getExtend(descriptor protoreflect.MessageDescriptor) (any, error) {
	opts := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
	if opts.GetExtends() == "" {
		return nil, nil
	}
	extends := opts.GetExtends()
	for _, d := range core.ResolveTopLevelTypes(descriptor.ParentFile()) {
		for _, nested := range core.ResolveNestedTypes(d) {
			if string(nested.FullName()) == extends {
				return g.imports.ImportGenericDescriptor(nested)
			}
		}
	}
	return g.imports.ImportClassOrInterface(extends)
}
func (g *MessageGenerator) getImplements(descriptor protoreflect.MessageDescriptor) ([]string, error) {
	opts := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
	webpbMessage, err := g.imports.ImportClassOrInterface("WebpbMessage")
	if err != nil {
		return nil, err
	}
	values := []string{webpbMessage}
	for _, impl := range opts.GetImplements() {
		imported, err := g.imports.ImportClassOrInterface(impl)
		if err != nil {
			return nil, err
		}
		values = append(values, imported)
	}
	return values, nil
}

func (g *MessageGenerator) getFields(descriptor protoreflect.MessageDescriptor) ([]map[string]any, error) {
	autoAliases := core.GetAutoAliases(descriptor)
	fields := g.getMemberFields(descriptor)
	out := make([]map[string]any, 0, len(fields))
	for _, field := range fields {
		fieldType, err := g.getFieldType(field)
		if err != nil {
			return nil, err
		}
		annos, err := g.getFieldAnnotations(descriptor, field, autoAliases[string(field.Name())])
		if err != nil {
			return nil, err
		}
		out = append(out, map[string]any{
			"type":  fieldType,
			"name":  string(field.Name()),
			"annos": annos,
		})
	}
	return out, nil
}

func (g *MessageGenerator) getMemberFields(descriptor protoreflect.MessageDescriptor) []protoreflect.FieldDescriptor {
	var fields []protoreflect.FieldDescriptor
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		if core.GetFieldOpts(field, core.HasFieldOpt).GetOpt().GetOmitted() {
			continue
		}
		fields = append(fields, field)
	}
	return fields
}

func (g *MessageGenerator) getFieldType(field protoreflect.FieldDescriptor) (string, error) {
	if field.IsMap() {
		key := core.GetMapKeyDescriptor(field)
		value := core.GetMapValueDescriptor(field)
		keyType, err := g.toType(key)
		if err != nil {
			return "", err
		}
		valueType, err := g.toType(value)
		if err != nil {
			return "", err
		}
		return g.imports.ImportClassOrInterface("Map<" + keyType + ", " + valueType + ">")
	}
	if field.Cardinality() == protoreflect.Repeated {
		elemType, err := g.toType(field)
		if err != nil {
			return "", err
		}
		return g.imports.ImportClassOrInterface("List<" + elemType + ">")
	}
	return g.toType(field)
}

func (g *MessageGenerator) getFieldAnnotations(descriptor protoreflect.MessageDescriptor, field protoreflect.FieldDescriptor, alias string) ([]string, error) {
	filter := NewAnnotationDistinctFilter(g.imports, g.webpbOpts.GetRepeatableAnnotation())
	var annotations []string
	for _, src := range [][]string{
		core.GetFieldOpts(field, core.HasFieldJava).GetJava().GetAnnotation(),
		core.GetMessageOpts(descriptor, core.HasMessageJava).GetJava().GetFieldAnnotation(),
		g.fileOpts.GetFieldAnnotation(),
		g.webpbOpts.GetFieldAnnotation(),
	} {
		for _, anno := range src {
			anno = strings.ReplaceAll(anno, "{{_ALIAS_}}", alias)
			anno = strings.ReplaceAll(anno, "{{_FIELD_NAME_}}", string(field.Name()))
			annotations = append(annotations, anno)
		}
	}
	if core.GetFieldOpts(field, core.HasFieldOpt).GetOpt().GetInQuery() {
		annotations = append(annotations, "@"+core.RuntimePackage+".common.InQuery")
	}
	var out []string
	for _, anno := range annotations {
		imported, err := g.imports.ImportAnnotation(anno)
		if err != nil {
			return nil, err
		}
		if filter.Allow(imported) {
			out = append(out, imported)
		}
	}
	return out, nil
}

func (g *MessageGenerator) toType(field protoreflect.FieldDescriptor) (string, error) {
	if typ, ok := javaPrimitiveTypes[field.Kind()]; ok {
		return typ, nil
	}
	return g.imports.ImportGenericDescriptor(core.GetGenericDescriptor(field))
}

func javaGenGetterOrDefault(opts *webpb.JavaFileOpts) bool {
	if opts == nil {
		return true
	}
	return opts.GetGenGetter()
}

func javaGenSetterOrDefault(opts *webpb.JavaFileOpts) bool {
	if opts == nil {
		return true
	}
	return opts.GetGenSetter()
}

func (g *MessageGenerator) getNestedMessages(descriptor protoreflect.MessageDescriptor, level int) ([]string, error) {
	mapFields := map[string]struct{}{}
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		if field.IsMap() {
			mapFields[core.Capitalize(string(field.Name()))+"Entry"] = struct{}{}
		}
	}
	engine, err := sharedJavaEngine()
	if err != nil {
		return nil, err
	}
	var nested []string
	for i := 0; i < descriptor.Messages().Len(); i++ {
		msg := descriptor.Messages().Get(i)
		if _, skip := mapFields[string(msg.Name())]; skip {
			continue
		}
		content, err := g.generate(func() (map[string]any, error) {
			return g.getMessageData(msg, level)
		}, "nested.message", level, engine)
		if err != nil {
			return nil, err
		}
		nested = append(nested, content)
	}
	return nested, nil
}
