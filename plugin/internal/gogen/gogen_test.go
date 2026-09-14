package gogen_test

import (
	"go/format"
	"strings"
	"testing"

	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"github.com/jinganix/webpb/plugin/internal/gogen"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protodesc"
	"google.golang.org/protobuf/reflect/protoreflect"
	"google.golang.org/protobuf/types/descriptorpb"
)

func normalizeSpaces(value string) string {
	return strings.Join(strings.Fields(value), " ")
}

func buildFile(t *testing.T) protoreflect.FileDescriptor {
	t.Helper()
	label := func(value string) *string { return proto.String(value) }
	field := func(name string, number int32, typ descriptorpb.FieldDescriptorProto_Type, labelValue descriptorpb.FieldDescriptorProto_Label) *descriptorpb.FieldDescriptorProto {
		return &descriptorpb.FieldDescriptorProto{
			Label:    labelValue.Enum(),
			Name:     label(name),
			Number:   proto.Int32(number),
			Type:     typ.Enum(),
			JsonName: label(name),
		}
	}
	protoFile := &descriptorpb.FileDescriptorProto{
		Name:    proto.String("ops/demo/demo.proto"),
		Package: proto.String("ops.demo"),
		Syntax:  proto.String("proto3"),
		EnumType: []*descriptorpb.EnumDescriptorProto{
			{
				Name: proto.String("Color"),
				Value: []*descriptorpb.EnumValueDescriptorProto{
					{Name: proto.String("RED"), Number: proto.Int32(0)},
					{Name: proto.String("GREEN"), Number: proto.Int32(1)},
				},
			},
		},
		MessageType: []*descriptorpb.DescriptorProto{
			{
				Name: proto.String("Demo"),
				Field: []*descriptorpb.FieldDescriptorProto{
					field("id", 1, descriptorpb.FieldDescriptorProto_TYPE_STRING, descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL),
					field("count", 2, descriptorpb.FieldDescriptorProto_TYPE_INT32, descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL),
					field("tags", 3, descriptorpb.FieldDescriptorProto_TYPE_STRING, descriptorpb.FieldDescriptorProto_LABEL_REPEATED),
					field("blob", 4, descriptorpb.FieldDescriptorProto_TYPE_BYTES, descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL),
				},
			},
		},
	}
	fd, err := protodesc.NewFile(protoFile, nil)
	if err != nil {
		t.Fatalf("build descriptor: %v", err)
	}
	return fd
}

func TestGenerate(t *testing.T) {
	fd := buildFile(t)
	generator := gogen.NewGenerator()
	file, err := generator.Generate([]protoreflect.FileDescriptor{fd}, fd, gogen.Options{Package: "demopb"})
	if err != nil {
		t.Fatalf("generate: %v", err)
	}
	if file.Name != "demopb/demo.pb.go" {
		t.Fatalf("unexpected file name %q", file.Name)
	}
	if _, err := format.Source([]byte(file.Content)); err != nil {
		t.Fatalf("generated source is not valid Go: %v\n%s", err, file.Content)
	}
	normalized := normalizeSpaces(file.Content)
	for _, expected := range []string{
		"package demopb",
		"type Color int32",
		"Color_RED Color = 0",
		"type Demo struct",
		"Tags []string",
		"Blob []byte",
		"return DemoClass",
	} {
		if !strings.Contains(normalized, normalizeSpaces(expected)) {
			t.Fatalf("generated source missing %q\n%s", expected, file.Content)
		}
	}
}

func TestGenerateUsesFieldNameTagsByDefault(t *testing.T) {
	fd := buildFile(t)
	file, err := gogen.NewGenerator().Generate([]protoreflect.FileDescriptor{fd}, fd, gogen.Options{})
	if err != nil {
		t.Fatalf("generate: %v", err)
	}
	if !strings.Contains(file.Content, "`webpb:\"id\" json:\"id\"`") {
		t.Fatalf("expected field-name webpb tags by default\n%s", file.Content)
	}
	if !strings.Contains(file.Content, "package webpbpb") {
		t.Fatalf("expected default package\n%s", file.Content)
	}
}

