package goplugin_test

import (
	"os"
	"path"
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/testutil"
	"github.com/jinganix/webpb/plugin/internal/tsgen"
)

var tsDumps = []string{
	"alias_skip",
	"auto_alias",
	"core_codegen",
	"enumeration",
	"errors",
	"message_extends",
	"generator_options",
	"imports",
}

var tsErrorFiles = map[string][]string{
	"errors": {"DuplicatedExtendsFields.proto"},
}

func TestTSGolden(t *testing.T) {
	generator := tsgen.NewGenerator()
	for _, dump := range tsDumps {
		dump := dump
		t.Run(dump, func(t *testing.T) {
			ctx, err := testutil.CreateContext(dump)
			if err != nil {
				t.Fatalf("create context: %v", err)
			}
			genCtx := tsgen.NewGeneratorContext(ctx.Descriptors, ctx.TargetDescriptors)
			files := map[string]string{}
			for _, fd := range ctx.TargetDescriptors {
				content, err := generator.Generate(genCtx, fd)
				if err != nil {
					if shouldExpectError(dump, fd.Path()) {
						continue
					}
					t.Fatalf("generate %s: %v", fd.Path(), err)
				}
				if content != "" {
					files[path.Base(string(fd.Package())+".ts")] = content
				}
			}
			subFiles, err := (&tsgen.SubTypesGenerator{}).Generate(ctx.TargetDescriptors)
			if err != nil {
				t.Fatalf("generate subtypes: %v", err)
			}
			for k, v := range subFiles {
				files[path.Base(k)] = v
			}
			fromFiles, err := (&tsgen.FromAliasGenerator{}).Generate(genCtx)
			if err != nil {
				if dump != "errors" {
					t.Fatalf("generate from alias: %v", err)
				}
			} else {
				for k, v := range fromFiles {
					files[path.Base(k)] = v
				}
			}
			formatted := files
			if dump != "errors" {
				var err error
				formatted, err = testutil.FormatGoldenFiles("ts", files)
				if err != nil {
					t.Fatalf("format golden: %v", err)
				}
			}
			for key, content := range formatted {
				expected, err := testutil.ReadExpected("ts", dump, key)
				if err != nil {
					if os.IsNotExist(err) {
						t.Fatalf("unexpected output without golden %s/%s", dump, key)
					}
					t.Fatalf("read expected %s/%s: %v", dump, key, err)
				}
				if !testutil.GoldenEqual(content, expected) {
					t.Fatalf("mismatch for %s/%s", dump, key)
				}
			}
		})
	}
}

func shouldExpectError(dump, protoPath string) bool {
	for _, name := range tsErrorFiles[dump] {
		if strings.HasSuffix(protoPath, name) {
			return true
		}
	}
	return false
}
