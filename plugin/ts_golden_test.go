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
	"auto_alias",
	"enumeration",
	"error_test",
	"extends_test",
	"import_test",
	"test1",
	"test2",
	"test3",
}

var tsErrorFiles = map[string][]string{
	"error_test": {"DuplicatedFieldsError.proto"},
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
					files[string(fd.Package())+".ts"] = content
				}
			}
			subFiles, err := (&tsgen.SubTypesGenerator{}).Generate(ctx.TargetDescriptors)
			if err != nil {
				t.Fatalf("generate subtypes: %v", err)
			}
			for k, v := range subFiles {
				files[k] = v
			}
			fromFiles, err := (&tsgen.FromAliasGenerator{}).Generate(genCtx)
			if err != nil {
				if dump != "error_test" {
					t.Fatalf("generate from alias: %v", err)
				}
			} else {
				for k, v := range fromFiles {
					files[k] = v
				}
			}
			for name, content := range files {
				key := path.Base(name)
				expected, err := testutil.ReadExpected("ts", dump, key)
				if err != nil {
					if os.IsNotExist(err) {
						t.Fatalf("unexpected output without golden %s/%s", dump, key)
					}
					t.Fatalf("read expected %s/%s: %v", dump, key, err)
				}
				if content != expected {
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
