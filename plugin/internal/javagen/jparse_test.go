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