func buildProto2File(t *testing.T) protoreflect.FileDescriptor {
	t.Helper()
	protoFile := &descriptorpb.FileDescriptorProto{
		Name:    proto.String("ops/demo2/demo2.proto"),
		Package: proto.String("ops.demo2"),
		Syntax:  proto.String("proto2"),
		EnumType: []*descriptorpb.EnumDescriptorProto{
			{
				Name: proto.String("Color"),
				Value: []*descriptorpb.EnumValueDescriptorProto{
					{Name: proto.String("RED"), Number: proto.Int32(0)},
				},
			},
		},
		MessageType: []*descriptorpb.DescriptorProto{
			{
				Name: proto.String("Demo2"),
				Field: []*descriptorpb.FieldDescriptorProto{
					{
						Label:    descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL.Enum(),
						Name:     proto.String("name"),
						Number:   proto.Int32(1),
						Type:     descriptorpb.FieldDescriptorProto_TYPE_STRING.Enum(),
						JsonName: proto.String("name"),
					},
					{
						Label:    descriptorpb.FieldDescriptorProto_LABEL_REQUIRED.Enum(),
						Name:     proto.String("id"),
						Number:   proto.Int32(2),
						Type:     descriptorpb.FieldDescriptorProto_TYPE_INT32.Enum(),
						JsonName: proto.String("id"),
					},
					{
						Label:      descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL.Enum(),
						Name:       proto.String("left"),
						Number:     proto.Int32(3),
						OneofIndex: proto.Int32(0),
						Type:       descriptorpb.FieldDescriptorProto_TYPE_STRING.Enum(),
						JsonName:   proto.String("left"),
					},
					{
						Label:      descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL.Enum(),
						Name:       proto.String("right"),
						Number:     proto.Int32(4),
						OneofIndex: proto.Int32(0),
						Type:       descriptorpb.FieldDescriptorProto_TYPE_INT32.Enum(),
						JsonName:   proto.String("right"),
					},
					{
						Label:    descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL.Enum(),
						Name:     proto.String("blob"),
						Number:   proto.Int32(5),
						Type:     descriptorpb.FieldDescriptorProto_TYPE_BYTES.Enum(),
						JsonName: proto.String("blob"),
					},
				},
				OneofDecl: []*descriptorpb.OneofDescriptorProto{{Name: proto.String("choice")}},
				Options: func() *descriptorpb.MessageOptions {
					options := &descriptorpb.MessageOptions{}
					proto.SetExtension(options, webpb.E_MOpts, &webpb.MessageOpts{
						Go: &webpb.GoMessageOpts{ImplementsInterface: proto.String("Greeter")},
					})
					return options
				}(),
			},
		},
	}
	fileOptions := &descriptorpb.FileOptions{}
	proto.SetExtension(fileOptions, webpb.E_FOpts, &webpb.FileOpts{
		Go: &webpb.GoFileOpts{EnumAutoAlias: proto.Bool(true)},
	})
	protoFile.Options = fileOptions

	fd, err := protodesc.NewFile(protoFile, nil)
	if err != nil {
		t.Fatalf("build proto2 descriptor: %v", err)
	}
	return fd
}

func TestGeneratePresenceAndOneof(t *testing.T) {
	fd := buildProto2File(t)
	file, err := gogen.NewGenerator().Generate([]protoreflect.FileDescriptor{fd}, fd, gogen.Options{})
	if err != nil {
		t.Fatalf("generate: %v", err)
	}
	normalized := normalizeSpaces(file.Content)
	for _, expected := range []string{
		"Name *string",
		"Id *int32",
		"Left *string",
		"Right *int32",
		"Blob []byte",
		"func (m *Demo2) ChoiceCase() string",
		`return "left"`,
	} {
		if !strings.Contains(normalized, normalizeSpaces(expected)) {
			t.Fatalf("generated source missing %q\n%s", expected, file.Content)
		}
	}
}

