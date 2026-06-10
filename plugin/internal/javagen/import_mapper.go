package javagen

import "regexp"

// ImportMapper maps imported paths to alternate paths.
type ImportMapper struct {
	mappings []mapping
}

type mapping struct {
	pattern     *regexp.Regexp
	replacement string
}

// NewImportMapper creates the default import mapper.
func NewImportMapper() *ImportMapper {
	return &ImportMapper{
		mappings: []mapping{
			{regexp.MustCompile(`^java\.lang.*$`), ""},
			{regexp.MustCompile(`^com\.google\.protobuf\.Any$`), "io.github.jinganix.webpb.runtime.Any"},
		},
	}
}

// Map maps a name to its replacement when matched.
func (m *ImportMapper) Map(name string) string {
	for _, mapping := range m.mappings {
		if mapping.pattern.MatchString(name) {
			return mapping.pattern.ReplaceAllString(name, mapping.replacement)
		}
	}
	return name
}
