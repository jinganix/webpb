package core

import (
	"fmt"

	"google.golang.org/protobuf/reflect/protoreflect"
)

// GetAutoAliases returns auto-generated alias names for message fields.
func GetAutoAliases(all []protoreflect.FileDescriptor, msg protoreflect.MessageDescriptor) map[string]string {
	messages := append(GetExtendedMessages(msg), msg)
	names := make(map[string]struct{})
	for _, messageDesc := range messages {
		for _, field := range GetMemberFields(all, messageDesc) {
			names[string(field.Name())] = struct{}{}
		}
	}

	aliases := make(map[string]string)
	usedAliases := make(map[string]struct{})
	offset := 0
	for _, messageDesc := range messages {
		fields := GetMemberFields(all, messageDesc)
		maxIndex := offset
		for _, field := range fields {
			index := offset + int(field.Number()) - 1
			alias := ToBase52(index)
			for {
				if _, nameConflict := names[alias]; nameConflict {
					index++
					alias = ToBase52(index)
					continue
				}
				if _, aliasUsed := usedAliases[alias]; aliasUsed {
					index++
					alias = ToBase52(index)
					continue
				}
				break
			}
			aliases[string(field.Name())] = alias
			usedAliases[alias] = struct{}{}
			if index > maxIndex {
				maxIndex = index
			}
		}
		reserve := int(GetMessageOpts(messageDesc, HasMessageOpt).GetOpt().GetAliasReserve())
		offset = max(maxIndex, reserve)
	}
	return aliases
}

// CheckAliasReserve returns an error when alias_reserve is not greater than the max field number.
func CheckAliasReserve(all []protoreflect.FileDescriptor, msg protoreflect.MessageDescriptor) error {
	messages := append(GetExtendedMessages(msg), msg)
	for _, messageDesc := range messages {
		reserve := GetMessageOpts(messageDesc, HasMessageOpt).GetOpt().GetAliasReserve()
		if reserve == 0 {
			continue
		}
		maxNumber := protoreflect.FieldNumber(0)
		for _, field := range GetMemberFields(all, messageDesc) {
			if number := field.Number(); number > maxNumber {
				maxNumber = number
			}
		}
		if reserve <= int32(maxNumber) {
			return fmt.Errorf(
				"`alias_reserve` must be greater than max field number %d in message `%s`",
				maxNumber,
				messageDesc.FullName(),
			)
		}
	}
	return nil
}
