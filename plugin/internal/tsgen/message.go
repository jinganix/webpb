package tsgen

import (
	"regexp"
	"sort"
	"strings"

	"github.com/jinganix/webpb/plugin/internal/commons"
	"github.com/jinganix/webpb/plugin/internal/core"
	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"google.golang.org/protobuf/reflect/protoreflect"
)

const unknownType = "unknown"

var tsPrimitiveTypes = map[protoreflect.Kind]string{
	protoreflect.BoolKind:     "boolean",
	protoreflect.BytesKind:    "Uint8Array",
	protoreflect.DoubleKind:   "number",
	protoreflect.Fixed32Kind:  "number",
	protoreflect.Fixed64Kind:  "number",
	protoreflect.FloatKind:    "number",
	protoreflect.Int32Kind:    "number",
	protoreflect.Int64Kind:    "number",
	protoreflect.Sfixed32Kind: "number",
	protoreflect.Sfixed64Kind: "number",
	protoreflect.Sint32Kind:   "number",
	protoreflect.Sint64Kind:   "number",
	protoreflect.StringKind:   "string",
	protoreflect.Uint32Kind:   "number",
	protoreflect.Uint64Kind:   "number",
}

// MessageGenerator generates TypeScript message classes.
type MessageGenerator struct {
	imports   *Imports
	webpbOpts *webpb.TsFileOpts
	fileOpts  *webpb.TsFileOpts
	allFiles  []protoreflect.FileDescriptor
}

// NewMessageGenerator creates a message generator.
func NewMessageGenerator(imports *Imports, fd protoreflect.FileDescriptor, allFiles []protoreflect.FileDescriptor) *MessageGenerator {
	return &MessageGenerator{
		imports:   imports,
		webpbOpts: core.GetWebpbFileOpts(fd, core.HasFileTs).GetTs(),
		fileOpts:  core.GetFileOpts(fd, core.HasFileTs).GetTs(),
		allFiles:  allFiles,
	}
}

// Generate generates TypeScript source for a message.
func (g *MessageGenerator) Generate(descriptor protoreflect.MessageDescriptor) (string, error) {
	if err := core.CheckDuplicatedFields(descriptor); err != nil {
		return "", err
	}
	engine, err := sharedTSEngine()
	if err != nil {
		return "", err
	}
	return g.generate(func() map[string]any {
		return g.getMessageData(descriptor, 0)
	}, "message", 0, engine)
}

func (g *MessageGenerator) generate(supplier func() map[string]any, tmpl string, level int, engine interface {
	Process(string, map[string]any) (string, error)
}) (string, error) {
	g.imports.ImportPath(NewImportPathOrdered("Webpb", "webpb", -1<<31))
	content, err := engine.Process(tmpl, supplier())
	if err != nil {
		return "", err
	}
	re := regexp.MustCompile(`(?m)^(.+)$`)
	indent := strings.Repeat(" ", level*2)
	return re.ReplaceAllString(content, indent+"$1"), nil
}

func (g *MessageGenerator) getMessageData(descriptor protoreflect.MessageDescriptor, level int) map[string]any {
	opts := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
	extend := g.getExtend(descriptor)
	return map[string]any{
		"extendI":       ToInterfaceName(extend),
		"extend":        extend,
		"className":     string(descriptor.Name()),
		"method":        opts.GetMethod(),
		"context":       core.Normalize(opts.GetContext()),
		"path":          g.getPath(descriptor, core.Normalize(opts.GetPath())),
		"fields":        g.getFields(descriptor),
		"nestedMsgs":    g.getNestedMessages(descriptor, level+1),
		"omitted":       g.getOmitted(descriptor),
		"hasAlias":      g.hasAlias(map[protoreflect.MessageDescriptor]struct{}{}, descriptor),
		"aliases":       g.getAliases(descriptor),
		"aliasMsgs":     g.getAliasMsgs(descriptor),
		"sub_type":      g.getSubType(descriptor),
		"sub_type_prop": g.getSubTypeProp(descriptor),
		"sub_values":    g.getSubValues(descriptor),
	}
}

func (g *MessageGenerator) getOmitted(descriptor protoreflect.MessageDescriptor) []string {
	var omitted []string
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		opt := core.GetFieldOpts(field, core.HasFieldOpt).GetOpt()
		if opt.GetInQuery() || opt.GetOmitted() {
			omitted = append(omitted, string(field.Name()))
		}
	}
	return omitted
}

