package template

import (
	"fmt"
	"reflect"
	"strings"
	"text/template"

	"github.com/jinganix/webpb/plugin/internal/core"
)

// Funcs returns Freemarker-compatible template functions.
func Funcs() template.FuncMap {
	return template.FuncMap{
		"hasContent": hasContent,
		"capFirst":   core.Capitalize,
		"seqContains": func(list any, item any) bool {
			v := reflect.ValueOf(list)
			if v.Kind() != reflect.Slice && v.Kind() != reflect.Array {
				return false
			}
			for i := 0; i < v.Len(); i++ {
				if fmt.Sprint(v.Index(i).Interface()) == fmt.Sprint(item) {
					return true
				}
			}
			return false
		},
		"then": func(cond bool, a, b any) any {
			if cond {
				return a
			}
			return b
		},
		"keepAfter": func(s, sep string) string {
			if i := strings.LastIndex(s, sep); i >= 0 {
				return s[i+len(sep):]
			}
			return s
		},
		"keepBefore": func(s, sep string) string {
			if i := strings.Index(s, sep); i >= 0 {
				return s[:i]
			}
			return s
		},
		"keys": func(m map[string]string) []string {
			keys := make([]string, 0, len(m))
			for k := range m {
				keys = append(keys, k)
			}
			// TreeMap order
			stringsSliceSort(keys)
			return keys
		},
		"get": func(m map[string]any, key string) any {
			if m == nil {
				return nil
			}
			return m[key]
		},
		"last": func(index int, list any) bool {
			v := reflect.ValueOf(list)
			return index == v.Len()-1
		},
	}
}

func hasContent(v any) bool {
	if v == nil {
		return false
	}
	switch t := v.(type) {
	case string:
		return t != ""
	case []string:
		return len(t) > 0
	case []any:
		return len(t) > 0
	case []map[string]any:
		return len(t) > 0
	case []map[string]string:
		return len(t) > 0
	case map[string]any:
		return len(t) > 0
	case map[string]string:
		return len(t) > 0
	default:
		rv := reflect.ValueOf(v)
		switch rv.Kind() {
		case reflect.Slice, reflect.Array, reflect.Map:
			return rv.Len() > 0
		default:
			return true
		}
	}
}

func stringsSliceSort(ss []string) {
	for i := 0; i < len(ss); i++ {
		for j := i + 1; j < len(ss); j++ {
			if ss[j] < ss[i] {
				ss[i], ss[j] = ss[j], ss[i]
			}
		}
	}
}
