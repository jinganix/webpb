package javagen

import (
	"testing"

	"github.com/jinganix/webpb/plugin/internal/testutil"
)

func TestGetJavaPackageFromDescriptor(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_core_codegen")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	if pkg := GetJavaPackage(ctx.TargetDescriptors[0]); pkg == "" {
		t.Fatal("expected java package from descriptor")
	}
}
