package commons

import "testing"

func TestOf(t *testing.T) {
	t.Parallel()

	t.Run("should return empty group when url is empty", func(t *testing.T) {
		group := Of("")
		if !group.IsEmpty() {
			t.Fatalf("expected empty group")
		}
	})

	t.Run("should parse query-only url when path contains equals", func(t *testing.T) {
		group := Of("foo=bar&baz=qux")
		if group.IsEmpty() {
			t.Fatalf("expected query segments")
		}
		if len(group.QuerySegments()) != 2 {
			t.Fatalf("expected 2 query segments, got %d", len(group.QuerySegments()))
		}
	})

	t.Run("should parse path and query segments when url has both", func(t *testing.T) {
		group := Of("/api/{id}/items?sort=asc")
		if len(group.PathSegments()) != 1 {
			t.Fatalf("expected 1 path segment, got %d", len(group.PathSegments()))
		}
		if group.Suffix() != "/items" {
			t.Fatalf("expected suffix /items, got %q", group.Suffix())
		}
		if len(group.Segments()) != 2 {
			t.Fatalf("expected 2 total segments, got %d", len(group.Segments()))
		}
	})
}
