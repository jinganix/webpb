package tsgen

// ImportPath represents a TypeScript import path.
type ImportPath struct {
	name  string
	path  string
	order int
}

// TypeImport records a type-only import from a module path.
type TypeImport struct {
	path  string
	names []string
	order int
}

// NewImportPath creates an ImportPath.
func NewImportPath(name, path string) ImportPath {
	return ImportPath{name: name, path: path, order: 0}
}

// NewImportPathOrdered creates an ImportPath with sort order.
func NewImportPathOrdered(name, path string, order int) ImportPath {
	return ImportPath{name: name, path: path, order: order}
}

// Name returns the import alias name.
func (p ImportPath) Name() string { return p.name }

// Path returns the import path.
func (p ImportPath) Path() string { return p.path }

// Order returns the sort order.
func (p ImportPath) Order() int { return p.order }
