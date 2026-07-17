package tsgen

import (
	"sort"
	"strings"
	"sync"

	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/template"
	"google.golang.org/protobuf/reflect/protoreflect"
)

var (
	tsEngineOnce sync.Once
	tsEngine     *template.Engine
	tsEngineErr  error
)

func sharedTSEngine() (*template.Engine, error) {
	tsEngineOnce.Do(func() {
		tsEngine, tsEngineErr = template.NewTSEngine()
	})
	return tsEngine, tsEngineErr
}

// Generator generates TypeScript source files.
type Generator struct{}

// FileOutput holds the main package file and any extra enum split files.
type FileOutput struct {
	MainTS string
	Extra  map[string]string
}

// NewGenerator creates a TypeScript generator.
func NewGenerator() *Generator {
	return &Generator{}
}

// Generate generates TypeScript outputs for a protobuf file.
func (g *Generator) Generate(ctx *GeneratorContext, fd protoreflect.FileDescriptor) (*FileOutput, error) {
	engine, err := sharedTSEngine()
	if err != nil {
		return nil, err
	}
	if shouldIgnore(string(fd.Package())) {
		return &FileOutput{}, nil
	}
	imports := NewImports(string(fd.Package()), getImports(fd), getLookup(fd), ctx.JsDtsEnums)
	var messages []string
	var shimBlocks []string
	extra := map[string]string{}
	enumGen := NewEnumGenerator(fd)
	enums := fd.Enums()
	for i := 0; i < enums.Len(); i++ {
		descriptor := enums.Get(i)
		outputs, err := enumGen.GenerateOutputs(descriptor)
		if err != nil {
			return nil, err
		}
		if outputs.InlineTS != "" {
			messages = append(messages, outputs.InlineTS)
		}
		if enumGen.UsesJsDts(descriptor) {
			shim, err := enumGen.GenerateShim(descriptor)
			if err != nil {
				return nil, err
			}
			if shim != "" {
				shimBlocks = append(shimBlocks, shim)
			}
		}
		enumName := string(descriptor.Name())
		if outputs.DTS != "" {
			extra[enumName+".d.ts"] = wrapEnumSplitFile(fd.Path(), outputs.DTS)
		}
		if outputs.JS != "" {
			extra[enumName+".js"] = wrapEnumSplitFile(fd.Path(), outputs.JS)
		}
	}
	var dynamicImports []string
	msgGen := NewMessageGenerator(imports, fd, ctx.AllDescriptors)
	msgTypes := fd.Messages()
	for i := 0; i < msgTypes.Len(); i++ {
		descriptor := msgTypes.Get(i)
		content, err := msgGen.Generate(descriptor)
		if err != nil {
			return nil, err
		}
		messages = append(messages, content)
		opt := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
		if opt.GetSubType() != "" {
			if _, ok := ctx.SubTypes[string(descriptor.Name())]; ok {
				dynamicImports = append(dynamicImports, string(descriptor.Name()))
			}
		}
	}
	var dynamicImportLines []string
	for _, name := range dynamicImports {
		dynamicImportLines = append(dynamicImportLines, `import("./`+name+`FromAlias");`)
	}
	importLines := imports.ToList()
	var importBlocks []string
	if len(importLines) > 0 {
		importBlocks = append(importBlocks, strings.Join(importLines, "\n"))
	}
	if len(dynamicImportLines) > 0 {
		importBlocks = append(importBlocks, strings.Join(dynamicImportLines, "\n"))
	}
	body := append(shimBlocks, messages...)
	if len(body) == 0 && len(extra) == 0 {
		return &FileOutput{}, nil
	}
	if len(body) == 0 {
		return &FileOutput{MainTS: "", Extra: extra}, nil
	}
	data := map[string]any{
		"filename":   fd.Path(),
		"imports":    strings.Join(importBlocks, "\n"),
		"hasImports": len(importBlocks) > 0,
		"messages":   body,
	}
	content, err := engine.Process("file", data)
	if err != nil {
		return nil, err
	}
	return &FileOutput{
		MainTS: normalizeTsTemplateOutput(content),
		Extra:  extra,
	}, nil
}

func shouldIgnore(packageName string) bool {
	return packageName == "" || strings.Contains(packageName, "google.protobuf")
}

func getImports(fd protoreflect.FileDescriptor) []string {
	seen := map[string]struct{}{}
	var imports []string
	for _, src := range [][]string{
		core.GetWebpbFileOpts(fd, core.HasFileTs).GetTs().GetImport(),
		core.GetFileOpts(fd, core.HasFileTs).GetTs().GetImport(),
	} {
		for _, item := range src {
			if _, ok := seen[item]; ok {
				continue
			}
			seen[item] = struct{}{}
			imports = append(imports, item)
		}
	}
	sort.Strings(imports)
	return imports
}

func getLookup(fd protoreflect.FileDescriptor) []string {
	seen := map[string]struct{}{}
	var lookup []string
	for _, d := range core.ResolveTopLevelTypes(fd) {
		for _, nested := range core.ResolveNestedTypes(d) {
			path := "./" + strings.ReplaceAll(string(nested.FullName()), ".", "/")
			if strings.Contains(path, "google") {
				continue
			}
			if _, ok := seen[path]; ok {
				continue
			}
			seen[path] = struct{}{}
			lookup = append(lookup, path)
		}
	}
	sort.Strings(lookup)
	return lookup
}
