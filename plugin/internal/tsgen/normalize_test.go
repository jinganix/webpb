package tsgen

import (
	"regexp"
	"testing"
)

func TestNormalizeTsNamespaceClose(t *testing.T) {
	in := "foo\n  }\n\n}\n"
	want := "foo\n  }\n}\n"
	got := regexp.MustCompile(`(?m)(\n  \}\n)\n+(\})`).ReplaceAllString(in, "$1$2")
	if got != want {
		t.Fatalf("got %q want %q", got, want)
	}
}

func TestNormalizeTsInterfaceBlocks(t *testing.T) {
	in := "export interface IMapValueTestPb {\n\n  \n  sort: Record<string, Direction>;\n  \n\n}\n"
	want := "export interface IMapValueTestPb {\n  sort: Record<string, Direction>;\n}\n"
	got := normalizeTsInterfaceBlocks(in)
	if got != want {
		t.Fatalf("got %q want %q", got, want)
	}
}
