package javagen

import (
	"strings"
	"testing"
)

func TestSplitAnnotationPairsIgnoresCommaInQuotedString(t *testing.T) {
	pairs := splitAnnotationPairs(`regexp = "^[a-z][a-z0-9]{2,31}$"`)
	if len(pairs) != 1 {
		t.Fatalf("expected 1 pair, got %d: %#v", len(pairs), pairs)
	}
	if !strings.Contains(pairs[0], `{2,31}`) {
		t.Fatalf("expected regexp quantifier in %q", pairs[0])
	}

	multi := splitAnnotationPairs(`ignoreUnknown = true, regexp = "^[a-z][a-z0-9]{2,31}$"`)
	if len(multi) != 2 {
		t.Fatalf("expected 2 pairs, got %d: %#v", len(multi), multi)
	}
}

func TestParseClassOrInterfaceType(t *testing.T) {
	lookup, err := GetLookup(nil)
	if err != nil {
		t.Fatalf("lookup: %v", err)
	}
	imports := NewImports("test", lookup, nil)
	parser := &typeParser{imports: imports}
	for _, input := range []string{"Integer", "List<String>", "Enumeration<Integer>", "Map<String, List<Integer>>"} {
		if _, err := parser.parseClassOrInterfaceType(input); err != nil {
			t.Fatalf("parse %q: %v", input, err)
		}
	}
}

func TestParseClassOrInterfaceTypeErrors(t *testing.T) {
	lookup, err := GetLookup(nil)
	if err != nil {
		t.Fatalf("lookup: %v", err)
	}
	parser := &typeParser{imports: NewImports("test", lookup, nil)}
	for _, input := range []string{"", "List<String", "Map<>"} {
		if _, err := parser.parseClassOrInterfaceType(input); err == nil {
			t.Fatalf("expected error for %q", input)
		}
	}
}

func TestSplitGenericArgsErrors(t *testing.T) {
	if _, err := splitGenericArgs(""); err == nil {
		t.Fatal("expected error for empty generic args")
	}
	if _, err := splitGenericArgs("A<B>>"); err == nil {
		t.Fatal("expected error for unbalanced generics")
	}
}
