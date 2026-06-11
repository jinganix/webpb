package javagen

import (
	"testing"

	"github.com/jinganix/webpb/plugin/internal/testutil"
	"google.golang.org/protobuf/reflect/protoreflect"
)

func TestRepeatedFieldTypes(t *testing.T) {
	ctx, err := testutil.CreateContext("proto2_generator_options")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	var fd protoreflect.FileDescriptor
	for _, candidate := range ctx.TargetDescriptors {
		if candidate.Path() == "RepeatedFieldTypes.proto" {
			fd = candidate
			break
		}
	}
	if fd == nil {
		t.Fatal("RepeatedFieldTypes.proto not found")
	}
	msgGen, err := NewMessageGenerator(fd)
	if err != nil {
		t.Fatalf("new generator: %v", err)
	}
	descriptor := fd.Messages().Get(0)
	want := map[string]string{
		"as_list":       "List<String>",
		"as_set":        "Set<String>",
		"as_collection": "Collection<String>",
	}
	for i := 0; i < descriptor.Fields().Len(); i++ {
		field := descriptor.Fields().Get(i)
		fieldType, err := msgGen.getFieldType(field)
		if err != nil {
			t.Fatalf("field %s: %v", field.Name(), err)
		}
		if fieldType != want[string(field.Name())] {
			t.Fatalf("field %s: got %q, want %q", field.Name(), fieldType, want[string(field.Name())])
		}
	}
}
