package tsgen_test

import (
	"strings"
	"testing"

	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"github.com/jinganix/webpb/plugin/internal/testutil"
	"github.com/jinganix/webpb/plugin/internal/tsgen"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protodesc"
	"google.golang.org/protobuf/reflect/protoreflect"
	"google.golang.org/protobuf/types/descriptorpb"
)

func generateForProto(t *testing.T, dump, suffix string) (bool, error) {
	t.Helper()
	ctx, err := testutil.CreateContext(dump)
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	genCtx := tsgen.NewGeneratorContext(ctx.Descriptors, ctx.TargetDescriptors)
	g := tsgen.NewGenerator()
	for _, fd := range ctx.TargetDescriptors {
		if !strings.HasSuffix(fd.Path(), suffix) {
			continue
		}
		out, err := g.Generate(genCtx, fd)
		if err != nil {
			return true, err
		}
		if out == nil || out.MainTS == "" {
			return false, nil
		}
		return false, nil
	}
	t.Fatalf("proto %q not found in %s", suffix, dump)
	return false, nil
}

func TestUnknownExtendsReturnsError(t *testing.T) {
	for _, suffix := range []string{"BadExtends.proto", "ExtendsTargetNotFound.proto", "BadClassOrInterface.proto"} {
		errored, err := generateForProto(t, "proto3_errors", suffix)
		if !errored || err == nil {
			t.Fatalf("%s: expected extends error, got errored=%v err=%v", suffix, errored, err)
		}
		if !strings.Contains(err.Error(), "extends target not found") {
			t.Fatalf("%s: unexpected error %v", suffix, err)
		}
	}
}

func TestSubtypeKeysUseFullName(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_message_extends")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	genCtx := tsgen.NewGeneratorContext(ctx.Descriptors, ctx.TargetDescriptors)
	for key := range genCtx.BaseTypes {
		if !strings.Contains(key, ".") {
			t.Fatalf("BaseTypes key %q is not a FullName", key)
		}
	}
	for key := range genCtx.SubTypes {
		if !strings.Contains(key, ".") {
			t.Fatalf("SubTypes key %q is not a FullName", key)
		}
		if _, ok := genCtx.BaseTypes[key]; !ok {
			t.Fatalf("SubTypes key %q has no BaseTypes entry", key)
		}
	}
	fromFiles, err := (&tsgen.FromAliasGenerator{}).Generate(genCtx)
	if err != nil {
		t.Fatalf("from alias: %v", err)
	}
	if len(fromFiles) == 0 {
		t.Fatal("expected from-alias files")
	}
}

func buildShortExtendsFile(t *testing.T) protoreflect.FileDescriptor {
	t.Helper()
	msgOpts := func(opt *webpb.OptMessageOpts) *descriptorpb.MessageOptions {
		mo := &descriptorpb.MessageOptions{}
		proto.SetExtension(mo, webpb.E_MOpts, &webpb.MessageOpts{Opt: opt})
		return mo
	}
	fileProto := &descriptorpb.FileDescriptorProto{
		Name:    proto.String("test/short/short_base.proto"),
		Package: proto.String("test.short"),
		Syntax:  proto.String("proto3"),
		MessageType: []*descriptorpb.DescriptorProto{
			{
				Name:    proto.String("Base"),
				Options: msgOpts(&webpb.OptMessageOpts{SubType: proto.String("type")}),
			},
			{
				Name:    proto.String("SubShort"),
				Options: msgOpts(&webpb.OptMessageOpts{Extends: proto.String("Base"), SubValues: []string{"a"}}),
			},
			{
				Name:    proto.String("SubFull"),
				Options: msgOpts(&webpb.OptMessageOpts{Extends: proto.String("test.short.Base"), SubValues: []string{"b"}}),
			},
		},
	}
	fd, err := protodesc.NewFile(fileProto, nil)
	if err != nil {
		t.Fatalf("build descriptor: %v", err)
	}
	return fd
}

func TestSubtypeShortExtendsKeyConsistency(t *testing.T) {
	fd := buildShortExtendsFile(t)
	all := []protoreflect.FileDescriptor{fd}
	genCtx := tsgen.NewGeneratorContext(all, all)
	// Short "Base" must resolve to the same FullName as the qualified form.
	subs, ok := genCtx.SubTypes["test.short.Base"]
	if !ok {
		t.Fatalf("expected SubTypes key %q, got %v", "test.short.Base", genCtx.SubTypes)
	}
	if len(genCtx.SubTypes) != 1 {
		t.Fatalf("expected short and full extends to coalesce, got %v", genCtx.SubTypes)
	}
	if len(subs) != 2 {
		t.Fatalf("expected 2 subtypes for test.short.Base, got %d", len(subs))
	}
	subFiles, err := (&tsgen.SubTypesGenerator{}).Generate(all, all)
	if err != nil {
		t.Fatalf("generate subtypes: %v", err)
	}
	if len(subFiles) != 1 {
		t.Fatalf("expected 1 Types file, got %v", subFiles)
	}
	content, ok := subFiles["BaseTypes.ts"]
	if !ok {
		t.Fatalf("expected BaseTypes.ts, got keys %v", keysOf(subFiles))
	}
	if !strings.Contains(content, "BaseTypes") {
		t.Fatalf("expected BaseTypes union, got\n%s", content)
	}
	for k := range subFiles {
		base := strings.TrimSuffix(k, "Types.ts")
		if strings.Contains(base, ".") {
			t.Fatalf("Types filename %q must use simpleTypeName, not FullName", k)
		}
	}
}

func keysOf(m map[string]string) []string {
	out := make([]string, 0, len(m))
	for k := range m {
		out = append(out, k)
	}
	return out
}
