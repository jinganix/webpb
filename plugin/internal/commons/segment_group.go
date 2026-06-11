package commons

import (
	"regexp"
	"strings"
)

var (
	pathPattern  = regexp.MustCompile(`\{[^/]+}`)
	queryPattern = regexp.MustCompile(`(?:(\w+)=)?([^/?&]+)&?`)
)

// SegmentGroup groups UrlSegment values captured from a URL.
type SegmentGroup struct {
	pathSegments  []UrlSegment
	suffix        string
	querySegments []UrlSegment
	segments      []UrlSegment
}

// Of parses a URL into a SegmentGroup.
func Of(url string) SegmentGroup {
	group := SegmentGroup{}
	if url == "" {
		return group
	}
	parts := strings.SplitN(url, "?", 2)
	path := parts[0]
	query := ""
	if len(parts) == 2 {
		query = parts[1]
	} else if strings.Contains(path, "=") {
		path = ""
		query = parts[0]
	}

	if path != "" {
		loc := pathPattern.FindAllStringIndex(path, -1)
		index := 0
		for _, match := range loc {
			value := path[match[0]:match[1]]
			group.pathSegments = append(group.pathSegments, NewUrlSegment(path[index:match[0]], "", value))
			index = match[1]
		}
		group.suffix = path[index:]
	}

	if query != "" {
		submatches := queryPattern.FindAllStringSubmatch(query, -1)
		for _, m := range submatches {
			key := ""
			if len(m) > 1 {
				key = m[1]
			}
			value := ""
			if len(m) > 2 {
				value = m[2]
			}
			group.querySegments = append(group.querySegments, NewUrlSegment("", key, value))
		}
	}

	group.segments = append(group.segments, group.pathSegments...)
	group.segments = append(group.segments, group.querySegments...)
	return group
}

// IsEmpty reports whether the group has no segments.
func (g SegmentGroup) IsEmpty() bool {
	return len(g.pathSegments) == 0 && len(g.querySegments) == 0
}

// PathSegments returns path segments.
func (g SegmentGroup) PathSegments() []UrlSegment {
	return g.pathSegments
}

// QuerySegments returns query segments.
func (g SegmentGroup) QuerySegments() []UrlSegment {
	return g.querySegments
}

// Segments returns all segments.
func (g SegmentGroup) Segments() []UrlSegment {
	return g.segments
}

// Suffix returns the path suffix.
func (g SegmentGroup) Suffix() string {
	return g.suffix
}
