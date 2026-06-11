package javagen

import (
	"sort"
	"strings"
	"testing"
)

func testAnnotationImports(t *testing.T) *Imports {
	t.Helper()
	paths := []string{
		"com.fasterxml.jackson.annotation.JsonAlias",
		"com.fasterxml.jackson.annotation.JsonIgnoreProperties",
		"com.fasterxml.jackson.annotation.JsonInclude",
		"com.fasterxml.jackson.annotation.JsonInclude.Include",
		"com.fasterxml.jackson.annotation.JsonProperty",
		"com.fasterxml.jackson.databind.annotation.JsonDeserialize",
		"com.fasterxml.jackson.databind.annotation.JsonSerialize",
		"com.fasterxml.jackson.databind.ser.std.ToStringSerializer",
		"io.github.jinganix.webpb.runtime.common.InQuery",
		"io.github.jinganix.webpb.runtime.enumeration.EnumerationDeserializer",
		"io.github.jinganix.webpb.runtime.enumeration.EnumerationSerializer",
		"io.github.jinganix.webpb.tests.Const",
		"jakarta.validation.constraints.NotNull",
		"java.util.regex.Pattern",
		"lombok.ToString",
	}
	lookup, err := newImportPaths(paths)
	if err != nil {
		t.Fatalf("newImportPaths: %v", err)
	}
	return NewImports("test", lookup, nil)
}

func newImportPaths(paths []string) ([]ImportPath, error) {
	var lookup []ImportPath
	for _, path := range paths {
		importPath, err := NewImportPath(path)
		if err != nil {
			return nil, err
		}
		lookup = append(lookup, importPath)
	}
	sort.Slice(lookup, func(i, j int) bool {
		return len(lookup[i].path) > len(lookup[j].path)
	})
	return lookup, nil
}

func TestParseAnnotation(t *testing.T) {
	imports := testAnnotationImports(t)
	parser := &annotationParser{imports: imports}

	tests := []struct {
		name  string
		input string
		want  string
	}{
		{
			name:  "marker annotation",
			input: "@ToString",
			want:  "@ToString",
		},
		{
			name:  "marker annotation with empty parens",
			input: "@NotNull()",
			want:  "@NotNull()",
		},
		{
			name:  "string literal argument",
			input: `@JsonProperty("fieldName")`,
			want:  `@JsonProperty("fieldName")`,
		},
		{
			name:  "boolean named argument",
			input: "@JsonIgnoreProperties(ignoreUnknown = true)",
			want:  "@JsonIgnoreProperties(ignoreUnknown = true)",
		},
		{
			name:  "fully qualified enum constant",
			input: "@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)",
			want:  "@JsonInclude(Include.NON_NULL)",
		},
		{
			name:  "short enum constant",
			input: "@JsonInclude(Include.NON_NULL)",
			want:  "@JsonInclude(Include.NON_NULL)",
		},
		{
			name:  "named enum constant",
			input: "@JsonInclude(value = com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)",
			want:  "@JsonInclude(value = Include.NON_NULL)",
		},
		{
			name:  "class literal serializer",
			input: "@JsonSerialize(using = ToStringSerializer.class)",
			want:  "@JsonSerialize(using = ToStringSerializer.class)",
		},
		{
			name:  "class literal deserializer",
			input: "@JsonDeserialize(using = EnumerationDeserializer.class)",
			want:  "@JsonDeserialize(using = EnumerationDeserializer.class)",
		},
		{
			name:  "constant reference",
			input: "@Pattern(regexp = Const.REGEX)",
			want:  "@Pattern(regexp = Const.REGEX)",
		},
		{
			name:  "regexp with comma in quoted string",
			input: `@Pattern(regexp = "^[a-z][a-z0-9]{2,31}$")`,
			want:  `@Pattern(regexp = "^[a-z][a-z0-9]{2,31}$")`,
		},
		{
			name:  "multiple named arguments",
			input: `@JsonIgnoreProperties(ignoreUnknown = true, value = "ignored")`,
			want:  `@JsonIgnoreProperties(ignoreUnknown = true, value = "ignored")`,
		},
		{
			name:  "runtime in query annotation",
			input: "@io.github.jinganix.webpb.runtime.common.InQuery",
			want:  "@InQuery",
		},
		{
			name:  "alias template replacement style",
			input: `@JsonProperty("aliasTest1")`,
			want:  `@JsonProperty("aliasTest1")`,
		},
		{
			name:  "json alias field name",
			input: `@JsonAlias("test1")`,
			want:  `@JsonAlias("test1")`,
		},
		{
			name:  "custom property name",
			input: `@JsonProperty("other")`,
			want:  `@JsonProperty("other")`,
		},
		{
			name:  "single quoted string argument",
			input: `@JsonProperty('fieldName')`,
			want:  `@JsonProperty('fieldName')`,
		},
		{
			name:  "escaped quote in string",
			input: `@JsonProperty("a\"b")`,
			want:  `@JsonProperty("a\"b")`,
		},
		{
			name:  "nested parentheses in value",
			input: `@JsonProperty(value = "a(b,c)d")`,
			want:  `@JsonProperty(value = "a(b,c)d")`,
		},
		{
			name:  "simple regexp",
			input: `@Pattern(regexp = "^[a-z]+$")`,
			want:  `@Pattern(regexp = "^[a-z]+$")`,
		},
		{
			name:  "notnull marker",
			input: "@NotNull",
			want:  "@NotNull",
		},
		{
			name:  "json ignore properties from file opts",
			input: "@JsonIgnoreProperties(ignoreUnknown = true)",
			want:  "@JsonIgnoreProperties(ignoreUnknown = true)",
		},
		{
			name:  "json serialize from include enum",
			input: "@JsonSerialize(using = EnumerationSerializer.class)",
			want:  "@JsonSerialize(using = EnumerationSerializer.class)",
		},
		{
			name:  "json deserialize from include enum",
			input: "@JsonDeserialize(using = EnumerationDeserializer.class)",
			want:  "@JsonDeserialize(using = EnumerationDeserializer.class)",
		},
		{
			name:  "whitespace around annotation",
			input: "  @ToString  ",
			want:  "@ToString",
		},
		{
			name:  "whitespace inside parens",
			input: "@NotNull( )",
			want:  "@NotNull()",
		},
		{
			name:  "whitespace around named argument",
			input: "@JsonIgnoreProperties( ignoreUnknown = true )",
			want:  "@JsonIgnoreProperties(ignoreUnknown = true)",
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			got, err := parser.parseAnnotation(tc.input)
			if err != nil {
				t.Fatalf("parseAnnotation(%q): %v", tc.input, err)
			}
			if got != tc.want {
				t.Fatalf("parseAnnotation(%q) = %q, want %q", tc.input, got, tc.want)
			}
		})
	}
}

