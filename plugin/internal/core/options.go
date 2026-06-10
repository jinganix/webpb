package core

import (
	"fmt"

	"google.golang.org/protobuf/encoding/protowire"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protoreflect"

	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
)

func parseOptsFromUnknown[T proto.Message](opts proto.Message, newMsg func() T, pred func(T) bool) T {
	def := newMsg()
	if opts == nil {
		return def
	}
	unknown := opts.ProtoReflect().GetUnknown()
	for len(unknown) > 0 {
		num, typ, n := protowire.ConsumeTag(unknown)
		if n < 0 {
			break
		}
		unknown = unknown[n:]
		if typ == protowire.BytesType {
			val, m := protowire.ConsumeBytes(unknown)
			if m < 0 {
				break
			}
			unknown = unknown[m:]
			msg := newMsg()
			if err := proto.Unmarshal(val, msg); err != nil {
				continue
			}
			if pred(msg) {
				return msg
			}
			continue
		}
		m := protowire.ConsumeFieldValue(num, typ, unknown)
		if m < 0 {
			break
		}
		unknown = unknown[m:]
	}
	return def
}

func getExtension[T proto.Message](opts proto.Message, ext protoreflect.ExtensionType, pred func(T) bool) (T, bool) {
	var zero T
	if opts == nil {
		return zero, false
	}
	if v, ok := proto.GetExtension(opts, ext).(T); ok && pred(v) {
		return v, true
	}
	return zero, false
}

func resolveFileOpts(opts proto.Message, pred func(*webpb.FileOpts) bool) *webpb.FileOpts {
	if msg := parseOptsFromUnknown(opts, func() *webpb.FileOpts { return &webpb.FileOpts{} }, pred); pred(msg) {
		return msg
	}
	if v, ok := getExtension[*webpb.FileOpts](opts, webpb.E_FOpts, pred); ok {
		return v
	}
	return &webpb.FileOpts{}
}

func resolveMessageOpts(opts proto.Message, pred func(*webpb.MessageOpts) bool) *webpb.MessageOpts {
	if msg := parseOptsFromUnknown(opts, func() *webpb.MessageOpts { return &webpb.MessageOpts{} }, pred); pred(msg) {
		return msg
	}
	if v, ok := getExtension[*webpb.MessageOpts](opts, webpb.E_MOpts, pred); ok {
		return v
	}
	return &webpb.MessageOpts{}
}

func resolveEnumOpts(opts proto.Message, pred func(*webpb.EnumOpts) bool) *webpb.EnumOpts {
	if msg := parseOptsFromUnknown(opts, func() *webpb.EnumOpts { return &webpb.EnumOpts{} }, pred); pred(msg) {
		return msg
	}
	if v, ok := getExtension[*webpb.EnumOpts](opts, webpb.E_EOpts, pred); ok {
		return v
	}
	return &webpb.EnumOpts{}
}

func resolveFieldOpts(opts proto.Message, pred func(*webpb.FieldOpts) bool) *webpb.FieldOpts {
	if msg := parseOptsFromUnknown(opts, func() *webpb.FieldOpts { return &webpb.FieldOpts{} }, pred); pred(msg) {
		return msg
	}
	if v, ok := getExtension[*webpb.FieldOpts](opts, webpb.E_Opts, pred); ok {
		return v
	}
	return &webpb.FieldOpts{}
}

func resolveEnumValueOpts(opts proto.Message, pred func(*webpb.EnumValueOpts) bool) *webpb.EnumValueOpts {
	if msg := parseOptsFromUnknown(opts, func() *webpb.EnumValueOpts { return &webpb.EnumValueOpts{} }, pred); pred(msg) {
		return msg
	}
	if v, ok := getExtension[*webpb.EnumValueOpts](opts, webpb.E_VOpts, pred); ok {
		return v
	}
	return &webpb.EnumValueOpts{}
}

func hasFileJava(opts *webpb.FileOpts) bool   { return opts.GetJava() != nil }
func hasFileTs(opts *webpb.FileOpts) bool    { return opts.GetTs() != nil }
func hasMessageOpt(opts *webpb.MessageOpts) bool  { return opts.GetOpt() != nil }
func hasMessageJava(opts *webpb.MessageOpts) bool { return opts.GetJava() != nil }
func hasMessageTs(opts *webpb.MessageOpts) bool   { return opts.GetTs() != nil }
func hasEnumOpt(opts *webpb.EnumOpts) bool        { return opts.GetOpt() != nil }
func hasEnumJava(opts *webpb.EnumOpts) bool       { return opts.GetJava() != nil }
func hasEnumTs(opts *webpb.EnumOpts) bool         { return opts.GetTs() != nil }
func hasFieldOpt(opts *webpb.FieldOpts) bool      { return opts.GetOpt() != nil }
func hasFieldJava(opts *webpb.FieldOpts) bool     { return opts.GetJava() != nil }
func hasFieldTs(opts *webpb.FieldOpts) bool       { return opts.GetTs() != nil }
func hasEnumValueOpt(opts *webpb.EnumValueOpts) bool { return opts.GetOpt() != nil }

// GetWebpbFileOpts resolves webpb options from the webpb options proto dependency.
func GetWebpbFileOpts(fd protoreflect.FileDescriptor, pred func(*webpb.FileOpts) bool) *webpb.FileOpts {
	deps := fileDependencies(fd)
	wd := ResolveFile(deps, WebpbOptions)
	return GetFileOpts(wd, pred)
}

