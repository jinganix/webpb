package main

import (
	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/javagen"
	"github.com/jinganix/webpb/plugin/internal/pluginutil"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

func main() {
	pluginutil.Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		ctx, err := core.NewRequestContext(req)
		if err != nil {
			return nil, err
		}
		generator := javagen.NewGenerator()
		var files []*pluginpb.CodeGeneratorResponse_File
		for _, fd := range ctx.TargetDescriptors {
			generated, err := generator.Generate(fd)
			if err != nil {
				return nil, err
			}
			for name, content := range generated {
				files = append(files, pluginutil.NewFile(name, content))
			}
		}
		return files, nil
	})
}
