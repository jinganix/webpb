package core

import (
	"regexp"
	"strings"

	"github.com/jinganix/webpb/plugin/internal/commons"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// IsEnum reports whether a field has enum type.
func IsEnum(field protoreflect.FieldDescriptor) bool {
	return field.Kind() == protoreflect.EnumKind
}

// IsMessage reports whether a field has message type.
func IsMessage(field protoreflect.FieldDescriptor) bool {
	return field.Kind() == protoreflect.MessageKind || field.Kind() == protoreflect.GroupKind
}

// GetGenericDescriptor returns the enum or message descriptor for a field.
func GetGenericDescriptor(field protoreflect.FieldDescriptor) protoreflect.Descriptor {
	if IsEnum(field) {
		return field.Enum()
	}
	return field.Message()
}

// ResolveNestedTypes returns descriptor and all nested message types.
func ResolveNestedTypes(descriptor protoreflect.Descriptor) []protoreflect.Descriptor {
	result := []protoreflect.Descriptor{descriptor}
	if msg, ok := descriptor.(protoreflect.MessageDescriptor); ok {
		nested := msg.Messages()
		for i := 0; i < nested.Len(); i++ {
			result = append(result, ResolveNestedTypes(nested.Get(i))...)
		}
	}
	return result
}

// ResolveTopLevelTypes returns top-level messages and enums recursively including dependencies.
func ResolveTopLevelTypes(fd protoreflect.FileDescriptor) []protoreflect.Descriptor {
	var result []protoreflect.Descriptor
	msgs := fd.Messages()
	for i := 0; i < msgs.Len(); i++ {
		result = append(result, msgs.Get(i))
	}
	enums := fd.Enums()
	for i := 0; i < enums.Len(); i++ {
		result = append(result, enums.Get(i))
	}
	deps := fd.Imports()
	for i := 0; i < deps.Len(); i++ {
		result = append(result, ResolveTopLevelTypes(deps.Get(i).FileDescriptor)...)
	}
	return result
}

func endsWithName(fullName, name string) bool {
	p1 := strings.Split(fullName, ".")
	p2 := strings.Split(name, ".")
	if len(p1) < len(p2) {
		return false
	}
	for i := 1; i <= len(p2); i++ {
		if p1[len(p1)-i] != p2[len(p2)-i] {
			return false
		}
	}
	return true
}

// ResolveMessage finds a message descriptor by suffix name.
func ResolveMessage(descriptors []protoreflect.FileDescriptor, name string) protoreflect.MessageDescriptor {
	for _, fd := range descriptors {
		msgs := fd.Messages()
		for i := 0; i < msgs.Len(); i++ {
			if endsWithName(string(msgs.Get(i).FullName()), name) {
				return msgs.Get(i)
			}
		}
		deps := make([]protoreflect.FileDescriptor, fd.Imports().Len())
		for i := 0; i < fd.Imports().Len(); i++ {
			deps[i] = fd.Imports().Get(i).FileDescriptor
		}
		if found := ResolveMessage(deps, name); found != nil {
			return found
		}
	}
	return nil
}

// ResolveEnum finds an enum descriptor by suffix name.
func ResolveEnum(descriptors []protoreflect.FileDescriptor, name string) protoreflect.EnumDescriptor {
	for _, fd := range descriptors {
		enums := fd.Enums()
		for i := 0; i < enums.Len(); i++ {
			if endsWithName(string(enums.Get(i).FullName()), name) {
				return enums.Get(i)
			}
		}
		deps := make([]protoreflect.FileDescriptor, fd.Imports().Len())
		for i := 0; i < fd.Imports().Len(); i++ {
			deps[i] = fd.Imports().Get(i).FileDescriptor
		}
		if found := ResolveEnum(deps, name); found != nil {
			return found
		}
	}
	return nil
}

// ResolveEnumValue finds an enum value descriptor by suffix name.
func ResolveEnumValue(descriptors []protoreflect.FileDescriptor, name string) protoreflect.EnumValueDescriptor {
	for _, fd := range descriptors {
		for _, enum := range collectFileEnums(fd) {
			values := enum.Values()
			for j := 0; j < values.Len(); j++ {
				value := values.Get(j)
				if endsWithName(string(value.FullName()), name) {
					return value
				}
			}
		}
		if enumName, valueName, ok := splitEnumValueName(name); ok {
			for _, enum := range collectFileEnums(fd) {
				if string(enum.Name()) != enumName {
					continue
				}
				if value := enum.Values().ByName(protoreflect.Name(valueName)); value != nil {
					return value
				}
			}
		}
		deps := make([]protoreflect.FileDescriptor, fd.Imports().Len())
		for i := 0; i < fd.Imports().Len(); i++ {
			deps[i] = fd.Imports().Get(i).FileDescriptor
		}
		if found := ResolveEnumValue(deps, name); found != nil {
			return found
		}
	}
	return nil
}

func splitEnumValueName(name string) (enumName, valueName string, ok bool) {
	parts := strings.Split(name, ".")
	if len(parts) < 2 {
		return "", "", false
	}
	return parts[len(parts)-2], parts[len(parts)-1], true
}

func collectFileEnums(fd protoreflect.FileDescriptor) []protoreflect.EnumDescriptor {
	var enums []protoreflect.EnumDescriptor
	for i := 0; i < fd.Enums().Len(); i++ {
		enums = append(enums, fd.Enums().Get(i))
	}
	var collectFromMessage func(protoreflect.MessageDescriptor)
	collectFromMessage = func(msg protoreflect.MessageDescriptor) {
		for i := 0; i < msg.Enums().Len(); i++ {
			enums = append(enums, msg.Enums().Get(i))
		}
		for i := 0; i < msg.Messages().Len(); i++ {
			collectFromMessage(msg.Messages().Get(i))
		}
	}
	for i := 0; i < fd.Messages().Len(); i++ {
		collectFromMessage(fd.Messages().Get(i))
	}
	return enums
}

// ResolveDescriptorFile finds the file that owns a descriptor when ParentFile is unavailable.
func ResolveDescriptorFile(root protoreflect.FileDescriptor, descriptor protoreflect.Descriptor) protoreflect.FileDescriptor {
	if fd := descriptor.ParentFile(); fd != nil {
		return fd
	}
	if root == nil {
		return nil
	}
	target := string(descriptor.FullName())
	var search func(protoreflect.FileDescriptor) protoreflect.FileDescriptor
	search = func(fd protoreflect.FileDescriptor) protoreflect.FileDescriptor {
		if fd == nil {
			return nil
		}
		for i := 0; i < fd.Messages().Len(); i++ {
			for _, d := range ResolveNestedTypes(fd.Messages().Get(i)) {
				if string(d.FullName()) == target {
					return fd
				}
			}
		}
		for i := 0; i < fd.Enums().Len(); i++ {
			if string(fd.Enums().Get(i).FullName()) == target {
				return fd
			}
		}
		for i := 0; i < fd.Imports().Len(); i++ {
			if found := search(fd.Imports().Get(i).FileDescriptor); found != nil {
				return found
			}
		}
		return nil
	}
	return search(root)
}

// ResolveFile finds a file descriptor by regex name match.
func ResolveFile(descriptors []protoreflect.FileDescriptor, regex string) protoreflect.FileDescriptor {
	pattern := regexp.MustCompile(regex)
	for _, fd := range descriptors {
		if pattern.MatchString(fd.Path()) {
			return fd
		}
		deps := make([]protoreflect.FileDescriptor, fd.Imports().Len())
		for i := 0; i < fd.Imports().Len(); i++ {
			deps[i] = fd.Imports().Get(i).FileDescriptor
		}
		if found := ResolveFile(deps, regex); found != nil {
			return found
		}
	}
	return nil
}

// GetFieldTypePackage returns the package of a field's message or enum type.
func GetFieldTypePackage(field protoreflect.FieldDescriptor) string {
	if IsMessage(field) {
		return string(field.Message().ParentFile().Package())
	}
	if IsEnum(field) {
		return string(field.Enum().ParentFile().Package())
	}
	return ""
}

// GetFieldTypeSimpleName returns the simple type name of a field.
func GetFieldTypeSimpleName(field protoreflect.FieldDescriptor) string {
	if IsMessage(field) {
		return string(field.Message().Name())
	}
	if IsEnum(field) {
		return string(field.Enum().Name())
	}
	return field.Kind().String()
}

// GetFieldTypeFullName returns the full type name of a field.
func GetFieldTypeFullName(field protoreflect.FieldDescriptor) string {
	if IsMessage(field) {
		return string(field.Message().FullName())
	}
	if IsEnum(field) {
		return string(field.Enum().FullName())
	}
	return field.Kind().String()
}

// GetMapKeyDescriptor returns the key field of a map entry message.
func GetMapKeyDescriptor(field protoreflect.FieldDescriptor) protoreflect.FieldDescriptor {
	return field.Message().Fields().Get(0)
}

// GetMapValueDescriptor returns the value field of a map entry message.
func GetMapValueDescriptor(field protoreflect.FieldDescriptor) protoreflect.FieldDescriptor {
	return field.Message().Fields().Get(1)
}

// Validation validates path variable accessors against a message descriptor.
func Validation(group commons.SegmentGroup, descriptor protoreflect.MessageDescriptor, files []protoreflect.FileDescriptor) {
	for _, segment := range group.Segments() {
		if segment.IsAccessor() && !validateAccessor(segment.Value(), descriptor, files) {
			panic("Invalid accessor " + segment.Value())
		}
	}
}

func validateAccessor(accessor string, descriptor protoreflect.MessageDescriptor, files []protoreflect.FileDescriptor) bool {
	names := strings.Split(accessor, ".")
	for i, name := range names {
		field := descriptor.Fields().ByName(protoreflect.Name(name))
		if field == nil {
			return false
		}
		if i == len(names)-1 {
			return true
		}
		if !IsMessage(field) {
			return false
		}
		descriptor = field.Message()
		if descriptor.Fields().ByName(protoreflect.Name(names[i+1])) == nil {
			if resolved := findMessageDescriptor(files, descriptor.FullName()); resolved != nil {
				descriptor = resolved
			}
		}
	}
	return true
}

func findMessageDescriptor(files []protoreflect.FileDescriptor, name protoreflect.FullName) protoreflect.MessageDescriptor {
	var search func(protoreflect.MessageDescriptor) protoreflect.MessageDescriptor
	search = func(descriptor protoreflect.MessageDescriptor) protoreflect.MessageDescriptor {
		if descriptor.FullName() == name {
			return descriptor
		}
		for i := 0; i < descriptor.Messages().Len(); i++ {
			if found := search(descriptor.Messages().Get(i)); found != nil {
				return found
			}
		}
		return nil
	}
	for _, fd := range files {
		for i := 0; i < fd.Messages().Len(); i++ {
			if found := search(fd.Messages().Get(i)); found != nil {
				return found
			}
		}
	}
	return nil
}
