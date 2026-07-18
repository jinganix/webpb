package javagen

import "testing"

func TestIsValidAnnotationRequest(t *testing.T) {
	tests := []struct {
		name  string
		anno  string
		valid bool
	}{
		{name: "marker", anno: "@Valid", valid: true},
		{name: "with parens", anno: "@Valid()", valid: true},
		{name: "whitespace", anno: " @Valid ", valid: true},
		{name: "other", anno: "@NotNull", valid: false},
	}
	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			if got := isValidAnnotationRequest(tc.anno); got != tc.valid {
				t.Fatalf("isValidAnnotationRequest(%q) = %v, want %v", tc.anno, got, tc.valid)
			}
		})
	}
}
