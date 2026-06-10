package goplugin_test

import (
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/javagen"
	"github.com/jinganix/webpb/plugin/internal/testutil"
)

var javaDumps = []string{
	"auto_alias",
	"enumeration",
	"extends_test",
	"import_test",
	"test1",
	"test2",
}

var javaErrorDumps = []string{
	"error_test",
	"test3",
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
			for _, fd := range ctx.TargetDescriptors {
				fd := fd
				files, err := generator.Generate(fd)
				if err != nil {
					t.Fatalf("generate %s: %v", fd.Path(), err)
				}
				for name, content := range files {
					key := strings.TrimPrefix(name, "/")
					expected, err := testutil.ReadExpected("java", dump, key)
					if err != nil {
						t.Fatalf("read expected %s/%s: %v", dump, key, err)
					}
					if content != expected {
						t.Fatalf("mismatch for %s/%s", dump, key)
					}
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
