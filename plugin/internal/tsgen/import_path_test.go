package tsgen

import "testing"

func TestImportPathAccessors(t *testing.T) {
	t.Parallel()
	path := NewImportPathOrdered("alias", "./foo", 3)
	if path.Name() != "alias" {
		t.Fatalf("Name() = %q", path.Name())
	}
	if path.Path() != "./foo" {
		t.Fatalf("Path() = %q", path.Path())
	}
	if path.Order() != 3 {
		t.Fatalf("Order() = %d", path.Order())
	}
	if NewImportPath("a", "b").Order() != 0 {
		t.Fatal("expected default order 0")
	}
}
