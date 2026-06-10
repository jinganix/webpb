package tsgen

import (
	"regexp"
	"strings"
)

func normalizeTsTemplateOutput(content string) string {
	content = normalizeTsInterfaceBlocks(content)
	// Blank line after header comment block before imports or exports.
	content = regexp.MustCompile(`(?m)(// https://github.com/jinganix/webpb\n)\n*(import |export )`).ReplaceAllString(content, "$1\n\n$2")
	content = regexp.MustCompile(`(?m)(// [^\n]+\.proto\n)\n*(import |export )`).ReplaceAllString(content, "$1\n\n$2")
	// Blank line before static fromAlias.
	content = regexp.MustCompile(`(?m)(  static create\([^\n]+\n    return [^\n]+;\n  \})\n(  static fromAlias)`).ReplaceAllString(content, "$1\n\n$2")
	content = regexp.MustCompile(`(?m)(    static create\([^\n]+\n      return [^\n]+;\n    \})\n(    static fromAlias)`).ReplaceAllString(content, "$1\n\n$2")
	// Remove blank line before namespace closing brace.
	content = regexp.MustCompile(`(?m)(\n  \}\n)\n+(\})`).ReplaceAllString(content, "$1$2")
	// Collapse excessive blank lines and trim trailing whitespace.
	content = regexp.MustCompile(`\n{3,}`).ReplaceAllString(content, "\n\n")
	return strings.TrimRight(content, "\n") + "\n"
}

func normalizeTsInterfaceBlocks(content string) string {
	lines := strings.Split(content, "\n")
	var out []string
	inInterface := false
	for _, line := range lines {
		trimmed := strings.TrimSpace(line)
		if strings.HasPrefix(trimmed, "export interface ") && strings.HasSuffix(trimmed, "{") {
			inInterface = true
			out = append(out, line)
			continue
		}
		if inInterface {
			if trimmed == "" {
				continue
			}
			if trimmed == "}" {
				inInterface = false
				out = append(out, line)
				continue
			}
			out = append(out, line)
			continue
		}
		out = append(out, line)
	}
	result := strings.Join(out, "\n")
	if strings.HasSuffix(content, "\n") && !strings.HasSuffix(result, "\n") {
		result += "\n"
	}
	return result
}
