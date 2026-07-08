package tsgen

import "testing"

func TestEnumHelperPrefix(t *testing.T) {
	t.Parallel()
	tests := map[string]string{
		"ClaimStatus": "claimStatus",
		"ErrorCode":   "errorCode",
		"LogLevel":    "logLevel",
		"":            "",
	}
	for input, want := range tests {
		if got := enumHelperPrefix(input); got != want {
			t.Fatalf("enumHelperPrefix(%q) = %q, want %q", input, got, want)
		}
	}
}
