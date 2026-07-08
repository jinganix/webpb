package tsgen

import (
	"regexp"
	"sort"
	"strings"
)

var prefixPattern = regexp.MustCompile(`^([./]+)`)

// Imports manages TypeScript imports.
type Imports struct {
	imported     []ImportPath
	typeImports  []TypeImport
	packageName  string
	imports      []string
	lookup       []string
	jsDtsEnums   map[string]struct{}
}

// NewImports creates an Imports instance.
func NewImports(packageName string, imports, lookup []string, jsDtsEnums map[string]struct{}) *Imports {
	if jsDtsEnums == nil {
		jsDtsEnums = map[string]struct{}{}
	}
	return &Imports{
		packageName: packageName,
		imports:     append([]string{}, imports...),
		lookup:      append([]string{}, lookup...),
		jsDtsEnums:  jsDtsEnums,
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

// ImportEnumType imports an enum as a type-only binding and returns its local name.
func (i *Imports) ImportEnumType(enumFullName string) string {
	parts := splitTypeName(enumFullName)
	if len(parts) == 0 {
		return enumFullName
	}
	enumName := parts[len(parts)-1]
	if len(parts) == 1 {
		if path, _, ok := i.resolveLookupPath(enumName); ok {
			i.addTypeImport(i.typeImportPath(path, enumName), enumName, 0)
		}
		return enumName
	}
	if parts[0] == i.packageName {
		if _, jsDts := i.jsDtsEnums[enumFullName]; jsDts {
			i.addTypeImport("./"+enumName+".js", enumName, 0)
		}
		return enumName
	}
	if _, jsDts := i.jsDtsEnums[enumFullName]; jsDts {
		i.addTypeImport(`./`+enumName+`.js`, enumName, 0)
		return enumName
	}
	if path, order, ok := i.resolveLookupPath(enumName); ok {
		i.addTypeImport(i.typeImportPath(path, enumName), enumName, order)
		return enumName
	}
	i.addTypeImport(`./`+parts[0], enumName, 0)
	return enumName
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

func (i *Imports) typeImportPath(modulePath, enumName string) string {
	for fullName := range i.jsDtsEnums {
		if strings.HasSuffix(fullName, "."+enumName) {
			return "./" + enumName + ".js"
		}
	}
	return modulePath
}

func (i *Imports) resolveLookupPath(typeName string) (path string, order int, ok bool) {
	typeTail := "/" + typeName
	for _, s := range i.lookup {
		if !strings.HasSuffix(s, typeTail) {
			continue
		}
		prefix := ""
		relative := s
		if m := prefixPattern.FindStringSubmatch(s); len(m) > 1 {
			prefix = m[1]
			relative = strings.TrimPrefix(s, prefix)
		}
		resolved := prefix + strings.TrimSuffix(relative, "/"+typeName)
		resolvedOrder := -1
		if prefix != "" && !strings.HasPrefix(prefix, "/") {
			resolvedOrder = 0
		}
		return resolved, resolvedOrder, true
	}
	return "", 0, false
}

func (i *Imports) addImported(importPath ImportPath) {
	for _, path := range i.imported {
		if path.name == importPath.name {
			return
		}
	}
	i.imported = append(i.imported, importPath)
}

func (i *Imports) addTypeImport(path, name string, order int) {
	for idx, entry := range i.typeImports {
		if entry.path != path {
			continue
		}
		for _, existing := range entry.names {
			if existing == name {
				return
			}
		}
		i.typeImports[idx].names = append(i.typeImports[idx].names, name)
		sort.Strings(i.typeImports[idx].names)
		return
	}
	i.typeImports = append(i.typeImports, TypeImport{
		path:  path,
		names: []string{name},
		order: order,
	})
}

// ToList returns import statements.
func (i *Imports) ToList() []string {
	sortedTypeImports := append([]TypeImport{}, i.typeImports...)
	sort.Slice(sortedTypeImports, func(a, b int) bool {
		if sortedTypeImports[a].order != 0 || sortedTypeImports[b].order != 0 {
			if sortedTypeImports[a].order != sortedTypeImports[b].order {
				return sortedTypeImports[a].order < sortedTypeImports[b].order
			}
		}
		return sortedTypeImports[a].path < sortedTypeImports[b].path
	})

	sorted := append([]ImportPath{}, i.imported...)
	sort.Slice(sorted, func(a, b int) bool {
		if sorted[a].order != 0 || sorted[b].order != 0 {
			if sorted[a].order != sorted[b].order {
				return sorted[a].order < sorted[b].order
			}
		}
		return sorted[a].name < sorted[b].name
	})

	out := make([]string, 0, len(sortedTypeImports)+len(sorted)+len(i.imports))
	for _, entry := range sortedTypeImports {
		out = append(out, `import type { `+strings.Join(entry.names, ", ")+` } from "`+entry.path+`";`)
	}
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
