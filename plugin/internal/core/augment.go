package core

import (
	"fmt"
	"sort"

	"google.golang.org/protobuf/reflect/protoreflect"
)

// IsAugmentMessage reports whether the message only contributes fields to another message.
func IsAugmentMessage(msg protoreflect.MessageDescriptor) bool {
	return GetAugmentOf(msg) != ""
}

// GetAugmentOf returns the augment_of target name, or empty when not an augment message.
func GetAugmentOf(msg protoreflect.MessageDescriptor) string {
	return GetMessageOpts(msg, HasMessageOpt).GetOpt().GetAugmentOf()
}

// IsOpenMessage reports whether other messages may augment this message.
func IsOpenMessage(msg protoreflect.MessageDescriptor) bool {
	return GetMessageOpts(msg, HasMessageOpt).GetOpt().GetOpen()
}

// GetFieldReserve returns the reserved field-number ceiling for host augment fields.
func GetFieldReserve(msg protoreflect.MessageDescriptor) int32 {
	return GetMessageOpts(msg, HasMessageOpt).GetOpt().GetFieldReserve()
}

// FindAugmentMessages returns augment messages that target msg, ordered by full name.
func FindAugmentMessages(all []protoreflect.FileDescriptor, msg protoreflect.MessageDescriptor) []protoreflect.MessageDescriptor {
	if msg == nil {
		return nil
	}
	var found []protoreflect.MessageDescriptor
	seen := map[protoreflect.FullName]struct{}{}
	for _, candidate := range collectTopLevelMessages(all) {
		augmentOf := GetAugmentOf(candidate)
		if augmentOf == "" {
			continue
		}
		target := ResolveMessage(all, augmentOf)
		if target == nil || target.FullName() != msg.FullName() {
			continue
		}
		if _, ok := seen[candidate.FullName()]; ok {
			continue
		}
		seen[candidate.FullName()] = struct{}{}
		found = append(found, candidate)
	}
	sort.Slice(found, func(i, j int) bool {
		return found[i].FullName() < found[j].FullName()
	})
	return found
}

// GetAugmentFields returns non-omitted fields contributed by augment messages.
func GetAugmentFields(all []protoreflect.FileDescriptor, msg protoreflect.MessageDescriptor) []protoreflect.FieldDescriptor {
	var fields []protoreflect.FieldDescriptor
	for _, augment := range FindAugmentMessages(all, msg) {
		for i := 0; i < augment.Fields().Len(); i++ {
			field := augment.Fields().Get(i)
			if GetFieldOpts(field, HasFieldOpt).GetOpt().GetOmitted() {
				continue
			}
			fields = append(fields, field)
		}
	}
	return fields
}

// GetMemberFields returns the message's own non-omitted fields plus augment fields.
func GetMemberFields(all []protoreflect.FileDescriptor, msg protoreflect.MessageDescriptor) []protoreflect.FieldDescriptor {
	var fields []protoreflect.FieldDescriptor
	for i := 0; i < msg.Fields().Len(); i++ {
		field := msg.Fields().Get(i)
		if GetFieldOpts(field, HasFieldOpt).GetOpt().GetOmitted() {
			continue
		}
		fields = append(fields, field)
	}
	fields = append(fields, GetAugmentFields(all, msg)...)
	return fields
}

// CheckAugmentMessage validates an augment fragment definition.
func CheckAugmentMessage(all []protoreflect.FileDescriptor, msg protoreflect.MessageDescriptor) error {
	augmentOf := GetAugmentOf(msg)
	if augmentOf == "" {
		return nil
	}
	opts := GetMessageOpts(msg, HasMessageOpt).GetOpt()
	if opts.GetMethod() != "" || opts.GetPath() != "" || opts.GetContext() != "" {
		return fmt.Errorf("augment message `%s` must not set method/path/context", msg.FullName())
	}
	if opts.GetExtends() != "" || len(opts.GetImplements()) > 0 {
		return fmt.Errorf("augment message `%s` must not set extends/implements", msg.FullName())
	}
	if opts.GetSubType() != "" || len(opts.GetSubValues()) > 0 {
		return fmt.Errorf("augment message `%s` must not set sub_type/sub_values", msg.FullName())
	}
	if opts.GetAliasReserve() != 0 || opts.GetOpen() || opts.GetFieldReserve() != 0 {
		return fmt.Errorf("augment message `%s` must not set alias_reserve/open/field_reserve", msg.FullName())
	}
	target := ResolveMessage(all, augmentOf)
	if target == nil {
		return fmt.Errorf("augment message `%s` targets unknown message `%s`", msg.FullName(), augmentOf)
	}
	if IsAugmentMessage(target) {
		return fmt.Errorf("augment message `%s` must not target another augment message `%s`", msg.FullName(), target.FullName())
	}
	if !IsOpenMessage(target) {
		return fmt.Errorf("message `%s` is not open; cannot augment from `%s`", target.FullName(), msg.FullName())
	}
	reserve := GetFieldReserve(target)
	for i := 0; i < msg.Fields().Len(); i++ {
		field := msg.Fields().Get(i)
		if reserve > 0 && int32(field.Number()) <= reserve {
			return fmt.Errorf(
				"augment field `%s.%s` number %d must be greater than field_reserve %d on `%s`",
				msg.FullName(),
				field.Name(),
				field.Number(),
				reserve,
				target.FullName(),
			)
		}
	}
	return CheckDuplicatedFields(all, target)
}

// CheckAugmentTarget validates that a non-augment message can accept its augment fields.
func CheckAugmentTarget(all []protoreflect.FileDescriptor, msg protoreflect.MessageDescriptor) error {
	if IsAugmentMessage(msg) {
		return nil
	}
	augments := FindAugmentMessages(all, msg)
	if len(augments) == 0 {
		return nil
	}
	if !IsOpenMessage(msg) {
		return fmt.Errorf("message `%s` is not open; cannot apply %d augment message(s)", msg.FullName(), len(augments))
	}
	for _, augment := range augments {
		if err := CheckAugmentMessage(all, augment); err != nil {
			return err
		}
	}
	return nil
}

func collectTopLevelMessages(all []protoreflect.FileDescriptor) []protoreflect.MessageDescriptor {
	var messages []protoreflect.MessageDescriptor
	seen := map[protoreflect.FullName]struct{}{}
	var walk func(protoreflect.FileDescriptor)
	walk = func(fd protoreflect.FileDescriptor) {
		if fd == nil {
			return
		}
		for i := 0; i < fd.Messages().Len(); i++ {
			msg := fd.Messages().Get(i)
			if _, ok := seen[msg.FullName()]; ok {
				continue
			}
			seen[msg.FullName()] = struct{}{}
			messages = append(messages, msg)
		}
		for i := 0; i < fd.Imports().Len(); i++ {
			walk(fd.Imports().Get(i).FileDescriptor)
		}
	}
	for _, fd := range all {
		walk(fd)
	}
	return messages
}
