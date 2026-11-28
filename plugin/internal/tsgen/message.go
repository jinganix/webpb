package tsgen

import (
	"fmt"
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
	if core.IsAugmentMessage(descriptor) {
		if err := core.CheckAugmentMessage(g.allFiles, descriptor); err != nil {
			return "", err
		}
		return "", nil
	}
	if err := core.CheckAugmentTarget(g.allFiles, descriptor); err != nil {
		return "", err
	}
	if err := core.CheckDuplicatedFields(g.allFiles, descriptor); err != nil {
		return "", err
	}
	if err := core.CheckAliasReserve(g.allFiles, descriptor); err != nil {
		return "", err
	}
	engine, err := sharedTSEngine()
	if err != nil {
		return "", err
	}
	return g.generate(func() (map[string]any, error) {
		return g.getMessageData(descriptor, 0)
	}, "message", 0, engine)
}

func (g *MessageGenerator) generate(supplier func() (map[string]any, error), tmpl string, level int, engine interface {
	Process(string, map[string]any) (string, error)
}) (string, error) {
	data, err := supplier()
	if err != nil {
		return "", err
	}
	g.imports.ImportPath(NewImportPathOrdered("Webpb", "webpb", -1<<31))
	content, err := engine.Process(tmpl, data)
	if err != nil {
		return "", err
	}
	re := regexp.MustCompile(`(?m)^(.+)$`)
	indent := strings.Repeat(" ", level*2)
	return re.ReplaceAllString(content, indent+"$1"), nil
}

// validationConstBlock renders the framework-neutral `<Class>Validation`
// descriptor object, or "" when no field declares constraints.
func validationConstBlock(className string, fields []map[string]any) string {
	var lines []string
	for _, field := range fields {
		rule, _ := field["validation"].(string)
		if rule == "" {
			continue
		}
		name, _ := field["name"].(string)
		lines = append(lines, "  "+name+": "+rule+",")
	}
	if len(lines) == 0 {
		return ""
	}
	return "export const " + className + "Validation = {\n" + strings.Join(lines, "\n") + "\n} as const;"
}

func (g *MessageGenerator) getMessageData(descriptor protoreflect.MessageDescriptor, level int) (map[string]any, error) {
	opts := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
	extend, err := g.getExtend(descriptor)
	if err != nil {
		return nil, err
	}
	fields, err := g.getFields(descriptor)
	if err != nil {
		return nil, err
	}
	path, err := g.getPath(descriptor, core.Normalize(opts.GetPath()))
	if err != nil {
		return nil, err
	}
	nested, err := g.getNestedMessages(descriptor, level+1)
	if err != nil {
		return nil, err
	}
	return map[string]any{
		"extendI":       ToInterfaceName(extend),
		"extend":        extend,
		"className":     string(descriptor.Name()),
		"method":        opts.GetMethod(),
		"context":       core.Normalize(opts.GetContext()),
		"path":          path,
		"fields":        fields,
		"validationConst": validationConstBlock(string(descriptor.Name()), fields),
		"nestedMsgs":    nested,
		"omitted":       g.getOmitted(descriptor),
		"hasAlias":      g.hasAlias(map[protoreflect.MessageDescriptor]struct{}{}, descriptor),
		"aliases":       g.getAliases(descriptor),
		"aliasMsgs":     g.getAliasMsgs(descriptor),
		"sub_type":      g.getSubType(descriptor),
		"sub_type_prop": g.getSubTypeProp(descriptor),
		"sub_values":    g.getSubValues(descriptor),
	}, nil
}

