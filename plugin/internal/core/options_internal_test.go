package core

import (
	"testing"

	"google.golang.org/protobuf/encoding/protowire"
	"google.golang.org/protobuf/proto"
	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
)

func TestParseOptsFromUnknownReturnsDefaultWhenNoMatch(t *testing.T) {
	def := parseOptsFromUnknown(nil, func() *webpb.FileOpts { return &webpb.FileOpts{} }, HasFileJava)
	if def == nil {
		t.Fatal("expected default file opts")
	}
}

func TestParseOptsFromUnknownReadsMatchingBytes(t *testing.T) {
	fileOpts := &webpb.FileOpts{Java: &webpb.JavaFileOpts{Annotation: []string{"@Demo"}}}
	encoded, err := proto.Marshal(fileOpts)
	if err != nil {
		t.Fatalf("marshal: %v", err)
	}
	unknown := protowire.AppendTag(nil, 99999, protowire.BytesType)
	unknown = protowire.AppendBytes(unknown, encoded)
	msg := (&webpb.FileOpts{}).ProtoReflect().New().Interface().(proto.Message)
	msg.ProtoReflect().SetUnknown(unknown)

	got := parseOptsFromUnknown(msg, func() *webpb.FileOpts { return &webpb.FileOpts{} }, func(opts *webpb.FileOpts) bool {
		return opts.GetJava() != nil
	})
	if got.GetJava() == nil || len(got.GetJava().GetAnnotation()) == 0 {
		t.Fatalf("expected parsed java opts, got %#v", got)
	}
}

func TestParseOptsFromUnknownSkipsInvalidAndNonMatchingEntries(t *testing.T) {
	valid := &webpb.FileOpts{Ts: &webpb.TsFileOpts{Import: []string{"./demo"}}}
	validBytes, err := proto.Marshal(valid)
	if err != nil {
		t.Fatalf("marshal valid: %v", err)
	}
	unknown := protowire.AppendTag(nil, 1, protowire.VarintType)
	unknown = protowire.AppendVarint(unknown, 42)
	unknown = protowire.AppendTag(unknown, 2, protowire.BytesType)
	unknown = protowire.AppendBytes(unknown, []byte("not-proto"))
	unknown = protowire.AppendTag(unknown, 3, protowire.BytesType)
	unknown = protowire.AppendBytes(unknown, validBytes)
	msg := (&webpb.FileOpts{}).ProtoReflect().New().Interface().(proto.Message)
	msg.ProtoReflect().SetUnknown(unknown)

	got := parseOptsFromUnknown(msg, func() *webpb.FileOpts { return &webpb.FileOpts{} }, HasFileTs)
	if got.GetTs() == nil || len(got.GetTs().GetImport()) == 0 {
		t.Fatalf("expected parsed ts opts, got %#v", got)
	}
}
