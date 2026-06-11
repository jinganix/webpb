package core

import (
	"strings"
	"testing"
)

func TestNormalize(t *testing.T) {
	t.Parallel()
	cases := map[string]string{
		"":                    "",
		"/foo/":               "/foo",
		"foo/bar/":            "/foo/bar",
		"http://example.com/": "http://example.com",
	}
	for input, want := range cases {
		if got := Normalize(input); got != want {
			t.Fatalf("Normalize(%q) = %q, want %q", input, got, want)
		}
	}
}

func TestCapitalize(t *testing.T) {
	t.Parallel()
	if Capitalize("") != "" {
		t.Fatal("expected empty string")
	}
	if Capitalize("foo") != "Foo" {
		t.Fatalf("expected Foo, got %q", Capitalize("foo"))
	}
}

func TestToBase52(t *testing.T) {
	t.Parallel()
	if ToBase52(0) == "" {
		t.Fatal("expected non-empty base52 for zero")
	}
	if ToBase52(52) == ToBase52(0) {
		t.Fatal("expected different values for different inputs")
	}
}

func TestLimitNewlineAndAlignNewline(t *testing.T) {
	t.Parallel()
	var builder strings.Builder
	builder.WriteString("line\n\n\n")
	LimitNewline(&builder, 1)
	if strings.Count(builder.String(), "\n") > 1 {
		t.Fatalf("expected at most one trailing newline after limit, got %q", builder.String())
	}

	builder.Reset()
	builder.WriteString("text")
	AlignNewline(&builder, 2)
	if !strings.HasSuffix(builder.String(), "\n\n") {
		t.Fatalf("expected two trailing newlines, got %q", builder.String())
	}
}
