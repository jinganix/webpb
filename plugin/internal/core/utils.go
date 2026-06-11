package core

import (
	"net/url"
	"strings"
)

// Normalize normalizes a request path.
func Normalize(path string) string {
	if path == "" {
		return path
	}
	if u, err := url.Parse(path); err == nil && u.Scheme != "" {
		return strings.TrimRight(path, "/")
	}
	tmp := strings.TrimLeft(path, "/")
	if tmp == "" {
		return ""
	}
	return "/" + strings.TrimRight(tmp, "/")
}

// LimitNewline trims trailing newlines beyond limit.
func LimitNewline(builder *strings.Builder, limit int) {
	s := builder.String()
	i := len(s)
	for i > 0 && s[i-1] == '\n' {
		if len(s)-i+1 > limit {
			builder.Reset()
			builder.WriteString(s[:len(s)-1])
			s = builder.String()
		}
		i--
	}
}

// AlignNewline ensures the builder ends with count newlines.
func AlignNewline(builder *strings.Builder, count int) {
	LimitNewline(builder, count)
	if count <= 0 {
		return
	}
	s := builder.String()
	for len(s) < count || s[len(s)-count] != '\n' {
		builder.WriteByte('\n')
		s = builder.String()
	}
}

// ToBase52 converts a number to a base-52 string.
func ToBase52(num int) string {
	const chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
	var sb strings.Builder
	for num > 0 || sb.Len() == 0 {
		remainder := num % len(chars)
		sb.WriteByte(chars[remainder])
		num /= len(chars)
	}
	runes := []rune(sb.String())
	for i, j := 0, len(runes)-1; i < j; i, j = i+1, j-1 {
		runes[i], runes[j] = runes[j], runes[i]
	}
	return string(runes)
}

// Capitalize capitalizes the first rune of s.
func Capitalize(s string) string {
	if s == "" {
		return s
	}
	return strings.ToUpper(s[:1]) + s[1:]
}
