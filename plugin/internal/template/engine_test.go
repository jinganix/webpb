package template

import (
	"embed"
	"testing"
)

//go:embed testdata/broken
var brokenFS embed.FS

func TestNewJavaEngineProcessesTemplate(t *testing.T) {
	engine, err := NewJavaEngine()
	if err != nil {
		t.Fatalf("NewJavaEngine: %v", err)
	}
	_, err = engine.Process("nonexistent", map[string]any{})
	if err == nil {
		t.Fatal("expected error for unknown template")
	}
	content, err := engine.Process("enum.names", map[string]any{
		"package":   "demo",
		"className": "Colors",
		"enums":     []map[string]string{{"name": "RED", "value": "0"}},
	})
	if err != nil {
		t.Fatalf("Process enum.names: %v", err)
	}
	if content == "" {
		t.Fatal("expected rendered template")
	}
}

func TestNewEngineReturnsErrorForInvalidTemplate(t *testing.T) {
	if _, err := newEngine(brokenFS, "testdata/broken"); err == nil {
		t.Fatal("expected error for invalid template syntax")
	}
}

func TestNewEngineReturnsErrorWhenDirectoryIsMissing(t *testing.T) {
	if _, err := newEngine(brokenFS, "missing"); err == nil {
		t.Fatal("expected error for missing template directory")
	}
}

func TestNewTSEngine(t *testing.T) {
	engine, err := NewTSEngine()
	if err != nil {
		t.Fatalf("NewTSEngine: %v", err)
	}
	content, err := engine.Process("enum", map[string]any{
		"filename":  "demo.ts",
		"className": "Colors",
		"enums":     []map[string]string{{"name": "RED", "value": "'RED'"}},
		"imports":   []string{},
	})
	if err != nil {
		t.Fatalf("Process enum: %v", err)
	}
	if content == "" {
		t.Fatal("expected rendered ts template")
	}
}
