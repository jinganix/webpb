package main

import (
	"strings"

	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/gogen"
	"github.com/jinganix/webpb/plugin/internal/pluginutil"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

func parseOptions(parameter string) gogen.Options {
	options := gogen.Options{}
	for _, part := range strings.Split(parameter, ",") {
		kv := strings.SplitN(part, "=", 2)
		if len(kv) != 2 {
			continue
		}
		switch kv[0] {
		case "package":
			options.Package = kv[1]
		case "module":
			options.Module = kv[1]
		case "auto_alias":
			options.AutoAlias = kv[1] == "true" || kv[1] == "1"
		case "enum_auto_alias":
			options.EnumAutoAlias = kv[1] == "true" || kv[1] == "1"
		case "int64_as_string":
			options.Int64AsString = kv[1] == "true" || kv[1] == "1"
		}
	}
	return options
}

func main() {
	pluginutil.Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		ctx, err := core.NewRequestContext(req)
		if err != nil {
			return nil, err
		}
		options := parseOptions(req.GetParameter())
		generator := gogen.NewGenerator()
		files := map[string]string{}
		packageName := ""
		for _, fd := range ctx.TargetDescriptors {
			output, err := generator.Generate(ctx.Descriptors, fd, options)
			if err != nil {
				return nil, err
			}
			if packageName == "" {
				packageName = generator.PackageFor(fd, options)
			}
			if output.Content != "" {
				files[output.Name] = output.Content
			}
		}
		if len(files) > 0 && packageName != "" {
			helpers := generator.HelpersFile(packageName)
			files[helpers.Name] = helpers.Content
		}
		var out []*pluginpb.CodeGeneratorResponse_File
		for name, content := range files {
			out = append(out, pluginutil.NewFile(name, content))
		}
		return out, nil
	})
}
