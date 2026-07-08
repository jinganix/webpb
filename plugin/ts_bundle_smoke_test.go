package goplugin_test

import (
	"bytes"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
	"strings"
	"testing"
)

func TestBundleSmokeEnumOutput(t *testing.T) {
	if runtime.GOOS == "windows" {
		t.Skip("esbuild smoke test skipped on Windows")
	}
	t.Parallel()

	esbuild, err := resolveEsbuild()
	if err != nil {
		t.Skipf("esbuild not available: %v", err)
	}

	root := filepath.Join("testdata", "ts", "bundle_smoke")

	t.Run("should omit enum member names when by_name is off", func(t *testing.T) {
		t.Parallel()
		out := bundleEntry(t, esbuild, filepath.Join(root, "entry-values-only.ts"))
		assertNoLegacyEnumAlias(t, out)
		assertNoMemberNameStrings(t, out, "a", "b", "c")
	})

	t.Run("should retain member names when by_name is on", func(t *testing.T) {
		t.Parallel()
		out := bundleEntry(t, esbuild, filepath.Join(root, "entry-with-maps.ts"))
		if !strings.Contains(out, "a") {
			t.Fatalf("expected bundled output to contain member name keys when enum_by_name is enabled; got:\n%s", out)
		}
	})
}

func resolveEsbuild() (string, error) {
	if path, err := exec.LookPath("esbuild"); err == nil {
		return path, nil
	}
	cmd := exec.Command("npx", "--yes", "esbuild", "--version")
	if out, err := cmd.CombinedOutput(); err != nil {
		return "", err
	} else if len(bytes.TrimSpace(out)) == 0 {
		return "", exec.ErrNotFound
	}
	return "npx", nil
}

func bundleEntry(t *testing.T, esbuild string, entry string) string {
	t.Helper()

	absEntry, err := filepath.Abs(entry)
	if err != nil {
		t.Fatalf("abs entry: %v", err)
	}

	var cmd *exec.Cmd
	if esbuild == "npx" {
		cmd = exec.Command("npx", "--yes", "esbuild", absEntry, "--bundle", "--minify", "--format=esm")
	} else {
		cmd = exec.Command(esbuild, absEntry, "--bundle", "--minify", "--format=esm")
	}
	cmd.Dir = filepath.Dir(absEntry)
	out, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("esbuild %s: %v\n%s", absEntry, err, out)
	}
	return string(out)
}

var quotedMemberName = regexp.MustCompile(`["']([a-z])["']\s*:`)

func assertNoLegacyEnumAlias(t *testing.T, out string) {
	t.Helper()
	if strings.Contains(out, "EnumFoo") {
		t.Fatalf("bundled output must not contain legacy EnumFoo alias; got:\n%s", out)
	}
}

func assertNoMemberNameStrings(t *testing.T, out string, names ...string) {
	t.Helper()
	for _, name := range names {
		if strings.Contains(out, `"`+name+`"`) || strings.Contains(out, "'"+name+"'") {
			t.Fatalf("bundled output must not contain quoted member name %q when enum_by_name is off; got:\n%s", name, out)
		}
	}
	if quotedMemberName.MatchString(out) {
		t.Fatalf("bundled output must not contain object keys for enum member names when enum_by_name is off; got:\n%s", out)
	}
}
