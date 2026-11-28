package gogen_test

import (
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/gogen"
	"github.com/jinganix/webpb/plugin/internal/testutil"
)

func TestUnknownExtendsReturnsError(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_errors")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	g := gogen.NewGenerator()
	for _, suffix := range []string{"BadExtends.proto", "ExtendsTargetNotFound.proto", "BadClassOrInterface.proto"} {
		found := false
		for _, fd := range ctx.TargetDescriptors {
			if !strings.HasSuffix(fd.Path(), suffix) {
				continue
			}
			found = true
			if _, err := g.Generate(ctx.Descriptors, fd, gogen.Options{}); err == nil {
				t.Fatalf("%s: expected extends error", suffix)
			} else if !strings.Contains(err.Error(), "extends target not found") {
				t.Fatalf("%s: unexpected error %v", suffix, err)
			}
		}
		if !found {
			t.Fatalf("proto %q not found", suffix)
		}
	}
}

func TestExternalSimpleExtendsAllowed(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_message_extends")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	g := gogen.NewGenerator()
	found := false
	for _, fd := range ctx.TargetDescriptors {
		if !strings.HasSuffix(fd.Path(), "BasicExtends.proto") {
			continue
		}
		found = true
		if _, err := g.Generate(ctx.Descriptors, fd, gogen.Options{}); err != nil {
			t.Fatalf("BasicExtends (external Custom): %v", err)
		}
	}
	if !found {
		t.Fatal("BasicExtends.proto not found")
	}
}
