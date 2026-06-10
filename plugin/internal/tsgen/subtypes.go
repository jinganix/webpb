package tsgen

import (
	"fmt"
	"strings"

	"github.com/jinganix/webpb/plugin/internal/core"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// SubTypesGenerator generates subtype union files.
type SubTypesGenerator struct{}

// Generate generates SubTypeTypes files.
func (g *SubTypesGenerator) Generate(descriptors []protoreflect.FileDescriptor) (map[string]string, error) {
	engine, err := sharedTSEngine()
	if err != nil {
		return nil, err
	}
	extendsMap := map[string][]protoreflect.MessageDescriptor{}
	for _, fileDescriptor := range descriptors {
		messages := fileDescriptor.Messages()
		for i := 0; i < messages.Len(); i++ {
			descriptor := messages.Get(i)
			opt := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
			if opt.GetExtends() == "" || len(opt.GetSubValues()) == 0 {
				continue
			}
			extendsMap[opt.GetExtends()] = append(extendsMap[opt.GetExtends()], descriptor)
		}
	}
	fileMap := map[string]string{}
	for typeName, descs := range extendsMap {
		content, err := g.genContent(engine, typeName, descs)
		if err != nil {
			return nil, err
		}
		fileMap[typeName+"Types.ts"] = content
	}
	return fileMap, nil
}

func (g *SubTypesGenerator) genContent(engine interface {
	Process(string, map[string]any) (string, error)
}, typeName string, descriptors []protoreflect.MessageDescriptor) (string, error) {
	imports := NewEmptyImports()
	var reexportLines []string
	var unionLines []string
	for _, descriptor := range descriptors {
		ifaceFull := ToInterfaceName(string(descriptor.FullName()))
		imports.ImportType(string(descriptor.FullName()))
		if idx := strings.LastIndex(ifaceFull, "."); idx >= 0 {
			reexportLines = append(reexportLines, fmt.Sprintf(
				`export type { %s } from "./%s";`, ifaceFull[idx+1:], ifaceFull[:idx]))
		}
		unionLines = append(unionLines, "  | "+ifaceFull)
	}
	importLines := imports.ToList()
	var headerParts []string
	if len(reexportLines) > 0 {
		headerParts = append(headerParts, strings.Join(reexportLines, "\n"))
	}
	if len(importLines) > 0 {
		headerParts = append(headerParts, strings.Join(importLines, "\n"))
	}
	data := map[string]any{
		"types_name": typeName + "Types",
		"header":     strings.Join(headerParts, "\n"),
		"union":      strings.Join(unionLines, "\n"),
		"hasHeader":  len(headerParts) > 0,
	}
	content, err := engine.Process("file.types", data)
	if err != nil {
		return "", err
	}
	return normalizeTsTemplateOutput(content), nil
}
