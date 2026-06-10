package javagen

import (
	"fmt"

	"github.com/jinganix/webpb/plugin/internal/core"
)

// AnnotationDistinctFilter filters duplicate non-repeatable annotations.
type AnnotationDistinctFilter struct {
	imports  *Imports
	repeat   []string
	existing map[string]struct{}
}

// NewAnnotationDistinctFilter creates a new filter.
func NewAnnotationDistinctFilter(imports *Imports, repeatable []string) *AnnotationDistinctFilter {
	repeat := append([]string{}, repeatable...)
	repeat = append(repeat, core.RuntimePackage+".WebpbSubValue")
	return &AnnotationDistinctFilter{
		imports:  imports,
		repeat:   repeat,
		existing: map[string]struct{}{},
	}
}

// Allow reports whether an annotation should be kept.
func (f *AnnotationDistinctFilter) Allow(str string) bool {
	name, err := parseAnnotationSimpleName(str)
	if err != nil {
		panic(fmt.Sprintf("Bad annotation: %s", str))
	}
	qualified := f.imports.ImportedQualifiedName(name)
	for _, r := range f.repeat {
		if r == qualified {
			return true
		}
	}
	if _, ok := f.existing[qualified]; ok {
		return false
	}
	f.existing[qualified] = struct{}{}
	return true
}
