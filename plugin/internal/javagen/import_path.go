package javagen

import (
	"fmt"
	"strings"
)

// ImportPath represents a Java import path.
type ImportPath struct {
	path       string
	identifier string
}

// NewImportPath parses a Java qualified name into an ImportPath.
func NewImportPath(path string) (ImportPath, error) {
	if strings.Contains(path, "..") {
		return ImportPath{}, fmt.Errorf("Invalid import path: %s", path)
	}
	identifier := path
	if i := strings.LastIndex(path, "."); i >= 0 {
		identifier = path[i+1:]
	}
	return ImportPath{path: path, identifier: identifier}, nil
}

// Path returns the full import path.
func (p ImportPath) Path() string { return p.path }

// Identifier returns the simple name.
func (p ImportPath) Identifier() string { return p.identifier }

// Relative resolves a name against this import path.
func (p ImportPath) Relative(name string) string {
	if rel, ok := p.relative(name); ok {
		return rel
	}
	return ""
}

func (p ImportPath) relative(name string) (string, bool) {
	if p.identifier == name {
		return name, true
	}
	if !strings.Contains(name, ".") {
		return "", false
	}
	parts := strings.Split(name, ".")
	names := make([]string, 0, len(parts))
	if !p.matchExpr(parts, &names) {
		return "", false
	}
	for i, j := 0, len(names)-1; i < j; i, j = i+1, j-1 {
		names[i], names[j] = names[j], names[i]
	}
	return strings.Join(names, "."), true
}

func (p ImportPath) matchExpr(parts []string, names *[]string) bool {
	if len(parts) == 0 {
		return false
	}
	last := parts[len(parts)-1]
	*names = append(*names, last)
	if p.identifier == last {
		expr := strings.Join(parts, ".")
		return strings.HasSuffix(p.path, expr)
	}
	if len(parts) == 1 {
		if p.identifier == parts[0] {
			*names = append(*names, parts[0])
			return true
		}
		return false
	}
	return p.matchExpr(parts[:len(parts)-1], names)
}
