package commons

import "strings"

// UrlSegment is a path param captured from a URL.
type UrlSegment struct {
	accessor bool
	prefix   string
	key      string
	value    string
}

// NewUrlSegment constructs a UrlSegment.
func NewUrlSegment(prefix, key, value string) UrlSegment {
	accessor := strings.HasPrefix(value, "{") && strings.HasSuffix(value, "}")
	v := value
	if accessor {
		v = value[1 : len(value)-1]
	}
	return UrlSegment{
		accessor: accessor,
		prefix:   prefix,
		key:      key,
		value:    v,
	}
}

// IsQuery reports whether the segment is in the query string.
func (s UrlSegment) IsQuery() bool {
	return s.key != ""
}

// IsAccessor reports whether the segment is a path accessor.
func (s UrlSegment) IsAccessor() bool {
	return s.accessor
}

// Prefix returns the segment prefix.
func (s UrlSegment) Prefix() string {
	return s.prefix
}

// Key returns the segment key.
func (s UrlSegment) Key() string {
	return s.key
}

// Value returns the segment value.
func (s UrlSegment) Value() string {
	return s.value
}
