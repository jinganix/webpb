package core_test

import (
	"strings"
	"testing"

	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/gogen"
	"github.com/jinganix/webpb/plugin/internal/javagen"
	"github.com/jinganix/webpb/plugin/internal/tsgen"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protodesc"
	"google.golang.org/protobuf/reflect/protoreflect"
	"google.golang.org/protobuf/types/descriptorpb"
)

func buildProto3OptionalFile(t *testing.T) protoreflect.FileDescriptor {
	t.Helper()
	fieldOpts := &descriptorpb.FieldOptions{}
	proto.SetExtension(fieldOpts, webpb.E_Opts, &webpb.FieldOpts{
		Opt: &webpb.OptFieldOpts{
			Valid: &webpb.FieldValidation{Required: proto.Bool(true)},
		},
	})
	protoFile := &descriptorpb.FileDescriptorProto{
		Name:    proto.String("test/optional/optional_presence.proto"),
		Package: proto.String("test.optional"),
		Syntax:  proto.String("proto3"),
		Options: func() *descriptorpb.FileOptions {
			opts := &descriptorpb.FileOptions{JavaPackage: proto.String("test.optional")}
			proto.SetExtension(opts, webpb.E_FOpts, &webpb.FileOpts{
				Java: &webpb.JavaFileOpts{Import: []string{"jakarta.validation.constraints.NotNull"}},
			})
			return opts
		}(),
		EnumType: []*descriptorpb.EnumDescriptorProto{
			{
				Name: proto.String("S"),
				Value: []*descriptorpb.EnumValueDescriptorProto{
					{Name: proto.String("S_UNSPECIFIED"), Number: proto.Int32(0)},
					{Name: proto.String("S_A"), Number: proto.Int32(1)},
				},
			},
		},
		MessageType: []*descriptorpb.DescriptorProto{
			{
				Name: proto.String("M"),
				Field: []*descriptorpb.FieldDescriptorProto{
					{
						Name:           proto.String("status"),
						Number:         proto.Int32(2),
						Label:          descriptorpb.FieldDescriptorProto_LABEL_OPTIONAL.Enum(),
						Type:           descriptorpb.FieldDescriptorProto_TYPE_ENUM.Enum(),
						TypeName:       proto.String(".test.optional.S"),
						JsonName:       proto.String("status"),
						Options:        fieldOpts,
						OneofIndex:     proto.Int32(0),
						Proto3Optional: proto.Bool(true),
					},
				},
				OneofDecl: []*descriptorpb.OneofDescriptorProto{{Name: proto.String("_status")}},
			},
		},
	}
	fd, err := protodesc.NewFile(protoFile, nil)
	if err != nil {
		t.Fatalf("build descriptor: %v", err)
	}
	return fd
}

func TestProto3OptionalFieldHasPresence(t *testing.T) {
	fd := buildProto3OptionalFile(t)
	all := []protoreflect.FileDescriptor{fd}
	msg := fd.Messages().Get(0)
	field := msg.Fields().ByName("status")
	if field == nil {
		t.Fatal("expected status field")
	}
	if !field.HasPresence() {
		t.Fatal("expected proto3 optional field to have presence")
	}
	if !field.HasOptionalKeyword() {
		t.Fatal("expected proto3 optional keyword")
	}
	oneof := field.ContainingOneof()
	if oneof == nil || !oneof.IsSynthetic() {
		t.Fatal("expected synthetic oneof for proto3 optional field")
	}
	found := false
	for _, member := range core.GetMemberFields(all, msg) {
		if string(member.Name()) == "status" {
			found = true
		}
	}
	if !found {
		t.Fatal("expected status in member fields")
	}
	valid := core.GetFieldValidation(field)
	if valid == nil || !valid.GetRequired() {
		t.Fatal("expected required validation on status field")
	}
	rendered := core.RenderJavaFieldValidation(field, valid, core.ResolveJavaValidationMapping(fd))
	joined := strings.Join(rendered, " ")
	if !strings.Contains(joined, "@NotNull") {
		t.Fatalf("expected @NotNull, got %q", joined)
	}
}

func TestProto3OptionalEmittersTreatAsNullable(t *testing.T) {
	fd := buildProto3OptionalFile(t)
	all := []protoreflect.FileDescriptor{fd}

	goFile, err := gogen.NewGenerator().Generate(all, fd, gogen.Options{})
	if err != nil {
		t.Fatalf("go generate: %v", err)
	}
	goSrc := strings.Join(strings.Fields(goFile.Content), " ")
	if !strings.Contains(goSrc, "Status *S") {
		t.Fatalf("expected Go pointer field, got\n%s", goFile.Content)
	}
	if strings.Contains(goSrc, "StatusCase") {
		t.Fatalf("unexpected Go synthetic-oneof case helper\n%s", goFile.Content)
	}

	javaFiles, err := javagen.NewGenerator().Generate(fd, all)
	if err != nil {
		t.Fatalf("java generate: %v", err)
	}
	javaJoined := ""
	for _, content := range javaFiles {
		javaJoined += content + "\n"
	}
	if !strings.Contains(javaJoined, "private S status;") {
		t.Fatalf("expected Java boxed enum field, got\n%s", javaJoined)
	}
	if !strings.Contains(javaJoined, "@NotNull") {
		t.Fatalf("expected Java @NotNull, got\n%s", javaJoined)
	}

	genCtx := tsgen.NewGeneratorContext(all, all)
	tsOut, err := tsgen.NewGenerator().Generate(genCtx, fd)
	if err != nil {
		t.Fatalf("ts generate: %v", err)
	}
	if !strings.Contains(tsOut.MainTS, "status?:") {
		t.Fatalf("expected TS optional field, got\n%s", tsOut.MainTS)
	}
	if !strings.Contains(tsOut.MainTS, "| null") {
		t.Fatalf("expected TS nullable field, got\n%s", tsOut.MainTS)
	}
}