func (g *MessageGenerator) hasAlias(checked map[protoreflect.MessageDescriptor]struct{}, descriptor protoreflect.MessageDescriptor) bool {
	if _, ok := checked[descriptor]; ok {
		return false
	}
	checked[descriptor] = struct{}{}
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		if core.GetFieldOpts(field, core.HasFieldTs).GetTs().GetAlias() != "" {
			return true
		}
		if g.isAutoAlias(descriptor, field) {
			return true
		}
		if !core.IsMessage(field) {
			continue
		}
		if g.hasAlias(checked, field.Message()) {
			return true
		}
	}
	return false
}

func (g *MessageGenerator) isAutoAlias(descriptor protoreflect.MessageDescriptor, field protoreflect.FieldDescriptor) bool {
	return g.webpbOpts.GetAutoAlias() ||
		g.fileOpts.GetAutoAlias() ||
		core.GetMessageOpts(descriptor, core.HasMessageTs).GetTs().GetAutoAlias() ||
		core.GetFieldOpts(field, core.HasFieldTs).GetTs().GetAutoAlias()
}

func (g *MessageGenerator) getAliases(descriptor protoreflect.MessageDescriptor) map[string]string {
	autoAliases := core.GetAutoAliases(descriptor)
	fields := core.GetAllFields(descriptor)
	aliases := map[string]string{}
	for _, field := range fields {
		alias := core.GetFieldOpts(field, core.HasFieldTs).GetTs().GetAlias()
		if alias != "" {
			aliases[string(field.Name())] = alias
		} else if g.isAutoAlias(descriptor, field) {
			aliases[string(field.Name())] = autoAliases[string(field.Name())]
		}
	}
	return aliases
}

func (g *MessageGenerator) getExtend(descriptor protoreflect.MessageDescriptor) string {
	extends := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt().GetExtends()
	if extends == "" {
		return ""
	}
	return g.imports.ImportType(extends)
}

func (g *MessageGenerator) getSubType(descriptor protoreflect.MessageDescriptor) any {
	prop := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt().GetSubType()
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		if string(field.Name()) != prop {
			continue
		}
		if core.IsEnum(field) {
			return g.imports.ImportType(string(field.Enum().FullName()))
		}
		return nil
	}
	return nil
}

func (g *MessageGenerator) getSubTypeProp(descriptor protoreflect.MessageDescriptor) string {
	return core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt().GetSubType()
}

func (g *MessageGenerator) getSubValues(descriptor protoreflect.MessageDescriptor) []string {
	var values []string
	for _, subValue := range core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt().GetSubValues() {
		if valueDescriptor := core.ResolveEnumValue([]protoreflect.FileDescriptor{descriptor.ParentFile()}, subValue); valueDescriptor != nil {
			g.imports.ImportType(string(valueDescriptor.Parent().(protoreflect.EnumDescriptor).FullName()))
			values = append(values, subValue)
			continue
		}
		values = append(values, `"`+subValue+`"`)
	}
	return values
}

func (g *MessageGenerator) getAliasMsgs(descriptor protoreflect.MessageDescriptor) []map[string]string {
	var aliasMsgs []map[string]string
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		value := field
		if field.IsMap() {
			value = core.GetMapValueDescriptor(field)
		}
		if !core.IsMessage(value) {
			continue
		}
		typ := g.toType(value, false)
		if typ == unknownType {
			continue
		}
		collection := "none"
		if field.IsMap() {
			collection = "map"
		} else if field.Cardinality() == protoreflect.Repeated {
			collection = "list"
		}
		aliasMsgs = append(aliasMsgs, map[string]string{
			"name":       string(field.Name()),
			"type":       typ,
			"collection": collection,
		})
	}
	sort.Slice(aliasMsgs, func(i, j int) bool {
		return aliasMsgs[i]["name"] < aliasMsgs[j]["name"]
	})
	return aliasMsgs
}

func (g *MessageGenerator) getPath(descriptor protoreflect.MessageDescriptor, path string) map[string]any {
	if path == "" {
		return map[string]any{}
	}
	group := commons.Of(path)
	core.Validation(group, descriptor, g.allFiles)
	queries := g.getQueries(group)
	data := map[string]any{"raw": path, "url": g.formatURL(group)}
	if len(queries) > 0 {
		data["queries"] = queries
	}
	return data
}

func (g *MessageGenerator) formatURL(group commons.SegmentGroup) string {
	var builder strings.Builder
	for _, segment := range group.PathSegments() {
		builder.WriteString(segment.Prefix())
		builder.WriteString("${")
		builder.WriteString(g.getter(segment.Value()))
		builder.WriteString("}")
	}
	builder.WriteString(group.Suffix())
	return builder.String()
}

