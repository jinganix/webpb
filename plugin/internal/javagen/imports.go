package javagen

import (
	"fmt"
	"sort"
	"strings"

	"github.com/jinganix/webpb/plugin/internal/core"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// Imports manages Java import statements.
type Imports struct {
	javaPackage string
	imported    []ImportPath
	lookup      []ImportPath
	mapper      *ImportMapper
	rootFd      protoreflect.FileDescriptor
}

// NewImports creates an Imports instance.
func NewImports(javaPackage string, lookup []ImportPath, rootFd protoreflect.FileDescriptor) *Imports {
	return &Imports{
		javaPackage: javaPackage,
		lookup:      lookup,
		mapper:      NewImportMapper(),
		rootFd:      rootFd,
	}
}

// GetLookup builds lookup paths for a file descriptor.
func GetLookup(fd protoreflect.FileDescriptor) ([]ImportPath, error) {
	if fd == nil {
		var lookup []ImportPath
		for _, path := range []string{
			"java.lang.Boolean",
			"java.lang.Double",
			"java.lang.Float",
			"java.lang.Integer",
			"java.lang.Long",
			"java.lang.String",
			"java.util.List",
			"java.util.Map",
			core.RuntimePackage + ".WebpbMessage",
			core.RuntimePackage + ".WebpbMeta",
			core.RuntimePackage + ".WebpbSubValue",
			core.RuntimePackage + ".common.InQuery",
			core.RuntimePackage + ".enumeration.Enumeration",
		} {
			importPath, err := NewImportPath(path)
			if err != nil {
				return nil, err
			}
			lookup = append(lookup, importPath)
		}
		return lookup, nil
	}
	var paths []string
	paths = append(paths,
		"java.lang.Boolean",
		"java.lang.Double",
		"java.lang.Float",
		"java.lang.Integer",
		"java.lang.Long",
		"java.lang.String",
		"java.util.List",
		"java.util.Map",
		core.RuntimePackage+".WebpbMessage",
		core.RuntimePackage+".WebpbMeta",
		core.RuntimePackage+".WebpbSubValue",
		core.RuntimePackage+".common.InQuery",
		core.RuntimePackage+".enumeration.Enumeration",
	)
	paths = append(paths, core.GetWebpbFileOpts(fd, core.HasFileJava).GetJava().GetImport()...)
	paths = append(paths, core.GetFileOpts(fd, core.HasFileJava).GetJava().GetImport()...)
	for _, d := range core.ResolveTopLevelTypes(fd) {
		pkg := GetJavaPackage(d)
		if pkg == "" {
			paths = append(paths, string(d.Name()))
		} else {
			paths = append(paths, pkg+"."+string(d.Name()))
		}
	}
	seen := map[string]struct{}{}
	var lookup []ImportPath
	for _, path := range paths {
		if _, ok := seen[path]; ok {
			continue
		}
		seen[path] = struct{}{}
		importPath, err := NewImportPath(path)
		if err != nil {
			return nil, err
		}
		lookup = append(lookup, importPath)
	}
	sort.Slice(lookup, func(i, j int) bool {
		return len(lookup[i].path) > len(lookup[j].path)
	})
	return lookup, nil
}

// ImportAnnotation imports types referenced in an annotation string.
func (i *Imports) ImportAnnotation(str string) (string, error) {
	parser := &annotationParser{imports: i}
	return parser.parseAnnotation(str)
}

// ImportClassOrInterface imports types referenced in a class or interface type string.
func (i *Imports) ImportClassOrInterface(str string) (string, error) {
	parser := &typeParser{imports: i}
	return parser.parseClassOrInterfaceType(str)
}

// ImportGenericDescriptor imports a message or enum descriptor type.
func (i *Imports) ImportGenericDescriptor(descriptor protoreflect.Descriptor) (string, error) {
	if descriptor == nil {
		return "", fmt.Errorf("Bad class or interface: <nil>")
	}
	fd := descriptor.ParentFile()
	if fd == nil {
		fd = core.ResolveDescriptorFile(i.rootFd, descriptor)
	}
	if fd == nil {
		return i.ImportClassOrInterface(string(descriptor.Name()))
	}
	packageName := string(fd.Package())
	relative := strings.Replace(string(descriptor.FullName()), packageName+".", "", 1)
	relative = strings.TrimPrefix(relative, ".")
	prefix := ""
	if pkg := GetJavaPackage(fd); pkg != "" {
		prefix = pkg + "."
	}
	fullPath := prefix + relative
	importPath := prefix + strings.Split(relative, ".")[0]
	return i.checkAndImport(fullPath, importPath, relative), nil
}

// ImportedQualifiedName returns the qualified name if imported.
func (i *Imports) ImportedQualifiedName(name string) string {
	for _, importPath := range i.imported {
		if rel := importPath.Relative(name); rel != "" {
			return importPath.Path()
		}
	}
	return name
}

func (i *Imports) importName(name string) (string, error) {
	for _, importPath := range i.lookup {
		if rel, ok := importPath.relative(name); ok {
			return i.checkAndImport(name, importPath.Path(), rel), nil
		}
	}
	return "", fmt.Errorf("No import path found for: %s", name)
}

func (i *Imports) checkAndImport(fullPath, importPath, relative string) string {
	mapped := i.mapper.Map(importPath)
	importPackage := mapped
	if idx := strings.LastIndex(mapped, "."); idx >= 0 {
		importPackage = mapped[:idx]
	}
	if importPackage == i.javaPackage || importPackage == importPath {
		return relative
	}
	identifier := strings.Split(relative, ".")[0]
	for _, imported := range i.imported {
		if imported.Path() != mapped && imported.Identifier() == identifier {
			return i.mapper.Map(fullPath)
		}
	}
	if !i.containsImport(mapped) {
		i.imported = append(i.imported, ImportPath{path: mapped, identifier: identifierFromPath(mapped)})
	}
	return relative
}

func identifierFromPath(path string) string {
	if i := strings.LastIndex(path, "."); i >= 0 {
		return path[i+1:]
	}
	return path
}

func (i *Imports) containsImport(path string) bool {
	for _, imported := range i.imported {
		if imported.Path() == path {
			return true
		}
	}
	return false
}

// ToList returns sorted import statements.
func (i *Imports) ToList() []string {
	seen := map[string]struct{}{}
	var out []string
	for _, e := range i.imported {
		mapped := i.mapper.Map(e.Path())
		if mapped == "" {
			continue
		}
		if _, ok := seen[mapped]; ok {
			continue
		}
		seen[mapped] = struct{}{}
		out = append(out, mapped)
	}
	sort.Strings(out)
	return out
}
