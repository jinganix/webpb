package javagen

import (
	"testing"

	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"github.com/jinganix/webpb/plugin/internal/testutil"
)

func TestEnumGeneratorGenerate(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	generator, err := NewEnumGenerator(ctx.TargetDescriptors[0])
	if err != nil {
		t.Fatalf("NewEnumGenerator: %v", err)
	}
	for i := 0; i < ctx.TargetDescriptors[0].Enums().Len(); i++ {
		enum := ctx.TargetDescriptors[0].Enums().Get(i)
		for _, tmpl := range []string{"enum", "enum.names", "enum.values"} {
			content, err := generator.Generate(enum, tmpl)
			if err != nil {
				t.Fatalf("Generate %s/%s: %v", enum.Name(), tmpl, err)
			}
			if content == "" {
				t.Fatalf("expected content for %s/%s", enum.Name(), tmpl)
			}
		}
	}
}

func TestEnumGeneratorGenerateFromAllGoldenDumps(t *testing.T) {
	for _, dump := range []string{
		"proto2_core_codegen",
		"proto2_enumeration",
		"proto2_message_extends",
		"proto3_core_codegen",
		"proto3_enumeration",
		"proto3_generator_options",
		"proto3_message_extends",
		"proto3_imports",
	} {
		dump := dump
		t.Run(dump, func(t *testing.T) {
			ctx, err := testutil.CreateContext(dump)
			if err != nil {
				t.Fatalf("create context: %v", err)
			}
			for _, fd := range ctx.TargetDescriptors {
				generator, err := NewEnumGenerator(fd)
				if err != nil {
					t.Fatalf("NewEnumGenerator: %v", err)
				}
				for i := 0; i < fd.Enums().Len(); i++ {
					if _, err := generator.Generate(fd.Enums().Get(i), "enum"); err != nil {
						t.Fatalf("Generate: %v", err)
					}
				}
			}
		})
	}
}

func TestEnumGeneratorGetImplements(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	generator, err := NewEnumGenerator(ctx.TargetDescriptors[0])
	if err != nil {
		t.Fatalf("NewEnumGenerator: %v", err)
	}
	for i := 0; i < ctx.TargetDescriptors[0].Enums().Len(); i++ {
		enum := ctx.TargetDescriptors[0].Enums().Get(i)
		if enum.Name() != "Test5" {
			continue
		}
		for _, tmpl := range []string{"enum", "enum.names", "enum.values"} {
			if _, err := generator.Generate(enum, tmpl); err != nil {
				t.Fatalf("Generate %s: %v", tmpl, err)
			}
		}
		return
	}
	t.Fatal("expected Test5 enum")
}

func TestEnumGeneratorGenerateImplementsTemplate(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	generator, err := NewEnumGenerator(ctx.TargetDescriptors[0])
	if err != nil {
		t.Fatalf("NewEnumGenerator: %v", err)
	}
	for i := 0; i < ctx.TargetDescriptors[0].Enums().Len(); i++ {
		enum := ctx.TargetDescriptors[0].Enums().Get(i)
		if enum.Name() != "Test5" {
			continue
		}
		if _, err := generator.Generate(enum, "enum.values"); err != nil {
			t.Fatalf("Generate enum.values: %v", err)
		}
		return
	}
	t.Fatal("expected Test5 enum")
}

func TestGetAnnotationsReturnsErrorForInvalidAnnotation(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	lookup, err := GetLookup(ctx.TargetDescriptors[0])
	if err != nil {
		t.Fatalf("GetLookup: %v", err)
	}
	generator := &EnumGenerator{
		fileDescriptor: ctx.TargetDescriptors[0],
		imports:        NewImports(GetJavaPackage(ctx.TargetDescriptors[0]), lookup, ctx.TargetDescriptors[0]),
		webpbOpts:      &webpb.JavaFileOpts{Annotation: []string{"@Bad(class..name)"}},
		fileOpts:       &webpb.JavaFileOpts{},
	}
	enum := ctx.TargetDescriptors[0].Enums().Get(0)
	if _, err := generator.getAnnotations(enum); err == nil {
		t.Fatal("expected annotation import error")
	}
	if _, err := generator.Generate(enum, "enum"); err == nil {
		t.Fatal("expected generate error for invalid annotation")
	}
}

func TestNewEnumGeneratorReturnsErrorWhenLookupFails(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_errors")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	for _, fd := range ctx.Descriptors {
		if _, err := NewEnumGenerator(fd); err != nil {
			return
		}
	}
	t.Fatal("expected lookup error for bad java import")
}

func TestGetEnumValueUsesStringNameWhenValueMissing(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	generator, err := NewEnumGenerator(ctx.TargetDescriptors[0])
	if err != nil {
		t.Fatalf("NewEnumGenerator: %v", err)
	}
	for i := 0; i < ctx.TargetDescriptors[0].Enums().Len(); i++ {
		enum := ctx.TargetDescriptors[0].Enums().Get(i)
		if enum.Name() != "Test3" {
			continue
		}
		value := generator.getEnumValue(enum, enum.Values().Get(0))
		if value != `"test3_1"` {
			t.Fatalf("getEnumValue = %q, want %q", value, `"test3_1"`)
		}
		return
	}
	t.Fatal("expected Test3 enum")
}

func TestGetEnumValueUsesExplicitOptValue(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	generator, err := NewEnumGenerator(ctx.TargetDescriptors[0])
	if err != nil {
		t.Fatalf("NewEnumGenerator: %v", err)
	}
	for i := 0; i < ctx.TargetDescriptors[0].Enums().Len(); i++ {
		enum := ctx.TargetDescriptors[0].Enums().Get(i)
		if enum.Name() != "Test5" {
			continue
		}
		value := generator.getEnumValue(enum, enum.Values().Get(0))
		if value != `"text1"` {
			t.Fatalf("getEnumValue = %q, want %q", value, `"text1"`)
		}
		return
	}
	t.Fatal("expected Test5 enum")
}

func TestEnumGeneratorReturnsErrorForUnknownTemplate(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	generator, err := NewEnumGenerator(ctx.TargetDescriptors[0])
	if err != nil {
		t.Fatalf("NewEnumGenerator: %v", err)
	}
	enum := ctx.TargetDescriptors[0].Enums().Get(0)
	if _, err := generator.Generate(enum, "missing-template"); err == nil {
		t.Fatal("expected error for unknown template")
	}
}

func TestEnumGeneratorGenerateFromEnumerationDump(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_enumeration")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	for _, fd := range ctx.TargetDescriptors {
		generator, err := NewEnumGenerator(fd)
		if err != nil {
			t.Fatalf("NewEnumGenerator: %v", err)
		}
		for i := 0; i < fd.Enums().Len(); i++ {
			enum := fd.Enums().Get(i)
			if _, err := generator.Generate(enum, "enum"); err != nil {
				t.Fatalf("Generate %s: %v", enum.Name(), err)
			}
		}
	}
}
