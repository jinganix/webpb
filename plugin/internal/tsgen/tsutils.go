package tsgen

import "strings"

// ToInterfaceName converts a type name to an interface name with an I prefix.
func ToInterfaceName(name string) string {
	if name == "" {
		return name
	}
	idx := strings.LastIndex(name, ".")
	if idx < 0 {
		return "I" + name
	}
	return name[:idx] + ".I" + name[idx+1:]
}
