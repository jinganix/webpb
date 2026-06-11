package testutil

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
)

const (
	javaFormatDir = "plugin/build/golden-format/java"
	tsFormatDir   = "plugin/build/golden-format/ts"
)

// FormatGoldenFiles applies project formatters to generated golden files in-place
// under the staging directory. Keys are relative paths (e.g. "test1/test/Test.java").
func FormatGoldenFiles(lang string, files map[string]string) (map[string]string, error) {
	switch lang {
	case "java":
		return formatJavaGoldenFiles(files)
	case "ts":
		return formatTsGoldenFiles(files)
	default:
		return nil, fmt.Errorf("unsupported golden language %q", lang)
	}
}

func formatJavaGoldenFiles(files map[string]string) (map[string]string, error) {
	root := RepoRoot()
	base := filepath.Join(root, javaFormatDir)
	if err := os.RemoveAll(base); err != nil {
		return nil, err
	}
	written := make(map[string]string, len(files))
	for key, content := range files {
		path := filepath.Join(base, filepath.FromSlash(key))
		if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
			return nil, err
		}
		if err := os.WriteFile(path, []byte(content), 0o644); err != nil {
			return nil, err
		}
		written[key] = path
	}
	gradlew := gradlewPath(root)
	cmd := exec.Command(gradlew, ":plugin:spotlessJavaApply", "--no-daemon", "-q")
	cmd.Dir = root
	if out, err := cmd.CombinedOutput(); err != nil {
		return nil, fmt.Errorf("spotlessJavaApply: %w\n%s", err, out)
	}
	out := make(map[string]string, len(files))
	for key, path := range written {
		data, err := os.ReadFile(path)
		if err != nil {
			return nil, err
		}
		out[key] = NormalizeEOL(string(data))
	}
	return out, nil
}

func formatTsGoldenFiles(files map[string]string) (map[string]string, error) {
	root := RepoRoot()
	base := filepath.Join(root, tsFormatDir)
	if err := os.RemoveAll(base); err != nil {
		return nil, err
	}
	for key, content := range files {
		path := filepath.Join(base, filepath.FromSlash(key))
		if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
			return nil, err
		}
		if err := os.WriteFile(path, []byte(content), 0o644); err != nil {
			return nil, err
		}
	}
	pluginDir := filepath.Join(root, "plugin")
	prettier := filepath.Join(root, "runtime", "ts", "node_modules", ".bin", prettierBin())
	eslint := filepath.Join(root, "runtime", "ts", "node_modules", ".bin", eslintBin())
	prettierConfig := filepath.Join(root, "runtime", "ts", ".prettierrc.js")
	if _, err := os.Stat(prettier); err != nil {
		return nil, fmt.Errorf("prettier not found at %s (run npm ci in runtime/ts)", prettier)
	}
	glob := filepath.Join("build", "golden-format", "ts", "**", "*.ts")
	prettierCmd := exec.Command(prettier, "--write", glob, "--config", prettierConfig)
	prettierCmd.Dir = pluginDir
	if out, err := prettierCmd.CombinedOutput(); err != nil {
		return nil, fmt.Errorf("prettier: %w\n%s", err, out)
	}
	if _, err := os.Stat(eslint); err == nil {
		eslintCmd := exec.Command(eslint, "--fix", "--no-ignore", glob, "--config", "eslint.golden.config.js")
		eslintCmd.Dir = pluginDir
		if out, err := eslintCmd.CombinedOutput(); err != nil {
			return nil, fmt.Errorf("eslint: %w\n%s", err, out)
		}
	}
	out := make(map[string]string, len(files))
	for key := range files {
		path := filepath.Join(base, filepath.FromSlash(key))
		data, err := os.ReadFile(path)
		if err != nil {
			return nil, err
		}
		out[key] = NormalizeEOL(string(data))
	}
	return out, nil
}

func gradlewPath(root string) string {
	name := "gradlew"
	if runtime.GOOS == "windows" {
		name = "gradlew.bat"
	}
	return filepath.Join(root, name)
}

func prettierBin() string {
	if runtime.GOOS == "windows" {
		return "prettier.cmd"
	}
	return "prettier"
}

func eslintBin() string {
	if runtime.GOOS == "windows" {
		return "eslint.cmd"
	}
	return "eslint"
}
