package tsgen

import (
	"regexp"
	"sort"
	"strings"
)

var prefixPattern = regexp.MustCompile(`^([./]+)`)

// Imports manages TypeScript imports.
type Imports struct {
	imported    []ImportPath
	packageName string
	imports     []string
	lookup      []string
}

// NewImports creates an Imports instance.
func NewImports(packageName string, imports, lookup []string) *Imports {
	return &Imports{
		packageName: packageName,
		imports:     append([]string{}, imports...),
		lookup:      append([]string{}, lookup...),
	}
}

// NewEmptyImports creates an empty Imports instance.
func NewEmptyImports() *Imports {
	return &Imports{}
}

// ImportPath records a dynamic import path.
func (i *Imports) ImportPath(importPath ImportPath) {
	if importPath.name == i.packageName {
		return
	}
	i.addImported(importPath)
}

// ImportType imports a type and returns its local reference.
func (i *Imports) ImportType(typeName string) string {
	parts := splitTypeName(typeName)
	if len(parts) == 1 {
		typeTail := "/" + parts[0]
		for _, s := range i.lookup {
			if strings.HasSuffix(s, typeTail) {
				prefix := ""
				relative := s
				if m := prefixPattern.FindStringSubmatch(s); len(m) > 1 {
					prefix = m[1]
					relative = strings.TrimPrefix(s, prefix)
				}
				path := strings.TrimSuffix(relative, "/"+parts[0])
				name := path[strings.LastIndex(path, "/")+1:]
				order := -1
				if prefix != "" && !strings.HasPrefix(prefix, "/") {
					order = 0
				}
				i.addImported(NewImportPathOrdered(name, prefix+path, order))
				return name + "." + typeName
			}
		}
		return typeName
	}
	if parts[0] == i.packageName {
		return typeName[strings.Index(typeName, ".")+1:]
	}
	i.addImported(NewImportPath(parts[0], "./"+parts[0]))
	return typeName
}

func (i *Imports) addImported(importPath ImportPath) {
	for _, path := range i.imported {
		if path.name == importPath.name {
			return
		}
	}
	i.imported = append(i.imported, importPath)
}

// ToList returns import statements.
func (i *Imports) ToList() []string {
	sorted := append([]ImportPath{}, i.imported...)
	sort.Slice(sorted, func(a, b int) bool {
		if sorted[a].order != 0 || sorted[b].order != 0 {
			if sorted[a].order != sorted[b].order {
				return sorted[a].order < sorted[b].order
			}
		}
		return sorted[a].name < sorted[b].name
	})
	out := make([]string, 0, len(sorted)+len(i.imports))
	for _, e := range sorted {
		out = append(out, `import * as `+e.name+` from "`+e.path+`";`)
	}
	out = append(out, i.imports...)
	return out
}

func splitTypeName(typeName string) []string {
	var parts []string
	for _, part := range strings.Split(typeName, ".") {
		if part != "" {
			parts = append(parts, part)
		}
	}
	return parts
}
