package tsgen

import (
	"github.com/jinganix/webpb/plugin/internal/core"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// GeneratorContext tracks subtype relationships across files.
type GeneratorContext struct {
	AllDescriptors []protoreflect.FileDescriptor
	BaseTypes      map[string]protoreflect.MessageDescriptor
	SubTypes       map[string][]protoreflect.MessageDescriptor
	JsDtsEnums     map[string]struct{}
}

// NewGeneratorContext builds generator context from request descriptors.
func NewGeneratorContext(all, targets []protoreflect.FileDescriptor) *GeneratorContext {
	ctx := &GeneratorContext{
		AllDescriptors: all,
		BaseTypes:      map[string]protoreflect.MessageDescriptor{},
		SubTypes:       map[string][]protoreflect.MessageDescriptor{},
		JsDtsEnums:     collectJsDtsEnums(targets),
	}
	for _, fileDescriptor := range targets {
		messages := fileDescriptor.Messages()
		for i := 0; i < messages.Len(); i++ {
			descriptor := messages.Get(i)
			opt := core.GetMessageOpts(descriptor, core.HasMessageOpt).GetOpt()
			if opt.GetSubType() != "" {
				ctx.BaseTypes[string(descriptor.Name())] = descriptor
			}
			if opt.GetExtends() != "" && len(opt.GetSubValues()) > 0 {
				ctx.SubTypes[opt.GetExtends()] = append(ctx.SubTypes[opt.GetExtends()], descriptor)
			}
		}
	}
	return ctx
}

func collectJsDtsEnums(targets []protoreflect.FileDescriptor) map[string]struct{} {
	out := map[string]struct{}{}
	for _, fd := range targets {
		enums := fd.Enums()
		for i := 0; i < enums.Len(); i++ {
			descriptor := enums.Get(i)
			if EnumUsesJsDts(fd, descriptor) {
				out[string(descriptor.FullName())] = struct{}{}
			}
		}
	}
	return out
}
