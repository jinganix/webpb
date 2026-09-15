package gogen

import (
	"fmt"
	"go/format"
	"path"
	"strconv"
	"strings"

	"github.com/jinganix/webpb/plugin/gen/webpb"
	"github.com/jinganix/webpb/plugin/internal/commons"
	"github.com/jinganix/webpb/plugin/internal/core"
	"google.golang.org/protobuf/reflect/protodesc"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// Options configures Go code generation.
type Options struct {
	// Package is the Go package name used by every generated file.
	Package string
	// Module is the import prefix of the generated package (documentation only).
	Module string
	// RuntimeImport is the import path of the Go runtime referenced by
	// polymorphism (sub_type/sub_values) helpers.
	RuntimeImport string
	// AutoAlias derives wire keys from proto field names for every file.
	AutoAlias bool
	// EnumAutoAlias emits the alias enum type for every enum.
	EnumAutoAlias bool
	// Int64AsString renders int64/uint64 fields as Go strings for every file.
	Int64AsString bool
}

const defaultRuntimeImport = "github.com/jinganix/webpb/runtime/go"

// File is a generated source file.
type File struct {
	Name    string
	Content string
}

// Generator renders protobuf descriptors into Go source.
type Generator struct{}

// NewGenerator creates a Go generator.
func NewGenerator() *Generator {
	return &Generator{}
}

func shouldIgnore(packageName string) bool {
	return packageName == "" || strings.Contains(packageName, "google.protobuf")
}

func firstUpper(name string) string {
	if name == "" {
		return name
	}
	return strings.ToUpper(name[:1]) + name[1:]
}

// goName converts a snake_case protobuf identifier to an exported Go name.
func goName(name string) string {
	parts := strings.Split(name, "_")
	var b strings.Builder
	for _, part := range parts {
		if part == "" {
			continue
		}
		b.WriteString(firstUpper(part))
	}
	if b.Len() == 0 {
		return firstUpper(name)
	}
	return b.String()
}

// goTypeName flattens nested declarations as Parent_Child.
func goTypeName(descriptor protoreflect.Descriptor) string {
	name := string(descriptor.Name())
	if message, ok := descriptor.Parent().(protoreflect.MessageDescriptor); ok {
		return goTypeName(message) + "_" + name
	}
	return name
}

func scalarType(kind protoreflect.Kind) string {
	switch kind {
	case protoreflect.DoubleKind:
		return "float64"
	case protoreflect.FloatKind:
		return "float32"
	case protoreflect.Int64Kind, protoreflect.Sint64Kind, protoreflect.Sfixed64Kind:
		return "int64"
	case protoreflect.Uint64Kind, protoreflect.Fixed64Kind:
		return "uint64"
	case protoreflect.Int32Kind, protoreflect.Sint32Kind, protoreflect.Sfixed32Kind:
		return "int32"
	case protoreflect.Uint32Kind, protoreflect.Fixed32Kind:
		return "uint32"
	case protoreflect.BoolKind:
		return "bool"
	case protoreflect.StringKind:
		return "string"
	case protoreflect.BytesKind:
		return "[]byte"
	default:
		return "any"
	}
}

func elementType(field protoreflect.FieldDescriptor, int64AsString bool) string {
	switch field.Kind() {
	case protoreflect.EnumKind:
		return goTypeName(field.Enum())
	case protoreflect.MessageKind, protoreflect.GroupKind:
		return "*" + goTypeName(field.Message())
	case protoreflect.Int64Kind, protoreflect.Sint64Kind, protoreflect.Sfixed64Kind,
		protoreflect.Uint64Kind, protoreflect.Fixed64Kind:
		if int64AsString {
			return "string"
		}
		return scalarType(field.Kind())
	default:
		return scalarType(field.Kind())
	}
}

// needsPointer gives singular fields with explicit presence (proto2 optional /
// required, proto3 optional, oneof members) a pointer so unset values are
// distinguishable on the wire.
func needsPointer(field protoreflect.FieldDescriptor) bool {
	if field.IsList() || field.IsMap() || !field.HasPresence() {
		return false
	}
	switch field.Kind() {
	case protoreflect.BytesKind, protoreflect.MessageKind, protoreflect.GroupKind:
		return false
	}
	return true
}

func fieldType(field protoreflect.FieldDescriptor, int64AsString bool) string {
	if field.IsMap() {
		key := scalarType(core.GetMapKeyDescriptor(field).Kind())
		value := elementType(core.GetMapValueDescriptor(field), int64AsString)
		return "map[" + key + "]" + value
	}
	if field.IsList() {
		return "[]" + elementType(field, int64AsString)
	}
	base := elementType(field, int64AsString)
	if needsPointer(field) {
		return "*" + base
	}
	return base
}

// goSettings captures the resolved Go language options for one proto file.
type goSettings struct {
	autoAlias     bool
	enumAutoAlias bool
	imports       []string
	int64AsString bool
	pkg           string
}

func resolveSettings(fd protoreflect.FileDescriptor, options Options) goSettings {
	settings := goSettings{
		autoAlias:     options.AutoAlias,
		enumAutoAlias: options.EnumAutoAlias,
		int64AsString: options.Int64AsString,
		pkg:           options.Package,
	}
	apply := func(goOpts *webpb.GoFileOpts) {
		if goOpts == nil {
			return
		}
		settings.autoAlias = settings.autoAlias || goOpts.GetAutoAlias()
		settings.enumAutoAlias = settings.enumAutoAlias || goOpts.GetEnumAutoAlias()
		settings.int64AsString = settings.int64AsString || goOpts.GetInt64AsString()
		settings.imports = append(settings.imports, goOpts.GetImport()...)
		if settings.pkg == "" && goOpts.GetPackage() != "" {
			settings.pkg = goOpts.GetPackage()
		}
	}
	apply(core.GetWebpbFileOpts(fd, core.HasFileGo).GetGo())
	apply(core.GetFileOpts(fd, core.HasFileGo).GetGo())
	if settings.pkg == "" {
		settings.pkg = goPackageOption(fd)
	}
	if settings.pkg == "" {
		settings.pkg = "webpbpb"
	}
	settings.imports = dedupeStrings(settings.imports)
	return settings
}

// goPackageOption reads the standard `go_package` file option.
func goPackageOption(fd protoreflect.FileDescriptor) string {
	options := protodesc.ToFileDescriptorProto(fd).GetOptions()
	value := options.GetGoPackage()
	if value == "" {
		return ""
	}
	if idx := strings.LastIndex(value, ";"); idx >= 0 {
		return value[idx+1:]
	}
	return path.Base(value)
}

func dedupeStrings(values []string) []string {
	seen := map[string]struct{}{}
	var out []string
	for _, value := range values {
		if _, ok := seen[value]; ok {
			continue
		}
		seen[value] = struct{}{}
		out = append(out, value)
	}
	return out
}

func escapeFmt(value string) string {
	return strings.ReplaceAll(value, "%", "%%")
}

// defaultLiteral renders a proto2 explicit default into a Go literal.
func defaultLiteral(field protoreflect.FieldDescriptor) (string, bool) {
	if field.IsList() || field.IsMap() || !field.HasDefault() {
		return "", false
	}
	value := field.Default()
	switch field.Kind() {
	case protoreflect.StringKind:
		return strconv.Quote(value.String()), true
	case protoreflect.BoolKind:
		return strconv.FormatBool(value.Bool()), true
	case protoreflect.Int32Kind, protoreflect.Sint32Kind, protoreflect.Sfixed32Kind:
		return strconv.FormatInt(value.Int(), 10), true
	case protoreflect.Int64Kind, protoreflect.Sint64Kind, protoreflect.Sfixed64Kind:
		return strconv.FormatInt(value.Int(), 10), true
	case protoreflect.Uint32Kind, protoreflect.Fixed32Kind, protoreflect.Uint64Kind, protoreflect.Fixed64Kind:
		return strconv.FormatUint(value.Uint(), 10), true
	case protoreflect.FloatKind, protoreflect.DoubleKind:
		return strconv.FormatFloat(value.Float(), 'g', -1, 64), true
	case protoreflect.EnumKind:
		enumValue := field.Enum().Values().ByNumber(value.Enum())
		if enumValue == nil {
			return "", false
		}
		return goTypeName(field.Enum()) + "_" + string(enumValue.Name()), true
	default:
		return "", false
	}
}

// toSnakeCase converts a PascalCase file base into snake_case, matching the
// protoc-gen-go naming convention.
func toSnakeCase(name string) string {
	var b strings.Builder
	for i, r := range name {
		if r >= 'A' && r <= 'Z' {
			if i > 0 {
				b.WriteByte('_')
			}
			b.WriteRune(r - 'A' + 'a')
			continue
		}
		b.WriteRune(r)
	}
	return b.String()
}

func accessorExpr(receiver, accessor string) string {
	expr := receiver
	for _, part := range strings.Split(accessor, ".") {
		expr += "." + goName(part)
	}
	return expr
}

// pathExpr renders a URL template into a Go format string plus arguments.
func pathExpr(receiver, rawPath string) (string, []string) {
	group := commons.Of(core.Normalize(rawPath))
	var b strings.Builder
	var args []string
	for _, segment := range group.PathSegments() {
		b.WriteString(escapeFmt(segment.Prefix()))
		if segment.IsAccessor() {
			b.WriteString("%v")
			args = append(args, accessorExpr(receiver, segment.Value()))
		} else {
			b.WriteString(escapeFmt(segment.Value()))
		}
	}
	b.WriteString(escapeFmt(group.Suffix()))
	queries := group.QuerySegments()
	if len(queries) > 0 {
		b.WriteString("?")
		parts := make([]string, 0, len(queries))
		for _, query := range queries {
			value := query.Value()
			if query.IsAccessor() {
				value = "%v"
				args = append(args, accessorExpr(receiver, query.Value()))
			}
			if query.Key() != "" {
				parts = append(parts, escapeFmt(query.Key())+"="+value)
			} else {
				parts = append(parts, value)
			}
		}
		b.WriteString(strings.Join(parts, "&"))
	}
	return b.String(), args
}

func collectEnums(fd protoreflect.FileDescriptor) []protoreflect.EnumDescriptor {
	var out []protoreflect.EnumDescriptor
	var walkMessage func(protoreflect.MessageDescriptor)
	walkMessage = func(message protoreflect.MessageDescriptor) {
		for i := 0; i < message.Enums().Len(); i++ {
			out = append(out, message.Enums().Get(i))
		}
		for i := 0; i < message.Messages().Len(); i++ {
			walkMessage(message.Messages().Get(i))
		}
	}
	for i := 0; i < fd.Enums().Len(); i++ {
		out = append(out, fd.Enums().Get(i))
	}
	for i := 0; i < fd.Messages().Len(); i++ {
		walkMessage(fd.Messages().Get(i))
	}
	return out
}

func collectMessages(fd protoreflect.FileDescriptor) []protoreflect.MessageDescriptor {
	var out []protoreflect.MessageDescriptor
	var walk func(protoreflect.MessageDescriptor)
	walk = func(message protoreflect.MessageDescriptor) {
		out = append(out, message)
		for i := 0; i < message.Messages().Len(); i++ {
			walk(message.Messages().Get(i))
		}
	}
	for i := 0; i < fd.Messages().Len(); i++ {
		walk(fd.Messages().Get(i))
	}
	return out
}

func renderEnum(enum protoreflect.EnumDescriptor, aliasEnabled bool) string {
	primary := renderPrimaryEnum(enum)
	if !aliasEnabled {
		return primary
	}
	name := goTypeName(enum)
	aliasName := "Const" + name
	stringValue := core.IsStringValue(enum)
	var b strings.Builder
	b.WriteString(primary)
	if stringValue {
		fmt.Fprintf(&b, "\ntype %s string\n\n", aliasName)
	} else {
		fmt.Fprintf(&b, "\ntype %s int32\n\n", aliasName)
	}
	b.WriteString("const (\n")
	values := enum.Values()
	for i := 0; i < values.Len(); i++ {
		value := values.Get(i)
		constName := aliasName + "_" + string(value.Name())
		if stringValue {
			literal := core.GetEnumValueOpts(value, core.HasEnumValueOpt).GetOpt().GetValue()
			if literal == "" {
				literal = string(value.Name())
			}
			fmt.Fprintf(&b, "\t%s %s = %q\n", constName, aliasName, literal)
		} else {
			fmt.Fprintf(&b, "\t%s %s = %d\n", constName, aliasName, value.Number())
		}
	}
	b.WriteString(")\n")
	fmt.Fprintf(&b, "\nfunc (x %s) Alias() %s { return %s(x) }\n", name, aliasName, aliasName)
	fmt.Fprintf(&b, "\nfunc (x %s) Value() %s { return %s(x) }\n", aliasName, name, name)
	return b.String()
}

func renderPrimaryEnum(enum protoreflect.EnumDescriptor) string {
	name := goTypeName(enum)
	stringValue := core.IsStringValue(enum)
	var b strings.Builder
	if stringValue {
		fmt.Fprintf(&b, "type %s string\n\n", name)
	} else {
		fmt.Fprintf(&b, "type %s int32\n\n", name)
	}
	b.WriteString("const (\n")
	values := enum.Values()
	for i := 0; i < values.Len(); i++ {
		value := values.Get(i)
		constName := name + "_" + string(value.Name())
		if stringValue {
			literal := core.GetEnumValueOpts(value, core.HasEnumValueOpt).GetOpt().GetValue()
			if literal == "" {
				literal = string(value.Name())
			}
			fmt.Fprintf(&b, "\t%s %s = %q\n", constName, name, literal)
		} else {
			fmt.Fprintf(&b, "\t%s %s = %d\n", constName, name, value.Number())
		}
	}
	b.WriteString(")\n")
	if stringValue {
		return b.String()
	}
	fmt.Fprintf(&b, "\nvar %sNames = map[%s]string{\n", name, name)
	for i := 0; i < values.Len(); i++ {
		fmt.Fprintf(&b, "\t%s_%s: %q,\n", name, string(values.Get(i).Name()), string(values.Get(i).Name()))
	}
	b.WriteString("}\n")
	fmt.Fprintf(&b, "\nfunc (x %s) String() string {\n", name)
	fmt.Fprintf(&b, "\tif name, ok := %sNames[x]; ok {\n\t\treturn name\n\t}\n\treturn \"\"\n}\n", name)
	return b.String()
}

func renderMessage(
	all []protoreflect.FileDescriptor,
	message protoreflect.MessageDescriptor,
	settings goSettings,
) (string, error) {
	options := core.GetMessageOpts(message, core.HasMessageOpt).GetOpt()
	if options.GetAugmentOf() != "" || message.IsMapEntry() {
		return "", nil
	}
	if err := core.CheckAliasReserve(all, message); err != nil {
		return "", err
	}
	msgGo := core.GetMessageOpts(message, core.HasMessageGo).GetGo()
	autoAlias := settings.autoAlias || msgGo.GetAutoAlias()
	name := goTypeName(message)
	aliases := core.GetAutoAliases(all, message)
	fields := core.GetAllFields(all, message)

	var b strings.Builder
	b.WriteString("const (\n")
	fmt.Fprintf(&b, "\t%sClass = %q\n", name, string(message.Name()))
	fmt.Fprintf(&b, "\t%sMethod = %q\n", name, options.GetMethod())
	fmt.Fprintf(&b, "\t%sContext = %q\n", name, options.GetContext())
	b.WriteString(")\n\n")

	fmt.Fprintf(&b, "type %s struct {\n", name)
	rendered := make([]protoreflect.FieldDescriptor, 0, len(fields))
	goValidation := core.ResolveGoValidationMapping(message.ParentFile())
	for _, field := range fields {
		fieldGo := core.GetFieldOpts(field, core.HasFieldGo).GetGo()
		if fieldGo.GetOmitted() {
			continue
		}
		if valid := core.GetFieldValidation(field); valid != nil {
			if err := core.ValidateFieldValidation(field, valid); err != nil {
				return "", err
			}
		}
		rendered = append(rendered, field)
		alias := fieldGo.GetAlias()
		if alias == "" && (autoAlias || fieldGo.GetAutoAlias()) {
			alias = aliases[string(field.Name())]
		}
		if alias == "" {
			alias = string(field.Name())
		}
		validateTag := core.RenderGoValidateTag(field, core.GetFieldValidation(field), goValidation)
		tag := fmt.Sprintf("webpb:%q json:%q", alias, alias)
		if validateTag != "" {
			tag += fmt.Sprintf(" validate:%q", validateTag)
		}
		fmt.Fprintf(
			&b,
			"\t%s %s `%s`\n",
			goName(string(field.Name())),
			fieldType(field, settings.int64AsString || fieldGo.GetAsString()),
			tag,
		)
	}
	b.WriteString("}\n\n")

	hasDefaults := false
	for _, field := range rendered {
		if _, ok := defaultLiteral(field); ok {
			hasDefaults = true
			break
		}
	}
	fmt.Fprintf(&b, "func New%s() *%s {\n", name, name)
	if hasDefaults {
		fmt.Fprintf(&b, "\treturn &%s{\n", name)
		for _, field := range rendered {
			literal, ok := defaultLiteral(field)
			if !ok {
				continue
			}
			if needsPointer(field) {
				literal = "webpbPtr(" + literal + ")"
			}
			fmt.Fprintf(&b, "\t\t%s: %s,\n", goName(string(field.Name())), literal)
		}
		b.WriteString("\t}\n}\n\n")
	} else {
		fmt.Fprintf(&b, "\treturn &%s{}\n}\n\n", name)
	}
	fmt.Fprintf(&b, "func (m *%s) WebpbClass() string { return %sClass }\n", name, name)
	fmt.Fprintf(&b, "func (m *%s) WebpbMethod() string { return %sMethod }\n", name, name)
	fmt.Fprintf(&b, "func (m *%s) WebpbContext() string { return %sContext }\n", name, name)

	format, args := pathExpr("m", options.GetPath())
	if len(args) == 0 {
		fmt.Fprintf(&b, "func (m *%s) WebpbPath() string { return %q }\n", name, format)
	} else {
		fmt.Fprintf(
			&b,
			"func (m *%s) WebpbPath() string { return fmt.Sprintf(%q, %s) }\n",
			name,
			format,
			strings.Join(args, ", "),
		)
	}

	oneofs := message.Oneofs()
	for i := 0; i < oneofs.Len(); i++ {
		oneof := oneofs.Get(i)
		if oneof.IsSynthetic() {
			continue
		}
		fmt.Fprintf(&b, "\nfunc (m *%s) %sCase() string {\n", name, goName(string(oneof.Name())))
		for i := 0; i < oneof.Fields().Len(); i++ {
			field := oneof.Fields().Get(i)
			fmt.Fprintf(
				&b,
				"\tif m.%s != nil {\n\t\treturn %q\n\t}\n",
				goName(string(field.Name())),
				string(field.Name()),
			)
		}
		b.WriteString("\treturn \"\"\n}\n")
	}

	if iface := msgGo.GetImplementsInterface(); iface != "" {
		fmt.Fprintf(
			&b,
			"\ntype %s interface {\n\tWebpbClass() string\n\tWebpbContext() string\n\tWebpbMethod() string\n\tWebpbPath() string\n}\n",
			iface,
		)
		fmt.Fprintf(&b, "\nvar _ %s = (*%s)(nil)\n", iface, name)
	}
	if extends := options.GetExtends(); extends != "" {
		if base := core.ResolveMessage(all, extends); base != nil {
			baseIface := core.GetMessageOpts(base, core.HasMessageGo).GetGo().GetImplementsInterface()
			if baseIface != "" {
				fmt.Fprintf(&b, "\nvar _ %s = (*%s)(nil)\n", baseIface, name)
			}
		}
	}
	return b.String(), nil
}

// PackageFor resolves the Go package name that Generate would use for a file.
func (g *Generator) PackageFor(fd protoreflect.FileDescriptor, options Options) string {
	return resolveSettings(fd, options).pkg
}

// HelpersFile returns the shared package helper used for optional (pointer)
// proto2 defaults. The CLI emits it once per run.
func (g *Generator) HelpersFile(pkg string) *File {
	if pkg == "" {
		pkg = "webpbpb"
	}
	return &File{
		Name:    pkg + "/webpb_helpers.pb.go",
		Content: "// " + core.Header + "\n// " + core.GitURL + "\n\npackage " + pkg + "\n\n" +
			"func webpbPtr[T any](value T) *T { return &value }\n",
	}
}

// renderSubTypes emits polymorphism helpers for base messages that declare
// `sub_type` and the sub messages that register via `extends`/`sub_values`.
func renderSubTypes(
	all []protoreflect.FileDescriptor,
	messages []protoreflect.MessageDescriptor,
	settings goSettings,
) ([]string, error) {
	type registration struct {
		subGoName string
		value     string
	}
	registrations := map[string][]registration{}
	bases := map[string]protoreflect.MessageDescriptor{}
	for _, message := range messages {
		options := core.GetMessageOpts(message, core.HasMessageOpt).GetOpt()
		if options.GetSubType() != "" {
			bases[goTypeName(message)] = message
			continue
		}
		if options.GetExtends() == "" || len(options.GetSubValues()) == 0 {
			continue
		}
		base := core.ResolveMessage(all, options.GetExtends())
		baseName := options.GetExtends()
		if base != nil {
			baseName = goTypeName(base)
		}
		if _, ok := bases[baseName]; !ok && base == nil {
			for _, candidate := range messages {
				if goTypeName(candidate) == baseName {
					bases[baseName] = candidate
					break
				}
			}
		}
		for _, value := range options.GetSubValues() {
			registrations[baseName] = append(registrations[baseName], registration{
				subGoName: goTypeName(message),
				value:     value,
			})
		}
	}

	var blocks []string
	for baseName, base := range bases {
		entries := registrations[baseName]
		options := core.GetMessageOpts(base, core.HasMessageOpt).GetOpt()
		prop := options.GetSubType()
		propAlias := prop
		if settings.autoAlias {
			if alias, ok := core.GetAutoAliases(all, base)[prop]; ok && alias != "" {
				propAlias = alias
			}
		}
		var b strings.Builder
		fmt.Fprintf(&b, "var %sSubTypes = webpb.NewSubTypeRegistry()\n\n", baseName)
		b.WriteString("func init() {\n")
		for _, entry := range entries {
			fmt.Fprintf(
				&b,
				"\t%sSubTypes.Register(%q, %q, func(data map[string]any) (webpb.Message, error) {\n",
				baseName,
				string(base.Name()),
				entry.value,
			)
			fmt.Fprintf(&b, "\t\tmessage := New%s()\n", entry.subGoName)
			b.WriteString("\t\treturn message, webpb.Populate(data, message)\n\t})\n")
		}
		b.WriteString("}\n\n")
		fmt.Fprintf(&b, "func %sFromAliasAny(data map[string]any) (webpb.Message, error) {\n", baseName)
		fmt.Fprintf(
			&b,
			"\tif message, err := %sSubTypes.Create(%q, fmt.Sprint(data[%q]), data); err != nil || message != nil {\n",
			baseName,
			string(base.Name()),
			propAlias,
		)
		b.WriteString("\t\treturn message, err\n\t}\n")
		fmt.Fprintf(&b, "\tmessage := New%s()\n", baseName)
		b.WriteString("\treturn message, webpb.Populate(data, message)\n}\n")
		blocks = append(blocks, b.String())
	}
	sortStrings(blocks)
	return blocks, nil
}

func sortStrings(values []string) {
	for i := 1; i < len(values); i++ {
		for j := i; j > 0 && values[j] < values[j-1]; j-- {
			values[j], values[j-1] = values[j-1], values[j]
		}
	}
}

// Generate renders every enum and message of one proto file into a Go source
// file. All generated files of a run share one Go package.
func (g *Generator) Generate(
	all []protoreflect.FileDescriptor,
	fd protoreflect.FileDescriptor,
	options Options,
) (*File, error) {
	if shouldIgnore(string(fd.Package())) {
		return &File{}, nil
	}
	settings := resolveSettings(fd, options)
	pkg := settings.pkg
	runtimeImport := options.RuntimeImport
	if runtimeImport == "" {
		runtimeImport = defaultRuntimeImport
	}

	usesFmt := false
	var blocks []string
	for _, enum := range collectEnums(fd) {
		enumGo := core.GetEnumOpts(enum, core.HasEnumGo).GetGo()
		aliasEnabled := settings.enumAutoAlias || enumGo.GetAutoAlias()
		blocks = append(blocks, renderEnum(enum, aliasEnabled))
	}
	messages := collectMessages(fd)
	for _, message := range messages {
		if _, args := pathExpr("m", core.GetMessageOpts(message, core.HasMessageOpt).GetOpt().GetPath()); len(args) > 0 {
			usesFmt = true
		}
		rendered, err := renderMessage(all, message, settings)
		if err != nil {
			return nil, err
		}
		if rendered != "" {
			blocks = append(blocks, rendered)
		}
	}
	subTypeBlocks, err := renderSubTypes(all, messages, settings)
	if err != nil {
		return nil, err
	}
	if len(subTypeBlocks) > 0 {
		usesFmt = true
		blocks = append(blocks, subTypeBlocks...)
	}
	if len(blocks) == 0 {
		return &File{}, nil
	}

	var b strings.Builder
	fmt.Fprintf(&b, "// %s\n", core.Header)
	fmt.Fprintf(&b, "// %s\n", core.GitURL)
	fmt.Fprintf(&b, "// %s\n\n", fd.Path())
	fmt.Fprintf(&b, "package %s\n\n", pkg)
	var imports []string
	if usesFmt {
		imports = append(imports, "\"fmt\"")
	}
	if len(subTypeBlocks) > 0 {
		imports = append(imports, "webpb "+strconv.Quote(runtimeImport))
	}
	if len(imports) > 0 {
		b.WriteString("import (\n")
		for _, line := range imports {
			fmt.Fprintf(&b, "\t%s\n", line)
		}
		b.WriteString(")\n\n")
	}
	b.WriteString(strings.Join(blocks, "\n"))

	base := toSnakeCase(strings.TrimSuffix(path.Base(fd.Path()), path.Ext(fd.Path())))
	content := strings.TrimRight(b.String(), "\n") + "\n"
	formatted, err := format.Source([]byte(content))
	if err != nil {
		return nil, fmt.Errorf("format %s: %w", fd.Path(), err)
	}
	return &File{
		Name:    pkg + "/" + base + ".pb.go",
		Content: string(formatted),
	}, nil
}