func TestParseAnnotationErrors(t *testing.T) {
	imports := testAnnotationImports(t)
	parser := &annotationParser{imports: imports}

	tests := []struct {
		name  string
		input string
	}{
		{name: "missing at sign", input: "BadAnnotation"},
		{name: "missing at sign with parens", input: "NotNull()"},
		{name: "unclosed paren", input: "@NotNull("},
		{name: "missing closing paren", input: "@JsonProperty(\"a\""},
		{name: "malformed pair", input: "@JsonProperty(, bar=1)"},
		{name: "unknown import", input: "@Unknown.Type(value = 1)"},
		{name: "empty input", input: ""},
		{name: "only at sign", input: "@"},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			_, err := parser.parseAnnotation(tc.input)
			if err == nil {
				t.Fatalf("parseAnnotation(%q) expected error", tc.input)
			}
			if !strings.Contains(err.Error(), "Bad annotation") && !strings.Contains(err.Error(), "No import path found") {
				t.Fatalf("parseAnnotation(%q) error = %v, want Bad annotation or No import path found", tc.input, err)
			}
		})
	}
}

func TestParseAnnotationSignupFieldAnnotations(t *testing.T) {
	imports := testAnnotationImports(t)
	parser := &annotationParser{imports: imports}

	notNull, err := parser.parseAnnotation("@NotNull")
	if err != nil {
		t.Fatalf("parse @NotNull: %v", err)
	}
	pattern, err := parser.parseAnnotation(`@Pattern(regexp = "^[a-z][a-z0-9]{2,31}$")`)
	if err != nil {
		t.Fatalf("parse @Pattern: %v", err)
	}
	if notNull != "@NotNull" {
		t.Fatalf("@NotNull = %q", notNull)
	}
	if !strings.Contains(pattern, "{2,31}") {
		t.Fatalf("@Pattern = %q, want regexp quantifier preserved", pattern)
	}
}

func TestParseAnnotationUnsupportedArrayInitializer(t *testing.T) {
	imports := testAnnotationImports(t)
	parser := &annotationParser{imports: imports}
	_, err := parser.parseAnnotation(`@JsonIgnoreProperties(value = {"a", "b"})`)
	if err == nil {
		t.Fatal("expected error for array initializer with comma")
	}
}

func TestImportAnnotation(t *testing.T) {
	imports := testAnnotationImports(t)

	got, err := imports.ImportAnnotation("@Pattern(regexp = \"^[a-z][a-z0-9]{2,31}$\")")
	if err != nil {
		t.Fatalf("ImportAnnotation: %v", err)
	}
	if !strings.Contains(got, "{2,31}") {
		t.Fatalf("ImportAnnotation = %q, want regexp quantifier preserved", got)
	}

	list := imports.ToList()
	if len(list) == 0 {
		t.Fatal("expected imports to be recorded")
	}
}

