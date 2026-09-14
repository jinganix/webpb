package goplugin_test

import (
	"os"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/gogen"
	"github.com/jinganix/webpb/plugin/internal/testutil"
)

var goDumps = append(
	append(proto2Dumps(), "proto2_errors"),
	append(proto3Dumps(), "proto3_errors")...,
)

func generateGoFiles(t *testing.T, dump string) map[string]string {
	t.Helper()
	ctx, err := testutil.CreateContext(dump)
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	generator := gogen.NewGenerator()
	options := gogen.Options{}
	files := map[string]string{}
	packageName := ""
	for _, fd := range ctx.TargetDescriptors {
		output, err := generator.Generate(ctx.Descriptors, fd, options)
		if err != nil {
			if shouldExpectError(dump, fd.Path()) {
				continue
			}
			t.Fatalf("generate %s: %v", fd.Path(), err)
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
	return files
}

func TestGoGolden(t *testing.T) {
	for _, dump := range goDumps {
		dump := dump
		t.Run(dump, func(t *testing.T) {
			files := generateGoFiles(t, dump)
			if len(files) == 0 {
				return
			}
			formatted, err := testutil.FormatGoldenFiles("go", files)
			if err != nil {
				t.Fatalf("format golden: %v", err)
			}
			for key, content := range formatted {
				expected, err := testutil.ReadExpected("go", dump, key)
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