func (g *MessageGenerator) getQueries(group commons.SegmentGroup) []map[string]string {
	if len(group.QuerySegments()) == 0 {
		return nil
	}
	var data []map[string]string
	for _, seg := range group.QuerySegments() {
		value := `"` + seg.Value() + `"`
		if seg.IsAccessor() {
			value = g.getter(seg.Value())
		}
		data = append(data, map[string]string{"key": seg.Key(), "value": value})
	}
	return data
}

func (g *MessageGenerator) getter(value string) string {
	if strings.Contains(value, ".") {
		return `Webpb.getter(p, "` + value + `")`
	}
	return "p?." + value
}

func (g *MessageGenerator) getFields(descriptor protoreflect.MessageDescriptor) []map[string]any {
	var fields []map[string]any
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		data := map[string]any{
			"type":       g.getFieldType(field),
			"name":       string(field.Name()),
			"optional":   field.Cardinality() != protoreflect.Required && field.Cardinality() != protoreflect.Repeated,
			"collection": "none",
		}
		if field.IsMap() {
			data["collection"] = "map"
		} else if field.Cardinality() == protoreflect.Repeated {
			data["collection"] = "list"
		}
		if g.containsMessage(field) {
			msgField := field
			if field.IsMap() {
				msgField = core.GetMapValueDescriptor(field)
			}
			msgType := g.toType(msgField, false)
			if msgType != unknownType {
				data["msgType"] = msgType
			}
		}
		if field.HasDefault() {
			value := field.Default()
			if field.Kind() == protoreflect.StringKind {
				data["default"] = `"` + value.String() + `"`
			} else if field.Kind() == protoreflect.BoolKind {
				data["default"] = value.Bool()
			} else {
				data["default"] = value.Int()
			}
		}
		fields = append(fields, data)
	}
	return fields
}

func (g *MessageGenerator) getFieldType(field protoreflect.FieldDescriptor) string {
	if field.IsMap() {
		key := core.GetMapKeyDescriptor(field)
		value := core.GetMapValueDescriptor(field)
		return "Record<" + g.toType(key, false) + ", " + g.toType(value, true) + ">"
	}
	if field.Cardinality() == protoreflect.Repeated {
		return g.toType(field, true) + "[]"
	}
	return g.toType(field, true)
}

func (g *MessageGenerator) containsMessage(field protoreflect.FieldDescriptor) bool {
	if !core.IsMessage(field) {
		return false
	}
	msgField := field
	if field.IsMap() {
		msgField = core.GetMapValueDescriptor(field)
	}
	if _, ok := tsPrimitiveTypes[msgField.Kind()]; ok || !core.IsMessage(msgField) {
		return false
	}
	return core.GetFieldTypeFullName(msgField) != "google.protobuf.Any"
}

func (g *MessageGenerator) toType(field protoreflect.FieldDescriptor, toI bool) string {
	if field.Kind() == protoreflect.Int64Kind || field.Kind() == protoreflect.Sint64Kind || field.Kind() == protoreflect.Sfixed64Kind || field.Kind() == protoreflect.Fixed64Kind || field.Kind() == protoreflect.Uint64Kind {
		if g.webpbOpts.GetInt64AsString() || g.fileOpts.GetInt64AsString() || core.GetFieldOpts(field, core.HasFieldTs).GetTs().GetAsString() {
			return "string"
		}
	}
	if typ, ok := tsPrimitiveTypes[field.Kind()]; ok {
		return typ
	}
	fullName := core.GetFieldTypeFullName(field)
	if fullName == "google.protobuf.Any" {
		return unknownType
	}
	typeName := fullName
	if toI && core.IsMessage(field) {
		typeName = ToInterfaceName(fullName)
	}
	return g.imports.ImportType(typeName)
}

func (g *MessageGenerator) getNestedMessages(descriptor protoreflect.MessageDescriptor, level int) []string {
	mapFields := map[string]struct{}{}
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		if field.IsMap() {
			mapFields[core.Capitalize(string(field.Name()))+"Entry"] = struct{}{}
		}
	}
	engine, _ := sharedTSEngine()
	var nested []string
	for i := 0; i < descriptor.Messages().Len(); i++ {
		msg := descriptor.Messages().Get(i)
		if _, skip := mapFields[string(msg.Name())]; skip {
			continue
		}
		content, err := g.generate(func() map[string]any {
			return g.getMessageData(msg, level)
		}, "nested.message", level, engine)
		if err != nil {
			panic(err)
		}
		nested = append(nested, content)
	}
	return nested
}