func TestSplitAnnotationPairs(t *testing.T) {
	tests := []struct {
		name  string
		input string
		want  []string
	}{
		{
			name:  "single pair",
			input: "ignoreUnknown = true",
			want:  []string{"ignoreUnknown = true"},
		},
		{
			name:  "comma inside quoted string",
			input: `regexp = "^[a-z][a-z0-9]{2,31}$"`,
			want:  []string{`regexp = "^[a-z][a-z0-9]{2,31}$"`},
		},
		{
			name:  "multiple pairs",
			input: `ignoreUnknown = true, regexp = "^[a-z][a-z0-9]{2,31}$"`,
			want: []string{
				"ignoreUnknown = true",
				`regexp = "^[a-z][a-z0-9]{2,31}$"`,
			},
		},
		{
			name:  "comma inside nested parens",
			input: `value = foo(a, b), other = 1`,
			want: []string{
				"value = foo(a, b)",
				"other = 1",
			},
		},
		{
			name:  "comma inside single quoted string",
			input: `value = 'a,b', other = 2`,
			want: []string{
				"value = 'a,b'",
				"other = 2",
			},
		},
		{
			name:  "escaped quote in string",
			input: `value = "a\"b", other = 3`,
			want: []string{
				`value = "a\"b"`,
				"other = 3",
			},
		},
		{
			name:  "deeply nested parens",
			input: `value = foo(bar(baz, qux)), other = 4`,
			want: []string{
				"value = foo(bar(baz, qux))",
				"other = 4",
			},
		},
		{
			name:  "signup regexp pair only",
			input: `regexp = "^[a-z][a-z0-9]{2,31}$"`,
			want:  []string{`regexp = "^[a-z][a-z0-9]{2,31}$"`},
		},
		{
			name:  "three pairs",
			input: `a = 1, b = 2, c = 3`,
			want:  []string{"a = 1", "b = 2", "c = 3"},
		},
		{
			name:  "comma after closing paren",
			input: `value = foo(), other = 1`,
			want:  []string{"value = foo()", "other = 1"},
		},
		{
			name:  "double quoted string with single comma",
			input: `value = "a,b"`,
			want:  []string{`value = "a,b"`},
		},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			got := splitAnnotationPairs(tc.input)
			if len(got) != len(tc.want) {
				t.Fatalf("splitAnnotationPairs(%q) = %#v, want %#v", tc.input, got, tc.want)
			}
			for i := range tc.want {
				if got[i] != tc.want[i] {
					t.Fatalf("splitAnnotationPairs(%q)[%d] = %q, want %q", tc.input, i, got[i], tc.want[i])
				}
			}
		})
	}
}

func TestParseAnnotationSimpleName(t *testing.T) {
	tests := []struct {
		name  string
		input string
		want  string
	}{
		{name: "marker", input: "@ToString", want: "ToString"},
		{name: "with parens", input: "@NotNull()", want: "NotNull"},
		{name: "with arguments", input: `@JsonProperty("x")`, want: "JsonProperty"},
		{name: "qualified name", input: "@com.example.Foo(bar)", want: "com.example.Foo"},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			got, err := parseAnnotationSimpleName(tc.input)
			if err != nil {
				t.Fatalf("parseAnnotationSimpleName(%q): %v", tc.input, err)
			}
			if got != tc.want {
				t.Fatalf("parseAnnotationSimpleName(%q) = %q, want %q", tc.input, got, tc.want)
			}
		})
	}
}

func TestParseAnnotationSimpleNameErrors(t *testing.T) {
	for _, input := range []string{"", "NotNull", "NotNull()"} {
		if _, err := parseAnnotationSimpleName(input); err == nil {
			t.Fatalf("parseAnnotationSimpleName(%q) expected error", input)
		}
	}
}

func TestParseAnnotationName(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		wantName string
		wantRest string
	}{
		{name: "marker", input: "ToString", wantName: "ToString", wantRest: ""},
		{name: "with empty parens", input: "NotNull()", wantName: "NotNull", wantRest: "()"},
		{name: "with args", input: `JsonProperty("x")`, wantName: "JsonProperty", wantRest: `("x")`},
	}

	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			gotName, gotRest, err := parseAnnotationName(tc.input)
			if err != nil {
				t.Fatalf("parseAnnotationName(%q): %v", tc.input, err)
			}
			if gotName != tc.wantName || gotRest != tc.wantRest {
				t.Fatalf("parseAnnotationName(%q) = (%q, %q), want (%q, %q)",
					tc.input, gotName, gotRest, tc.wantName, tc.wantRest)
			}
		})
	}
}