// GetFileOpts resolves FileOpts from descriptor options.
func GetFileOpts(fd protoreflect.FileDescriptor, pred func(*webpb.FileOpts) bool) *webpb.FileOpts {
	if fd == nil {
		return &webpb.FileOpts{}
	}
	opts, _ := fd.Options().(proto.Message)
	return resolveFileOpts(opts, pred)
}

// GetMessageOpts resolves MessageOpts from descriptor options.
func GetMessageOpts(msg protoreflect.MessageDescriptor, pred func(*webpb.MessageOpts) bool) *webpb.MessageOpts {
	if msg == nil {
		return &webpb.MessageOpts{}
	}
	opts, _ := msg.Options().(proto.Message)
	return resolveMessageOpts(opts, pred)
}

// GetEnumOpts resolves EnumOpts from descriptor options.
func GetEnumOpts(enum protoreflect.EnumDescriptor, pred func(*webpb.EnumOpts) bool) *webpb.EnumOpts {
	if enum == nil {
		return &webpb.EnumOpts{}
	}
	opts, _ := enum.Options().(proto.Message)
	return resolveEnumOpts(opts, pred)
}

// GetFieldOpts resolves FieldOpts from descriptor options.
func GetFieldOpts(field protoreflect.FieldDescriptor, pred func(*webpb.FieldOpts) bool) *webpb.FieldOpts {
	if field == nil {
		return &webpb.FieldOpts{}
	}
	opts, _ := field.Options().(proto.Message)
	return resolveFieldOpts(opts, pred)
}

// GetEnumValueOpts resolves EnumValueOpts from descriptor options.
func GetEnumValueOpts(value protoreflect.EnumValueDescriptor, pred func(*webpb.EnumValueOpts) bool) *webpb.EnumValueOpts {
	if value == nil {
		return &webpb.EnumValueOpts{}
	}
	opts, _ := value.Options().(proto.Message)
	return resolveEnumValueOpts(opts, pred)
}

// IsStringValue reports whether an enum uses string values.
func IsStringValue(enum protoreflect.EnumDescriptor) bool {
	enumOpts := GetEnumOpts(enum, hasEnumOpt)
	if enumOpts.GetOpt().GetStringValue() {
		return true
	}
	values := enum.Values()
	for i := 0; i < values.Len(); i++ {
		opts := GetEnumValueOpts(values.Get(i), hasEnumValueOpt).GetOpt()
		if opts.GetValue() != "" {
			return true
		}
	}
	return false
}

// GetExtendedMessages returns the extend chain for a message.
func GetExtendedMessages(msg protoreflect.MessageDescriptor) []protoreflect.MessageDescriptor {
	var descriptors []protoreflect.MessageDescriptor
	current := msg
	for {
		messageOpts := GetMessageOpts(current, hasMessageOpt).GetOpt()
		if messageOpts.GetExtends() == "" {
			for i, j := 0, len(descriptors)-1; i < j; i, j = i+1, j-1 {
				descriptors[i], descriptors[j] = descriptors[j], descriptors[i]
			}
			return descriptors
		}
		current = ResolveMessage([]protoreflect.FileDescriptor{current.ParentFile()}, messageOpts.GetExtends())
		if current != nil {
			descriptors = append(descriptors, current)
		}
	}
}

// CheckDuplicatedFields returns an error when extended fields have duplicate names.
func CheckDuplicatedFields(msg protoreflect.MessageDescriptor) error {
	fields := GetAllFields(msg)
	seen := make(map[string]struct{})
	for _, field := range fields {
		name := string(field.Name())
		if _, ok := seen[name]; ok {
			return fmt.Errorf("Duplicated field name `%s.%s` in %s when extends", msg.Name(), name, field.Parent().(protoreflect.MessageDescriptor).ParentFile().Path())
		}
		seen[name] = struct{}{}
	}
	return nil
}

// GetAllFields returns fields from the message and its extend chain.
func GetAllFields(msg protoreflect.MessageDescriptor) []protoreflect.FieldDescriptor {
	extended := GetExtendedMessages(msg)
	var result []protoreflect.FieldDescriptor
	for _, d := range extended {
		fields := d.Fields()
		for i := 0; i < fields.Len(); i++ {
			result = append(result, fields.Get(i))
		}
	}
	fields := msg.Fields()
	for i := 0; i < fields.Len(); i++ {
		result = append(result, fields.Get(i))
	}
	return result
}

func fileDependencies(fd protoreflect.FileDescriptor) []protoreflect.FileDescriptor {
	deps := make([]protoreflect.FileDescriptor, fd.Imports().Len())
	for i := 0; i < fd.Imports().Len(); i++ {
		deps[i] = fd.Imports().Get(i).FileDescriptor
	}
	return deps
}

// Option predicates exported for generators.
var (
	HasFileJava     = hasFileJava
	HasFileTs       = hasFileTs
	HasMessageOpt   = hasMessageOpt
	HasMessageJava  = hasMessageJava
	HasMessageTs    = hasMessageTs
	HasEnumOpt      = hasEnumOpt
	HasEnumJava     = hasEnumJava
	HasEnumTs       = hasEnumTs
	HasFieldOpt     = hasFieldOpt
	HasFieldJava    = hasFieldJava
	HasFieldTs      = hasFieldTs
	HasEnumValueOpt = hasEnumValueOpt
)
