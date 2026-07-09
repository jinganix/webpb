package tsgen

import (
	"fmt"
	"strings"
	"unicode"

	"github.com/jinganix/webpb/plugin/internal/core"
	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"google.golang.org/protobuf/reflect/protoreflect"
)

const (
	enumEmitModeTS        = "ts"
	enumEmitModeJsDts     = "js_dts"
	enumEmitModeTsAndJs   = "ts_and_js_dts"
)

// EnumOutputs holds generated enum artifacts.
type EnumOutputs struct {
	InlineTS string
	DTS      string
	JS       string
}

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

// Generate generates inline TypeScript source for an enum.
func (g *EnumGenerator) Generate(descriptor protoreflect.EnumDescriptor) (string, error) {
	outputs, err := g.GenerateOutputs(descriptor)
	if err != nil {
		return "", err
	}
	return outputs.InlineTS, nil
}

// GenerateOutputs generates inline TS and optional js_dts split files.
func (g *EnumGenerator) GenerateOutputs(descriptor protoreflect.EnumDescriptor) (EnumOutputs, error) {
	engine, err := sharedTSEngine()
	if err != nil {
		return EnumOutputs{}, err
	}
	g.stringValue = core.IsStringValue(descriptor)
	data := g.enumTemplateData(descriptor)
	var outputs EnumOutputs
	mode := g.emitMode(descriptor)
	if mode == enumEmitModeTS {
		inline, err := engine.Process("enum", data)
		if err != nil {
			return EnumOutputs{}, err
		}
		outputs.InlineTS = inline
	}
	if mode == enumEmitModeJsDts || mode == enumEmitModeTsAndJs {
		dts, err := engine.Process("enum.d.ts", data)
		if err != nil {
			return EnumOutputs{}, err
		}
		js, err := engine.Process("enum.js", data)
		if err != nil {
			return EnumOutputs{}, err
		}
		outputs.DTS = dts
		outputs.JS = js
	}
	return outputs, nil
}

// GenerateShim generates a package-level re-export shim for js_dts enums.
func (g *EnumGenerator) GenerateShim(descriptor protoreflect.EnumDescriptor) (string, error) {
	if !g.usesJsDts(descriptor) {
		return "", nil
	}
	engine, err := sharedTSEngine()
	if err != nil {
		return "", err
	}
	g.stringValue = core.IsStringValue(descriptor)
	data := g.enumTemplateData(descriptor)
	return engine.Process("enum.shim", data)
}

// UsesJsDts reports whether an enum is emitted as split .js + .d.ts files.
func (g *EnumGenerator) UsesJsDts(descriptor protoreflect.EnumDescriptor) bool {
	return g.usesJsDts(descriptor)
}

// EnumUsesJsDts reports whether an enum is emitted as .js + .d.ts files.
func EnumUsesJsDts(fd protoreflect.FileDescriptor, descriptor protoreflect.EnumDescriptor) bool {
	return NewEnumGenerator(fd).usesJsDts(descriptor)
}

func (g *EnumGenerator) usesJsDts(descriptor protoreflect.EnumDescriptor) bool {
	mode := g.emitMode(descriptor)
	return mode == enumEmitModeJsDts || mode == enumEmitModeTsAndJs
}

func (g *EnumGenerator) enumTemplateData(descriptor protoreflect.EnumDescriptor) map[string]any {
	constEnum := g.isDefaultConstEnum(descriptor)
	primaryKeyword := "enum"
	aliasKeyword := "const enum"
	aliasPrefix := "Const"
	if constEnum {
		primaryKeyword = "const enum"
		aliasKeyword = "enum"
		aliasPrefix = "Enum"
	}
	return map[string]any{
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
}

func (g *EnumGenerator) enumTs(descriptor protoreflect.EnumDescriptor) *webpb.TsEnumOpts {
	return core.GetEnumOpts(descriptor, core.HasEnumTs).GetTs()
}

func (g *EnumGenerator) emitMode(descriptor protoreflect.EnumDescriptor) string {
	mode := g.resolveString(
		descriptor,
		func(o *webpb.TsEnumOpts) *string { return o.EnumEmitMode },
		func(o *webpb.TsFileOpts) *string { return o.EnumEmitMode },
		enumEmitModeTS,
	)
	switch mode {
	case enumEmitModeJsDts, enumEmitModeTsAndJs:
		return mode
	default:
		return enumEmitModeTS
	}
}

func (g *EnumGenerator) resolveString(
	descriptor protoreflect.EnumDescriptor,
	fromEnum func(*webpb.TsEnumOpts) *string,
	fromFile func(*webpb.TsFileOpts) *string,
	def string,
) string {
	if enumTs := g.enumTs(descriptor); enumTs != nil {
		if v := fromEnum(enumTs); v != nil && strings.TrimSpace(*v) != "" {
			return strings.TrimSpace(*v)
		}
	}
	if g.fileOpts != nil {
		if v := fromFile(g.fileOpts); v != nil && strings.TrimSpace(*v) != "" {
			return strings.TrimSpace(*v)
		}
	}
	if g.webpbOpts != nil {
		if v := fromFile(g.webpbOpts); v != nil && strings.TrimSpace(*v) != "" {
			return strings.TrimSpace(*v)
		}
	}
	return def
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

func (g *EnumGenerator) isEnumAutoAlias(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumAutoAlias },
		func(o *webpb.TsFileOpts) *bool { return o.EnumAutoAlias },
		true,
	)
}

func (g *EnumGenerator) isEnumValuesLiteral(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumValuesLiteral },
		func(o *webpb.TsFileOpts) *bool { return o.EnumValuesLiteral },
		false,
	)
}

func (g *EnumGenerator) isEnumByName(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumByName },
		func(o *webpb.TsFileOpts) *bool { return o.EnumByName },
		false,
	)
}

func (g *EnumGenerator) isEnumByValue(descriptor protoreflect.EnumDescriptor) bool {
	return g.resolveBool(
		descriptor,
		func(o *webpb.TsEnumOpts) *bool { return o.EnumByValue },
		func(o *webpb.TsFileOpts) *bool { return o.EnumByValue },
		false,
	)
}

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

func wrapEnumSplitFile(protoPath, body string) string {
	content := "// Code generated by Webpb compiler, do not edit.\n" +
		"// https://github.com/jinganix/webpb\n" +
		"// " + protoPath + "\n\n" +
		body
	return normalizeTsTemplateOutput(content)
}
