package core_test

import (
	"testing"

	"github.com/jinganix/webpb/plugin/internal/commons"
	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/testutil"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protoreflect"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
	"google.golang.org/protobuf/types/descriptorpb"
)

func TestResolveHelpers(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	descriptors := ctx.Descriptors
	if core.ResolveMessage(descriptors, "Test") == nil {
		t.Fatal("expected Test message")
	}
	if core.ResolveEnum(descriptors, "Enum") == nil {
		t.Fatal("expected Enum")
	}
	if core.ResolveEnum(descriptors, "NotExists") != nil {
		t.Fatal("expected nil for missing enum")
	}
	if core.ResolveFile(descriptors, "CoreMessages.proto") == nil {
		t.Fatal("expected CoreMessages.proto")
	}
}

func TestFieldTypeNames(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	msg := core.ResolveMessage(ctx.Descriptors, "Test")
	if msg == nil {
		t.Fatal("missing Test message")
	}
	primitive := msg.Fields().ByName("test1")
	if primitive == nil {
		t.Fatal("missing test1 field")
	}
	if core.GetFieldTypeFullName(primitive) == "" {
		t.Fatal("expected primitive type name")
	}
	messageField := msg.Fields().ByName("test2")
	if messageField == nil {
		t.Fatal("missing test2 field")
	}
	_ = core.GetFieldTypePackage(messageField)
	if core.GetFieldTypeSimpleName(messageField) == "" {
		t.Fatal("expected message field simple name")
	}
	enumField := msg.Fields().ByName("test3")
	if enumField == nil {
		t.Fatal("missing test3 field")
	}
	_ = core.GetFieldTypePackage(enumField)
	if core.GetFieldTypeSimpleName(enumField) == "" {
		t.Fatal("expected enum field simple name")
	}
	nestedField := msg.Fields().ByName("test4")
	if nestedField == nil {
		t.Fatal("missing test4 field")
	}
	if core.GetFieldTypePackage(nestedField) == "" {
		t.Fatal("expected nested message package")
	}
	mapField := msg.Fields().ByName("test5")
	if mapField != nil && core.IsMessage(mapField) && mapField.IsMap() {
		_ = core.GetMapKeyDescriptor(mapField)
		_ = core.GetMapValueDescriptor(mapField)
	}
}

func TestResolveNestedAndTopLevelTypes(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	fd := ctx.TargetDescriptors[0]
	types := core.ResolveTopLevelTypes(fd)
	if len(types) == 0 {
		t.Fatal("expected top-level types")
	}
	for _, descriptor := range types {
		if nested := core.ResolveNestedTypes(descriptor); len(nested) == 0 {
			t.Fatalf("expected nested types for %v", descriptor.FullName())
		}
	}
}

func TestValidation(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	msg := core.ResolveMessage(ctx.Descriptors, "Test")
	group := commons.Of("/{test1}")
	core.Validation(group, msg, ctx.Descriptors)

	test2 := core.ResolveMessage(ctx.Descriptors, "Test2")
	nestedGroup := commons.Of("/{test3.test1}")
	core.Validation(nestedGroup, test2, ctx.Descriptors)

	defer func() {
		if recover() == nil {
			t.Fatal("expected panic for invalid accessor")
		}
	}()
	core.Validation(commons.Of("/{missing}"), msg, ctx.Descriptors)
}

func TestResolveDescriptorFile(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	root := ctx.TargetDescriptors[0]
	msg := root.Messages().Get(0)
	if core.ResolveDescriptorFile(root, msg) == nil {
		t.Fatal("expected file for message descriptor")
	}
}

func TestIsEnumAndMessage(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	msg := core.ResolveMessage(ctx.Descriptors, "Test")
	field := msg.Fields().Get(0)
	if !core.IsMessage(field) && !core.IsEnum(field) {
		if field.Kind() != protoreflect.Int32Kind {
			t.Fatalf("unexpected field kind %v", field.Kind())
		}
	}
}

func TestGetWebpbFileOptsFromMultipleDumps(t *testing.T) {
	for _, dump := range []string{
		"proto2_core_codegen",
		"proto2_generator_options",
		"proto3_core_codegen",
		"proto3_generator_options",
		"proto3_auto_alias",
		"proto3_message_extends",
		"proto3_enumeration",
		"proto3_imports",
	} {
		dump := dump
		t.Run(dump, func(t *testing.T) {
			ctx, err := testutil.CreateContext(dump)
			if err != nil {
				t.Fatalf("create context: %v", err)
			}
			for _, fd := range ctx.Descriptors {
				_ = core.GetWebpbFileOpts(fd, core.HasFileJava)
				_ = core.GetWebpbFileOpts(fd, core.HasFileTs)
				_ = core.GetFileOpts(fd, core.HasFileJava)
				_ = core.GetFileOpts(fd, core.HasFileTs)
				for i := 0; i < fd.Messages().Len(); i++ {
					msg := fd.Messages().Get(i)
					_ = core.GetMessageOpts(msg, core.HasMessageJava)
					_ = core.GetMessageOpts(msg, core.HasMessageTs)
					for j := 0; j < msg.Fields().Len(); j++ {
						field := msg.Fields().Get(j)
						_ = core.GetFieldOpts(field, core.HasFieldJava)
						_ = core.GetFieldOpts(field, core.HasFieldTs)
					}
				}
				for i := 0; i < fd.Enums().Len(); i++ {
					enum := fd.Enums().Get(i)
					_ = core.GetEnumOpts(enum, core.HasEnumJava)
					_ = core.GetEnumOpts(enum, core.HasEnumTs)
					for j := 0; j < enum.Values().Len(); j++ {
						_ = core.GetEnumValueOpts(enum.Values().Get(j), core.HasEnumValueOpt)
					}
				}
			}
		})
	}
}

