package commons

import "testing"

func mustOf(t *testing.T, url string) SegmentGroup {
	t.Helper()
	group, err := Of(url)
	if err != nil {
		t.Fatalf("Of(%q): %v", url, err)
	}
	return group
}

func TestOf(t *testing.T) {
	t.Parallel()

	t.Run("should return empty group when url is empty", func(t *testing.T) {
		group := mustOf(t, "")
		if !group.IsEmpty() {
			t.Fatalf("expected empty group")
		}
	})

	t.Run("should parse query-only url when path contains equals", func(t *testing.T) {
		group := mustOf(t, "foo=bar&baz=qux")
		if group.IsEmpty() {
			t.Fatalf("expected query segments")
		}
		if len(group.QuerySegments()) != 2 {
			t.Fatalf("expected 2 query segments, got %d", len(group.QuerySegments()))
		}
	})

	t.Run("should parse path and query segments when url has both", func(t *testing.T) {
		group := mustOf(t, "/api/{id}/items?sort=asc")
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

	t.Run("should keep dash and dot in query keys", func(t *testing.T) {
		group := mustOf(t, "/api/items?x-key=1&y.key=2")
		segs := group.QuerySegments()
		if len(segs) != 2 {
			t.Fatalf("expected 2 query segments, got %d", len(segs))
		}
		if segs[0].Key() != "x-key" || segs[1].Key() != "y.key" {
			t.Fatalf("unexpected query keys %q %q", segs[0].Key(), segs[1].Key())
		}
	})

	t.Run("should error on unbalanced braces", func(t *testing.T) {
		for _, url := range []string{"/api/{id", "/api/id}", "/api/{a/b}"} {
			if _, err := Of(url); err == nil {
				t.Fatalf("expected error for %q", url)
			}
		}
	})
}
