package core_test

import (
	"testing"

	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/testutil"
)

func TestGetAutoAliasesUsesFieldNumber(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_auto_alias")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	level1 := core.ResolveMessage(ctx.Descriptors, "ExtendsProto.Level1")
	if level1 == nil {
		t.Fatal("Level1 not found")
	}
	aliases := core.GetAutoAliases(level1)
	if aliases["foo_3"] != "c" || aliases["foo_4"] != "d" {
		t.Fatalf("Level1 aliases = %v, want foo_3=c foo_4=d", aliases)
	}

	level2 := core.ResolveMessage(ctx.Descriptors, "ExtendsProto.Level2")
	aliases = core.GetAutoAliases(level2)
	if aliases["foo_2"] != "b" {
		t.Fatalf("Level2 alias = %q, want b", aliases["foo_2"])
	}

	level3 := core.ResolveMessage(ctx.Descriptors, "ExtendsProto.Level3")
	aliases = core.GetAutoAliases(level3)
	if aliases["foo_1"] != "a" {
		t.Fatalf("Level3 alias = %q, want a", aliases["foo_1"])
	}
}

func TestGetAutoAliasesAliasReserve(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_auto_alias")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	child := core.ResolveMessage(ctx.Descriptors, "AliasReserveChild")
	if child == nil {
		t.Fatal("AliasReserveChild not found")
	}
	if err := core.CheckAliasReserve(child); err != nil {
		t.Fatalf("check alias reserve: %v", err)
	}
	aliases := core.GetAutoAliases(child)
	if aliases["foo_2"] != core.ToBase52(5) {
		t.Fatalf("AliasReserveChild alias = %q, want %q", aliases["foo_2"], core.ToBase52(5))
	}
}

func TestCheckAliasReserveRejectsInvalidReserve(t *testing.T) {
	ctx, err := testutil.CreateContext("proto3_errors")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	msg := core.ResolveMessage(ctx.Descriptors, "InvalidAliasReserve")
	if msg == nil {
		t.Fatal("InvalidAliasReserve not found")
	}
	if err := core.CheckAliasReserve(msg); err == nil {
		t.Fatal("expected alias reserve validation error")
	}
}
