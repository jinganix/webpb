package javagen

import (
	"testing"

	"github.com/jinganix/webpb/plugin/internal/testutil"
	"google.golang.org/protobuf/reflect/protoreflect"
)

func TestPrimitiveTypesFields(t *testing.T) {
	ctx, err := testutil.CreateContext("generator_options")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	var fd protoreflect.FileDescriptor
	for _, candidate := range ctx.TargetDescriptors {
		if candidate.Path() == "PrimitiveTypes.proto" {
			fd = candidate
			break
		}
	}
	if fd == nil {
		t.Fatal("PrimitiveTypes.proto not found")
	}
	msgGen, err := NewMessageGenerator(fd)
	if err != nil {
		t.Fatalf("new generator: %v", err)
	}
	descriptor := fd.Messages().Get(0)
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		fieldType, err := msgGen.getFieldType(field)
		if err != nil {
			t.Fatalf("field %s: %v", field.Name(), err)
		}
		t.Logf("field %s -> %s", field.Name(), fieldType)
	}
}
