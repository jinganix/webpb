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

func TestImportEnumTypeCrossPackage(t *testing.T) {
	t.Parallel()
	imports := NewImports("TestProto", nil, []string{"./IncludeProto/Enum"})
	ref := imports.ImportEnumType("IncludeProto.Enum")
	if ref != "Enum" {
		t.Fatalf("got %q, want Enum", ref)
	}
	got := imports.ToList()
	if len(got) != 1 || got[0] != `import type { Enum } from "./IncludeProto";` {
		t.Fatalf("unexpected imports: %v", got)
	}
}

func TestImportEnumTypeSamePackage(t *testing.T) {
	t.Parallel()
	imports := NewImports("RepeatedEnumProto", nil, nil)
	ref := imports.ImportEnumType("RepeatedEnumProto.Status")
	if ref != "Status" {
		t.Fatalf("got %q, want Status", ref)
	}
	if len(imports.ToList()) != 0 {
		t.Fatalf("unexpected imports: %v", imports.ToList())
	}
}
