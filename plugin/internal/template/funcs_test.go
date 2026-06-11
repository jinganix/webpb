package template

import (
	"reflect"
	"testing"
)

func TestHasContent(t *testing.T) {
	t.Parallel()
	cases := []struct {
		value any
		want  bool
	}{
		{nil, false},
		{"", false},
		{"x", true},
		{[]string{}, false},
		{[]string{"a"}, true},
		{[]any{}, false},
		{[]any{1}, true},
		{map[string]any{}, false},
		{map[string]any{"a": 1}, true},
		{map[string]string{}, false},
		{map[string]string{"a": "b"}, true},
		{[]map[string]any{}, false},
		{[]map[string]string{}, false},
		{42, true},
	}
	for _, tc := range cases {
		if got := hasContent(tc.value); got != tc.want {
			t.Fatalf("hasContent(%#v) = %v, want %v", tc.value, got, tc.want)
		}
	}
}

func TestTemplateFuncs(t *testing.T) {
	t.Parallel()
	funcs := Funcs()
	seqContains := funcs["seqContains"].(func(any, any) bool)
	if !seqContains([]string{"a", "b"}, "b") {
		t.Fatal("expected seqContains true")
	}
	if seqContains("not-a-slice", "a") {
		t.Fatal("expected seqContains false for non-slice")
	}

	then := funcs["then"].(func(bool, any, any) any)
	if then(true, "a", "b") != "a" || then(false, "a", "b") != "b" {
		t.Fatal("unexpected then result")
	}

	keepAfter := funcs["keepAfter"].(func(string, string) string)
	if keepAfter("a/b/c", "/") != "c" {
		t.Fatalf("keepAfter = %q", keepAfter("a/b/c", "/"))
	}

	keepBefore := funcs["keepBefore"].(func(string, string) string)
	if keepBefore("a/b", "/") != "a" {
		t.Fatalf("keepBefore = %q", keepBefore("a/b", "/"))
	}

	keys := funcs["keys"].(func(map[string]string) []string)
	if got := keys(map[string]string{"b": "2", "a": "1"}); !reflect.DeepEqual(got, []string{"a", "b"}) {
		t.Fatalf("keys = %v", got)
	}

	get := funcs["get"].(func(map[string]any, string) any)
	if get(nil, "k") != nil || get(map[string]any{"k": 1}, "k") != 1 {
		t.Fatal("unexpected get result")
	}

	last := funcs["last"].(func(int, any) bool)
	if !last(1, []int{0, 1}) || last(0, []int{0, 1}) {
		t.Fatal("unexpected last result")
	}
}
