package tsgen

import (
	"fmt"

	"github.com/jinganix/webpb/plugin/internal/core"
	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// EnumGenerator generates TypeScript enums.
type EnumGenerator struct {
	webpbOpts *webpb.TsFileOpts
	fileOpts  *webpb.TsFileOpts
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
	data := map[string]any{
		"className": string(descriptor.Name()),
		"enums":     g.getEnums(descriptor),
	}
	tmpl := "enum"
	if g.isDefaultConstEnum() {
		tmpl = "const.enum"
	}
	return engine.Process(tmpl, data)
}

func (g *EnumGenerator) isDefaultConstEnum() bool {
	if g.fileOpts != nil && g.fileOpts.DefaultConstEnum != nil {
		return g.fileOpts.GetDefaultConstEnum()
	}
	if g.webpbOpts != nil && g.webpbOpts.DefaultConstEnum != nil {
		return g.webpbOpts.GetDefaultConstEnum()
	}
	return false
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
