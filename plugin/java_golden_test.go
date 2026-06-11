package goplugin_test

import (
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/javagen"
	"github.com/jinganix/webpb/plugin/internal/testutil"
)

var javaDumps = []string{
	"alias_skip",
	"auto_alias",
	"core_codegen",
	"enumeration",
	"message_extends",
	"generator_options",
	"imports",
}

var javaErrorDumps = []string{
	"errors",
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
				expected, err := testutil.ReadExpected("java", dump, key)
				if err != nil {
					t.Fatalf("read expected %s/%s: %v", dump, key, err)
				}
				if !testutil.GoldenEqual(content, expected) {
					t.Fatalf("mismatch for %s/%s", dump, key)
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
				_, err := generator.Generate(fd)
				if err == nil {
					t.Fatalf("expected error for %s", fd.Path())
				}
			}
		})
	}
}
