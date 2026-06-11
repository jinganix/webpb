package template

import (
	"bytes"
	"embed"
	"fmt"
	"path"
	"strings"
	"text/template"
)

//go:embed java/*.tmpl
var javaFS embed.FS

//go:embed ts/*.tmpl
var tsFS embed.FS

// Engine renders embedded Go templates.
type Engine struct {
	templates *template.Template
}

// NewJavaEngine creates a template engine for Java output.
func NewJavaEngine() (*Engine, error) {
	return newEngine(javaFS, "java")
}

// NewTSEngine creates a template engine for TypeScript output.
func NewTSEngine() (*Engine, error) {
	return newEngine(tsFS, "ts")
}

func newEngine(fs embed.FS, dir string) (*Engine, error) {
	tmpl := template.New(dir).Funcs(Funcs())
	entries, err := fs.ReadDir(dir)
	if err != nil {
		return nil, err
	}
	for _, entry := range entries {
		if entry.IsDir() || !strings.HasSuffix(entry.Name(), ".tmpl") {
			continue
		}
		content, err := fs.ReadFile(path.Join(dir, entry.Name()))
		if err != nil {
			return nil, err
		}
		name := strings.TrimSuffix(entry.Name(), ".tmpl")
		if _, err := tmpl.New(name).Parse(string(content)); err != nil {
			return nil, fmt.Errorf("parse %s: %w", name, err)
		}
	}
	return &Engine{templates: tmpl}, nil
}

// Process renders a named template with data.
func (e *Engine) Process(name string, data map[string]any) (string, error) {
	var buf bytes.Buffer
	if err := e.templates.ExecuteTemplate(&buf, name, data); err != nil {
		return "", err
	}
	return buf.String(), nil
}