func TestGenerateGlobalAutoAlias(t *testing.T) {
	fd := buildFile(t)
	file, err := gogen.NewGenerator().Generate(
		[]protoreflect.FileDescriptor{fd},
		fd,
		gogen.Options{AutoAlias: true},
	)
	if err != nil {
		t.Fatalf("generate: %v", err)
	}
	if !strings.Contains(file.Content, "`webpb:\"a\" json:\"a\"`") {
		t.Fatalf("expected alias tags with global auto_alias\n%s", file.Content)
	}
}

func TestGenerateGlobalInt64AsString(t *testing.T) {
	protoFile := &descriptorpb.FileDescriptorProto{
		Name:    proto.String("ops/demo3/demo3.proto"),
		Package: proto.String("ops.demo3"),
		Syntax:  proto.String("proto3"),
		MessageType: []*descriptorpb.DescriptorProto{{
			Name: proto.String("Demo3"),
			Field: []*descriptorpb.FieldDescriptorProto{{
				Label:    descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL.Enum(),
				Name:     proto.String("count"),
				Number:   proto.Int32(1),
				Type:     descriptorpb.FieldDescriptorProto_TYPE_INT64.Enum(),
				JsonName: proto.String("count"),
			}},
		}},
	}
	fd, err := protodesc.NewFile(protoFile, nil)
	if err != nil {
		t.Fatalf("build descriptor: %v", err)
	}
	file, err := gogen.NewGenerator().Generate(
		[]protoreflect.FileDescriptor{fd},
		fd,
		gogen.Options{Int64AsString: true},
	)
	if err != nil {
		t.Fatalf("generate: %v", err)
	}
	if !strings.Contains(normalizeSpaces(file.Content), "Count string") {
		t.Fatalf("expected string int64 field\n%s", file.Content)
	}
}

func TestGenerateEnumAliasAndInterface(t *testing.T) {
	fd := buildProto2File(t)
	file, err := gogen.NewGenerator().Generate([]protoreflect.FileDescriptor{fd}, fd, gogen.Options{})
	if err != nil {
		t.Fatalf("generate: %v", err)
	}
	normalized := normalizeSpaces(file.Content)
	for _, expected := range []string{
		"type ConstColor int32",
		"ConstColor_RED ConstColor = 0",
		"func (x Color) Alias() ConstColor",
		"func (x ConstColor) Value() Color",
		"type Greeter interface",
		"var _ Greeter = (*Demo2)(nil)",
	} {
		if !strings.Contains(normalized, normalizeSpaces(expected)) {
			t.Fatalf("generated source missing %q\n%s", expected, file.Content)
		}
	}
}

func TestHelpersFile(t *testing.T) {
	file := gogen.NewGenerator().HelpersFile("demopb")
	if file.Name != "demopb/webpb_helpers.pb.go" {
		t.Fatalf("unexpected helper file name %q", file.Name)
	}
	if !strings.Contains(file.Content, "func webpbPtr[T any](value T) *T") {
		t.Fatalf("missing helper\n%s", file.Content)
	}
	if _, err := format.Source([]byte(file.Content)); err != nil {
		t.Fatalf("helper is not valid Go: %v", err)
	}
}

func TestGenerateSkipsAugmentMessages(t *testing.T) {
	fd := buildFile(t)
	file, err := gogen.NewGenerator().Generate([]protoreflect.FileDescriptor{fd}, fd, gogen.Options{})
	if err != nil {
		t.Fatalf("generate: %v", err)
	}
	if strings.Contains(file.Content, "type Demo_Broken") {
		t.Fatalf("nested types should be flattened, got\n%s", file.Content)
	}
}
