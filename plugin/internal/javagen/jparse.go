package javagen

import (
	"fmt"
	"strings"
)

type typeParser struct {
	imports *Imports
}

func (p *typeParser) parseClassOrInterfaceType(s string) (string, error) {
	s = strings.TrimSpace(s)
	if s == "" {
		return "", fmt.Errorf("Bad class or interface: %s", s)
	}
	out, err := p.renderTypeString(s)
	if err != nil {
		return "", err
	}
	return out, nil
}

func (p *typeParser) renderTypeString(s string) (string, error) {
	s = strings.TrimSpace(s)
	idx := strings.Index(s, "<")
	if idx < 0 {
		return p.imports.importName(s)
	}
	if !strings.HasSuffix(s, ">") {
		return "", fmt.Errorf("Bad class or interface: %s", s)
	}
	base := strings.TrimSpace(s[:idx])
	args, err := splitGenericArgs(s[idx+1 : len(s)-1])
	if err != nil {
		return "", err
	}
	renderedArgs := make([]string, len(args))
	for i, arg := range args {
		rendered, err := p.renderTypeString(arg)
		if err != nil {
			return "", err
		}
		renderedArgs[i] = rendered
	}
	importedBase, err := p.imports.importName(base)
	if err != nil {
		return "", err
	}
	return importedBase + "<" + strings.Join(renderedArgs, ", ") + ">", nil
}

func splitGenericArgs(s string) ([]string, error) {
	s = strings.TrimSpace(s)
	if s == "" {
		return nil, fmt.Errorf("expected type")
	}
	var args []string
	depth := 0
	start := 0
	for i := 0; i < len(s); i++ {
		switch s[i] {
		case '<':
			depth++
		case '>':
			depth--
			if depth < 0 {
				return nil, fmt.Errorf("expected ,")
			}
		case ',':
			if depth == 0 {
				args = append(args, strings.TrimSpace(s[start:i]))
				start = i + 1
			}
		}
	}
	if depth != 0 {
		return nil, fmt.Errorf("expected ,")
	}
	args = append(args, strings.TrimSpace(s[start:]))
	return args, nil
}

type annotationParser struct {
	imports *Imports
}

func (p *annotationParser) parseAnnotation(s string) (string, error) {
	s = strings.TrimSpace(s)
	if !strings.HasPrefix(s, "@") {
		return "", fmt.Errorf("Bad annotation: %s", s)
	}
	body := strings.TrimSpace(s[1:])
	name, rest, err := parseAnnotationName(body)
	if err != nil {
		return "", fmt.Errorf("Bad annotation: %s", s)
	}
	importedName, err := p.imports.importName(name)
	if err != nil {
		return "", err
	}
	rest = strings.TrimSpace(rest)
	if rest == "" {
		return "@" + importedName, nil
	}
	if !strings.HasPrefix(rest, "(") || !strings.HasSuffix(rest, ")") {
		return "", fmt.Errorf("Bad annotation: %s", s)
	}
	inner := strings.TrimSpace(rest[1 : len(rest)-1])
	if inner == "" {
		return "@" + importedName + "()", nil
	}
	if !strings.Contains(inner, "=") {
		value, err := p.importAnnotationValue(strings.TrimSpace(inner))
		if err != nil {
			return "", err
		}
		return "@" + importedName + "(" + value + ")", nil
	}
	pairs := splitAnnotationPairs(inner)
	for i, pair := range pairs {
		kv := strings.SplitN(pair, "=", 2)
		if len(kv) != 2 {
			return "", fmt.Errorf("Bad annotation: %s", s)
		}
		key := strings.TrimSpace(kv[0])
		value, err := p.importAnnotationValue(strings.TrimSpace(kv[1]))
		if err != nil {
			return "", err
		}
		pairs[i] = key + " = " + value
	}
	return "@" + importedName + "(" + strings.Join(pairs, ", ") + ")", nil
}

func (p *annotationParser) importAnnotationValue(value string) (string, error) {
	if strings.Contains(value, ".") && !strings.HasPrefix(value, "\"") {
		return p.imports.importName(value)
	}
	return value, nil
}

func parseAnnotationName(s string) (string, string, error) {
	for i := 0; i < len(s); i++ {
		if s[i] == '(' {
			return strings.TrimSpace(s[:i]), s[i:], nil
		}
	}
	return strings.TrimSpace(s), "", nil
}

func splitAnnotationPairs(s string) []string {
	var pairs []string
	depth := 0
	inString := false
	var quote byte
	start := 0
	for i := 0; i < len(s); i++ {
		ch := s[i]
		if inString {
			if ch == '\\' && i+1 < len(s) {
				i++
				continue
			}
			if ch == quote {
				inString = false
				quote = 0
			}
			continue
		}
		switch ch {
		case '"', '\'':
			inString = true
			quote = ch
		case '(':
			depth++
		case ')':
			depth--
		case ',':
			if depth == 0 {
				pairs = append(pairs, strings.TrimSpace(s[start:i]))
				start = i + 1
			}
		}
	}
	pairs = append(pairs, strings.TrimSpace(s[start:]))
	return pairs
}

func parseAnnotationSimpleName(s string) (string, error) {
	s = strings.TrimSpace(s)
	if !strings.HasPrefix(s, "@") {
		return "", fmt.Errorf("Bad annotation: %s", s)
	}
	name, _, err := parseAnnotationName(strings.TrimSpace(s[1:]))
	if err != nil {
		return "", fmt.Errorf("Bad annotation: %s", s)
	}
	return name, nil
}