func (g *MessageGenerator) getOmitted(descriptor protoreflect.MessageDescriptor) []string {
	var omitted []string
	seen := map[string]struct{}{}
	add := func(name string) {
		if _, ok := seen[name]; ok {
			return
		}
		seen[name] = struct{}{}
		omitted = append(omitted, name)
	}
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		opt := core.GetFieldOpts(field, core.HasFieldOpt).GetOpt()
		if opt.GetInQuery() || opt.GetOmitted() {
			add(string(field.Name()))
		}
	}
	for _, field := range core.GetAugmentFields(g.allFiles, descriptor) {
		opt := core.GetFieldOpts(field, core.HasFieldOpt).GetOpt()
		if opt.GetInQuery() {
			add(string(field.Name()))
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
	autoAliases := core.GetAutoAliases(g.allFiles, descriptor)
	fields := core.GetAllFields(g.allFiles, descriptor)
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

func (g *MessageGenerator) getExtend(descriptor protoreflect.MessageDescriptor) (string, error) {
	opts := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
	extends := opts.GetExtends()
	if extends == "" {
		return "", nil
	}
	if !isValidExtendsRef(extends) {
		return "", fmt.Errorf("extends target not found: %s", extends)
	}
	if core.ResolveMessage(g.allFiles, extends) != nil {
		return g.imports.ImportType(extends), nil
	}
	if !strings.Contains(extends, ".") {
		if g.imports.HasLookup(extends) || g.imports.HasRawIdentifier(extends) {
			return g.imports.ImportType(extends), nil
		}
		// Plain single-part bases without sub_values may be ambient globals
		// (e.g. AbstractClass, Custom) needing no import.
		if len(opts.GetSubValues()) == 0 {
			return g.imports.ImportType(extends), nil
		}
	}
	return "", fmt.Errorf("extends target not found: %s", extends)
}

func isValidExtendsRef(extends string) bool {
	if extends == "" {
		return false
	}
	if strings.HasPrefix(extends, ".") || strings.HasSuffix(extends, ".") || strings.Contains(extends, "..") {
		return false
	}
	for _, part := range strings.Split(extends, ".") {
		if part == "" {
			return false
		}
	}
	return true
}

func (g *MessageGenerator) getSubType(descriptor protoreflect.MessageDescriptor) any {
	prop := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt().GetSubType()
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		if string(field.Name()) != prop {
			continue
		}
		if core.IsEnum(field) {
			return g.imports.ImportEnumType(string(field.Enum().FullName()))
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
			if enum, ok := valueDescriptor.Parent().(protoreflect.EnumDescriptor); ok {
				g.imports.ImportType(string(enum.FullName()))
			}
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

func (g *MessageGenerator) getPath(descriptor protoreflect.MessageDescriptor, path string) (map[string]any, error) {
	if path == "" {
		return map[string]any{}, nil
	}
	group, err := commons.Of(path)
	if err != nil {
		return nil, err
	}
	if err := core.Validation(group, descriptor, g.allFiles); err != nil {
		return nil, err
	}
	queries := g.getQueries(group)
	data := map[string]any{"raw": path, "url": g.formatURL(group)}
	if len(queries) > 0 {
		data["queries"] = queries
	}
	return data, nil
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

func (g *MessageGenerator) getFields(descriptor protoreflect.MessageDescriptor) ([]map[string]any, error) {
	var fields []map[string]any
	fd := descriptor.ParentFile()
	mapping := core.ResolveTsValidationMapping(fd)
	decoratorsEnabled := core.ResolveTsValidationDecoratorsEnabled(fd)
	objectEnabled := core.ResolveTsValidationObjectEnabled(fd)
	appendField := func(field protoreflect.FieldDescriptor) error {
		var decorators []string
		if valid := core.GetFieldValidation(field); valid != nil {
			if err := core.ValidateFieldValidation(field, valid); err != nil {
				return err
			}
			if decoratorsEnabled {
				decorators = core.RenderTsFieldValidation(field, valid, mapping)
			}
		}
		var validationRule string
		if objectEnabled {
			if rule := core.TsValidationRule(core.GetFieldValidation(field)); rule != nil {
				validationRule = core.FormatTsValidationRule(rule)
			}
		}
		if len(decorators) > 0 {
			g.imports.AddRawImport(`import { ` + strings.Join(tsDecoratorNames(decorators), ", ") + ` } from "class-validator";`)
		}
		data := map[string]any{
			"type":       g.getFieldType(field),
			"name":       string(field.Name()),
			"optional":   field.Cardinality() != protoreflect.Required && field.Cardinality() != protoreflect.Repeated,
			"collection": "none",
			"decorators": decorators,
			"validation": validationRule,
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
		return nil
	}
	for i := 0; i < descriptor.Fields().Len(); i++ {
		if err := appendField(descriptor.Fields().Get(i)); err != nil {
			return nil, err
		}
	}
	for _, field := range core.GetAugmentFields(g.allFiles, descriptor) {
		if err := appendField(field); err != nil {
			return nil, err
		}
	}
	return fields, nil
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
	if core.IsEnum(field) {
		return g.imports.ImportEnumType(fullName)
	}
	typeName := fullName
	if toI && core.IsMessage(field) {
		typeName = ToInterfaceName(fullName)
	}
	return g.imports.ImportType(typeName)
}

var tsDecoratorNamePattern = regexp.MustCompile(`^@([A-Za-z0-9_]+)`)

func tsDecoratorNames(decorators []string) []string {
	seen := map[string]struct{}{}
	var out []string
	for _, d := range decorators {
		m := tsDecoratorNamePattern.FindStringSubmatch(strings.TrimSpace(d))
		if len(m) != 2 {
			continue
		}
		if _, ok := seen[m[1]]; ok {
			continue
		}
		seen[m[1]] = struct{}{}
		out = append(out, m[1])
	}
	sort.Strings(out)
	return out
}

func (g *MessageGenerator) getNestedMessages(descriptor protoreflect.MessageDescriptor, level int) ([]string, error) {
	mapFields := map[string]struct{}{}
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		if field.IsMap() {
			mapFields[core.Capitalize(string(field.Name()))+"Entry"] = struct{}{}
		}
	}
	engine, err := sharedTSEngine()
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
