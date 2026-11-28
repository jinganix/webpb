package goplugin_test

import (
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/javagen"
	"github.com/jinganix/webpb/plugin/internal/testutil"
)

var javaDumps = append(proto2Dumps(), proto3Dumps()...)

var javaErrorDumps = []string{
	"proto2_errors",
	"proto3_errors",
}

func TestJavaGolden(t *testing.T) {
	generator := javagen.NewGenerator()
	for _, dump := range javaDumps {
		dump := dump
		t.Run(dump, func(t *testing.T) {
			ctx, err := testutil.CreateContext(dump)
			if err != nil {
				t.Fatalf("create context: %v", err)
			}
			generated := map[string]string{}
			for _, fd := range ctx.TargetDescriptors {
				files, err := generator.Generate(fd, ctx.Descriptors)
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
				expected, err := testutil.ReadExpected("java", dump, key)
				if err != nil {
					t.Fatalf("read expected %s/%s: %v", dump, key, err)
				}
				if !testutil.GoldenEqual(content, expected) {
					t.Fatalf("mismatch for %s/%s", dump, key)
				}
			}
			root := filepath.Join(testutil.RepoRoot(), "plugin", "testdata", "java", dump)
			var expectedKeys []string
			err = filepath.WalkDir(root, func(path string, d os.DirEntry, err error) error {
				if err != nil {
					return err
				}
				if d.IsDir() {
					return nil
				}
				rel, err := filepath.Rel(root, path)
				if err != nil {
					return err
				}
				expectedKeys = append(expectedKeys, filepath.ToSlash(rel))
				return nil
			})
			if err != nil {
				t.Fatalf("list expected %s: %v", dump, err)
			}
			for _, key := range expectedKeys {
				if _, ok := formatted[key]; !ok {
					t.Fatalf("orphan golden without generator output %s/%s", dump, key)
				}
			}
		})
	}
}

func TestJavaGoldenErrors(t *testing.T) {
	generator := javagen.NewGenerator()
	for _, dump := range javaErrorDumps {
		dump := dump
		t.Run(dump, func(t *testing.T) {
			ctx, err := testutil.CreateContext(dump)
			if err != nil {
				t.Fatalf("create context: %v", err)
			}
			for _, fd := range ctx.TargetDescriptors {
				_, err := generator.Generate(fd, ctx.Descriptors)
				if err == nil {
					t.Fatalf("expected error for %s", fd.Path())
				}
			}
		})
	}
}
