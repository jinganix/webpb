package javagen

import "testing"

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
