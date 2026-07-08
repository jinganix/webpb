package tsgen

import (
	"fmt"
	"unicode"

	"github.com/jinganix/webpb/plugin/internal/core"
	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// EnumGenerator generates TypeScript enums.
type EnumGenerator struct {
	webpbOpts   *webpb.TsFileOpts
	fileOpts    *webpb.TsFileOpts
	stringValue bool
}

// NewEnumGenerator creates an enum generator.
func NewEnumGenerator(fd protoreflect.FileDescriptor) *EnumGenerator {
	return &EnumGenerator{
		webpbOpts: core.GetWebpbFileOpts(fd, core.HasFileTs).GetTs(),
		fileOpts:  core.GetFileOpts(fd, core.HasFileTs).GetTs(),
	}
}

// Generate generates TypeScript source for an enum.
func (g *EnumGenerator) Generate(descriptor protoreflect.EnumDescriptor) (string, error) {
	engine, err := sharedTSEngine()
	if err != nil {
		return "", err
	}
	g.stringValue = core.IsStringValue(descriptor)
	constEnum := g.isDefaultConstEnum(descriptor)
	primaryKeyword := "enum"
	aliasKeyword := "const enum"
	aliasPrefix := "Const"
	if constEnum {
		primaryKeyword = "const enum"
		aliasKeyword = "enum"
		aliasPrefix = "Enum"
	}
	data := map[string]any{
		"className":      string(descriptor.Name()),
		"enums":          g.getEnums(descriptor),
		"primaryKeyword": primaryKeyword,
		"aliasKeyword":   aliasKeyword,
		"aliasPrefix":    aliasPrefix,
		"autoAlias":      g.isEnumAutoAlias(descriptor),
		"valuesLiteral":  g.isEnumValuesLiteral(descriptor),
		"byName":         g.isEnumByName(descriptor),
		"byValue":        g.isEnumByValue(descriptor),
		"helpers":        g.isEnumHelpers(descriptor),
		"helperPrefix":   enumHelperPrefix(string(descriptor.Name())),
	}
	return engine.Process("enum", data)
}

func (g *EnumGenerator) enumTs(descriptor protoreflect.EnumDescriptor) *webpb.TsEnumOpts {
	return core.GetEnumOpts(descriptor, core.HasEnumTs).GetTs()
}

// resolveBool resolves a tri-state option by cascading enum -> file -> webpb options,
// falling back to def when unset at every level.
func (g *EnumGenerator) resolveBool(
	descriptor protoreflect.EnumDescriptor,
	fromEnum func(*webpb.TsEnumOpts) *bool,
	fromFile func(*webpb.TsFileOpts) *bool,
	def bool,
) bool {
	if enumTs := g.enumTs(descriptor); enumTs != nil {
		if v := fromEnum(enumTs); v != nil {
			return *v
		}
	}
	if g.fileOpts != nil {
		if v := fromFile(g.fileOpts); v != nil {
			return *v
		}
	}
	if g.webpbOpts != nil {
		if v := fromFile(g.webpbOpts); v != nil {
			return *v
		}
	}
	return def
}

func (g *EnumGenerator) isDefaultConstEnum(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.DefaultConstEnum },
		func(o *webpb.TsFileOpts) *bool { return o.DefaultConstEnum },
		false,
	)
}

// isEnumAutoAlias reports whether the secondary alias enum (Enum*/Const*) should be
// generated. Defaults to true to preserve backward-compatible output.
func (g *EnumGenerator) isEnumAutoAlias(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumAutoAlias },
		func(o *webpb.TsFileOpts) *bool { return o.EnumAutoAlias },
		true,
	)
}

// isEnumValuesLiteral reports whether the *Values array should use literal values
// instead of enum member references.
func (g *EnumGenerator) isEnumValuesLiteral(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumValuesLiteral },
		func(o *webpb.TsFileOpts) *bool { return o.EnumValuesLiteral },
		false,
	)
}

// isEnumByName reports whether a name -> value map should be generated.
func (g *EnumGenerator) isEnumByName(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumByName },
		func(o *webpb.TsFileOpts) *bool { return o.EnumByName },
		false,
	)
}

// isEnumByValue reports whether a value -> name map should be generated.
func (g *EnumGenerator) isEnumByValue(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumByValue },
		func(o *webpb.TsFileOpts) *bool { return o.EnumByValue },
		false,
	)
}

// isEnumHelpers reports whether narrow helper functions should be generated.
func (g *EnumGenerator) isEnumHelpers(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumHelpers },
		func(o *webpb.TsFileOpts) *bool { return o.EnumHelpers },
		false,
	)
}

func enumHelperPrefix(className string) string {
	if className == "" {
		return ""
	}
	runes := []rune(className)
	runes[0] = unicode.ToLower(runes[0])
	return string(runes)
}

func (g *EnumGenerator) getEnums(enumDescriptor protoreflect.EnumDescriptor) []map[string]string {
	var enums []map[string]string
	values := enumDescriptor.Values()
	for i := 0; i < values.Len(); i++ {
		descriptor := values.Get(i)
		enums = append(enums, map[string]string{
			"name":  string(descriptor.Name()),
			"value": g.getEnumValue(descriptor),
		})
	}
	return enums
}

func (g *EnumGenerator) getEnumValue(descriptor protoreflect.EnumValueDescriptor) string {
	opts := core.GetEnumValueOpts(descriptor, core.HasEnumValueOpt).GetOpt()
	if opts.GetValue() == "" {
		if g.stringValue {
			return `"` + string(descriptor.Name()) + `"`
		}
		return fmt.Sprintf("%d", int32(descriptor.Number()))
	}
	return `"` + opts.GetValue() + `"`
}