func TestGetWebpbFileOpts(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_generator_options")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	fd := ctx.TargetDescriptors[0]
	if opts := core.GetWebpbFileOpts(fd, core.HasFileJava); opts == nil {
		t.Fatal("expected webpb file opts")
	}
	if fileOpts := core.GetFileOpts(fd, core.HasFileJava); fileOpts == nil {
		t.Fatal("expected file opts")
	}
	if tsOpts := core.GetFileOpts(fd, core.HasFileTs); tsOpts == nil {
		t.Fatal("expected ts file opts")
	}
	for i := 0; i < fd.Messages().Len(); i++ {
		msg := fd.Messages().Get(i)
		_ = core.GetMessageOpts(msg, core.HasMessageJava)
		for j := 0; j < msg.Fields().Len(); j++ {
			_ = core.GetFieldOpts(msg.Fields().Get(j), core.HasFieldJava)
		}
	}
	for i := 0; i < fd.Enums().Len(); i++ {
		enum := fd.Enums().Get(i)
		_ = core.GetEnumOpts(enum, core.HasEnumJava)
		for j := 0; j < enum.Values().Len(); j++ {
			_ = core.GetEnumValueOpts(enum.Values().Get(j), core.HasEnumValueOpt)
		}
	}
}

func TestCheckDuplicatedFields(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	msg := core.ResolveMessage(ctx.Descriptors, "Test")
	if msg == nil {
		t.Fatal("missing Test message")
	}
	if fields := core.GetAllFields(msg); len(fields) == 0 {
		t.Fatal("expected fields")
	}
	core.CheckDuplicatedFields(msg)
}

func TestValidationForAllMessagePaths(t *testing.T) {
	for _, dump := range []string{
		"proto2_core_codegen",
		"proto2_message_extends",
		"proto3_core_codegen",
		"proto3_message_extends",
		"proto3_imports",
	} {
		dump := dump
		t.Run(dump, func(t *testing.T) {
			ctx, err := testutil.CreateContext(dump)
			if err != nil {
				t.Fatalf("create context: %v", err)
			}
			for _, fd := range ctx.Descriptors {
				for i := 0; i < fd.Messages().Len(); i++ {
					validateMessagePaths(t, ctx, fd.Messages().Get(i))
				}
			}
		})
	}
}

func validateMessagePaths(t *testing.T, ctx *core.RequestContext, msg protoreflect.MessageDescriptor) {
	t.Helper()
	opts := core.GetMessageOpts(msg, core.HasMessageOpt)
	if opts != nil && opts.GetOpt().GetPath() != "" {
		core.Validation(commons.Of(opts.GetOpt().GetPath()), msg, ctx.Descriptors)
	}
	for i := 0; i < msg.Messages().Len(); i++ {
		validateMessagePaths(t, ctx, msg.Messages().Get(i))
	}
}

func TestFindMessageDescriptor(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	msg := core.ResolveMessage(ctx.Descriptors, "Test")
	if msg == nil {
		t.Fatal("missing Test message")
	}
	if core.FindMessageDescriptor(ctx.Descriptors, msg.FullName()) == nil {
		t.Fatal("expected to find Test descriptor")
	}
}

func TestResolveEnumValueByQualifiedName(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	if core.ResolveEnumValue(ctx.Descriptors, "Test5.test5_1") == nil {
		t.Fatal("expected enum value by qualified name")
	}
}

func TestNewRequestContextReturnsErrorWhenDescriptorBuildFails(t *testing.T) {
	req := &pluginpb.CodeGeneratorRequest{
		ProtoFile: []*descriptorpb.FileDescriptorProto{
			{
				Name: proto.String("bad.proto"),
				MessageType: []*descriptorpb.DescriptorProto{
					{
						Name: proto.String("Msg"),
						Field: []*descriptorpb.FieldDescriptorProto{
							{
								Name:   proto.String("child"),
								Number: proto.Int32(1),
								Type:   descriptorpb.FieldDescriptorProto_TYPE_MESSAGE.Enum(),
							},
						},
					},
				},
			},
		},
	}
	if _, err := core.NewRequestContext(req); err == nil {
		t.Fatal("expected error for invalid message field")
	}
}

func TestBuildDescriptorsNormalizesPaths(t *testing.T) {
	req := &pluginpb.CodeGeneratorRequest{
		ProtoFile: []*descriptorpb.FileDescriptorProto{
			{Name: proto.String(`dir\file.proto`), MessageType: []*descriptorpb.DescriptorProto{
				{Name: proto.String("Msg")},
			}},
		},
		FileToGenerate: []string{`dir\file.proto`},
	}
	all, targets, err := core.BuildDescriptors(req)
	if err != nil {
		t.Fatalf("BuildDescriptors: %v", err)
	}
	if len(all) != 1 || len(targets) != 1 {
		t.Fatalf("expected one file descriptor, got %d / %d", len(all), len(targets))
	}
}
