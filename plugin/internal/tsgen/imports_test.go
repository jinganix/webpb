package tsgen

import "testing"

func TestSplitTypeName(t *testing.T) {
	t.Parallel()
	got := splitTypeName("....BadClassOrInterface")
	want := []string{"BadClassOrInterface"}
	if len(got) != len(want) || got[0] != want[0] {
		t.Fatalf("splitTypeName(%q) = %v, want %v", "....BadClassOrInterface", got, want)
	}
}

func TestImportTypeIgnoresInvalidExtends(t *testing.T) {
	t.Parallel()
	imports := NewEmptyImports()
	ref := imports.ImportType("....BadClassOrInterface")
	if ref != "....BadClassOrInterface" {
		t.Fatalf("got %q", ref)
	}
	if len(imports.ToList()) != 0 {
		t.Fatalf("unexpected imports: %v", imports.ToList())
	}
}
