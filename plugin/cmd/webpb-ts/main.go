package main

import (
	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/pluginutil"
	"github.com/jinganix/webpb/plugin/internal/tsgen"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

func main() {
	pluginutil.Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		ctx, err := core.NewRequestContext(req)
		if err != nil {
			return nil, err
		}
		generator := tsgen.NewGenerator()
		genCtx := tsgen.NewGeneratorContext(ctx.Descriptors, ctx.TargetDescriptors)
		files := map[string]string{}
		for _, fd := range ctx.TargetDescriptors {
			content, err := generator.Generate(genCtx, fd)
			if err != nil {
				return nil, err
			}
			if content != "" {
				files[string(fd.Package())+".ts"] = content
			}
		}
		subFiles, err := (&tsgen.SubTypesGenerator{}).Generate(ctx.TargetDescriptors)
		if err != nil {
			return nil, err
		}
		for k, v := range subFiles {
			files[k] = v
		}
		fromFiles, err := (&tsgen.FromAliasGenerator{}).Generate(genCtx)
		if err != nil {
			return nil, err
		}
		for k, v := range fromFiles {
			files[k] = v
		}
		var out []*pluginpb.CodeGeneratorResponse_File
		for name, content := range files {
			out = append(out, pluginutil.NewFile(name, content))
		}
		return out, nil
	})
}
