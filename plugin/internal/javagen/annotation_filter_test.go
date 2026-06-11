package javagen

import (
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/core"
)

func TestAnnotationDistinctFilterAllow(t *testing.T) {
	t.Run("should panic when annotation is invalid", func(t *testing.T) {
		imports := NewImports("test", nil, nil)
		filter := NewAnnotationDistinctFilter(imports, nil)
		defer func() {
			if r := recover(); r == nil {
				t.Fatal("expected panic")
			} else if msg, ok := r.(string); !ok || !strings.Contains(msg, "Bad annotation") {
				t.Fatalf("panic = %v, want Bad annotation", r)
			}
		}()
		filter.Allow("abc")
	})

	t.Run("should allow repeatable annotation every time", func(t *testing.T) {
		imports := testAnnotationImports(t)
		filter := NewAnnotationDistinctFilter(imports, []string{
			"com.fasterxml.jackson.annotation.JsonProperty",
		})
		anno, err := imports.ImportAnnotation(`@JsonProperty("a")`)
		if err != nil {
			t.Fatalf("ImportAnnotation: %v", err)
		}
		if !filter.Allow(anno) {
			t.Fatalf("first Allow(%q) = false, want true", anno)
		}
		if !filter.Allow(anno) {
			t.Fatalf("second Allow(%q) = false, want true for repeatable", anno)
		}
	})

	t.Run("should allow built-in webpb sub value annotation repeatedly", func(t *testing.T) {
		imports := testAnnotationImports(t)
		filter := NewAnnotationDistinctFilter(imports, nil)
		anno := "@" + core.RuntimePackage + ".WebpbSubValue"
		if !filter.Allow(anno) {
			t.Fatalf("first Allow(%q) = false, want true", anno)
		}
		if !filter.Allow(anno) {
			t.Fatalf("second Allow(%q) = false, want true for WebpbSubValue", anno)
		}
	})

	t.Run("should allow first occurrence of non-repeatable annotation", func(t *testing.T) {
		imports := testAnnotationImports(t)
		filter := NewAnnotationDistinctFilter(imports, nil)
		if !filter.Allow("@ToString") {
			t.Fatal("first Allow(@ToString) = false, want true")
		}
	})

	t.Run("should reject duplicate non-repeatable annotation", func(t *testing.T) {
		imports := testAnnotationImports(t)
		filter := NewAnnotationDistinctFilter(imports, nil)
		if !filter.Allow("@ToString") {
			t.Fatal("first Allow(@ToString) = false, want true")
		}
		if filter.Allow("@ToString") {
			t.Fatal("second Allow(@ToString) = true, want false")
		}
	})

	t.Run("should treat annotations with different arguments as duplicates", func(t *testing.T) {
		imports := testAnnotationImports(t)
		filter := NewAnnotationDistinctFilter(imports, nil)
		if !filter.Allow(`@JsonInclude(Include.NON_NULL)`) {
			t.Fatal("first Allow = false, want true")
		}
		if filter.Allow(`@JsonInclude(Include.ALWAYS)`) {
			t.Fatal("second Allow with different args = true, want false")
		}
	})

	t.Run("should dedupe after import resolution", func(t *testing.T) {
		imports := testAnnotationImports(t)
		filter := NewAnnotationDistinctFilter(imports, nil)
		first, err := imports.ImportAnnotation("@JsonInclude(Include.NON_NULL)")
		if err != nil {
			t.Fatalf("ImportAnnotation: %v", err)
		}
		second, err := imports.ImportAnnotation(
			"@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)",
		)
		if err != nil {
			t.Fatalf("ImportAnnotation: %v", err)
		}
		if first != second {
			t.Fatalf("imported forms differ: %q vs %q", first, second)
		}
		if !filter.Allow(first) {
			t.Fatal("first Allow = false, want true")
		}
		if filter.Allow(second) {
			t.Fatal("second Allow = true, want false")
		}
	})

	t.Run("should allow different annotation types", func(t *testing.T) {
		imports := testAnnotationImports(t)
		filter := NewAnnotationDistinctFilter(imports, nil)
		if !filter.Allow("@ToString") {
			t.Fatal("Allow(@ToString) = false, want true")
		}
		if !filter.Allow("@NotNull") {
			t.Fatal("Allow(@NotNull) = false, want true")
		}
		if !filter.Allow("@JsonInclude(Include.NON_NULL)") {
			t.Fatal("Allow(@JsonInclude) = false, want true")
		}
	})
}
