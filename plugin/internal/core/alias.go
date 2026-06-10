package core

import "google.golang.org/protobuf/reflect/protoreflect"

// GetAutoAliases returns auto-generated alias names for message fields.
func GetAutoAliases(msg protoreflect.MessageDescriptor) map[string]string {
	fields := GetAllFields(msg)
	names := make(map[string]struct{}, len(fields))
	for _, field := range fields {
		names[string(field.Name())] = struct{}{}
	}
	aliases := make(map[string]string)
	index := 0
	for _, field := range fields {
		alias := ToBase52(index)
		index++
		for {
			if _, exists := names[alias]; exists {
				alias = ToBase52(index)
				index++
				continue
			}
			break
		}
		aliases[string(field.Name())] = alias
	}
	return aliases
}
