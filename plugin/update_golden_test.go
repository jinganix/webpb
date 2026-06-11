//go:build updategolden

package goplugin_test

import (
	"os"
	"path"
	"path/filepath"
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/javagen"
	"github.com/jinganix/webpb/plugin/internal/testutil"
	"github.com/jinganix/webpb/plugin/internal/tsgen"
)

var updateDumps = []string{
	"alias_skip",
	"auto_alias",
	"core_codegen",
	"enumeration",
	"errors",
	"message_extends",
	"generator_options",
	"imports",
}

func TestUpdateJavaGolden(t *testing.T) {
	generator := javagen.NewGenerator()
	root := filepath.Join(testutil.RepoRoot(), "plugin", "testdata", "java")
	for _, dump := range updateDumps {
		dump := dump
		t.Run(dump, func(t *testing.T) {
			if dump == "errors" {
				t.Skip("errors case is validated by TestJavaGoldenErrors")
			}
			ctx, err := testutil.CreateContext(dump)
			if err != nil {
				t.Fatalf("create context: %v", err)
			}
			generated := map[string]string{}
			for _, fd := range ctx.TargetDescriptors {
				files, err := generator.Generate(fd)
				if err != nil {
					t.Fatalf("generate %s: %v", fd.Path(), err)
				}
				for name, content := range files {
					key := strings.TrimPrefix(name, "/")
					generated[key] = content
				}
			}
			formatted, err := testutil.FormatGoldenFiles("java", generated)
			if err != nil {
				t.Fatalf("format golden: %v", err)
			}
			for key, content := range formatted {
				out := filepath.Join(root, dump, key)
				if err := os.MkdirAll(filepath.Dir(out), 0o755); err != nil {
					t.Fatalf("mkdir: %v", err)
				}
				if err := os.WriteFile(out, []byte(content), 0o644); err != nil {
					t.Fatalf("write %s: %v", out, err)
				}
			}
		})
	}
}

func TestUpdateTSGolden(t *testing.T) {
	generator := tsgen.NewGenerator()
	root := filepath.Join(testutil.RepoRoot(), "plugin", "testdata", "ts")
	for _, dump := range updateDumps {
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
			if dump == "errors" {
				return
			}
			formatted, err := testutil.FormatGoldenFiles("ts", files)
			if err != nil {
				t.Fatalf("format golden: %v", err)
			}
			for key, content := range formatted {
				out := filepath.Join(root, dump, key)
				if err := os.WriteFile(out, []byte(content), 0o644); err != nil {
					t.Fatalf("write %s: %v", out, err)
				}
			}
		})
	}
}
