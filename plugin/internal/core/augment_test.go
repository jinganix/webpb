package core_test

import (
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/testutil"
)

func TestFindAugmentMessagesMergesIntoOpenTarget(t *testing.T) {
	ctx, err := testutil.CreateContext("proto2_message_augment")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	user := core.ResolveMessage(ctx.Descriptors, "UserAugmentProto.UserPb")
	if user == nil {
		t.Fatal("UserPb not found")
	}
	augments := core.FindAugmentMessages(ctx.Descriptors, user)
	if len(augments) != 1 {
		t.Fatalf("augments = %d, want 1", len(augments))
	}
	if string(augments[0].Name()) != "AugmentUserPb" {
		t.Fatalf("augment = %s, want AugmentUserPb", augments[0].Name())
	}
	fields := core.GetMemberFields(ctx.Descriptors, user)
	names := map[string]struct{}{}
	for _, field := range fields {
		names[string(field.Name())] = struct{}{}
	}
	for _, want := range []string{"id", "username", "staffId", "displayName"} {
		if _, ok := names[want]; !ok {
			t.Fatalf("missing field %s in %v", want, names)
		}
	}
}

func TestCheckAugmentMessageRejectsClosedTarget(t *testing.T) {
	ctx, err := testutil.CreateContext("proto2_errors")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	msg := core.ResolveMessage(ctx.Descriptors, "AugmentClosedUserPb")
	if msg == nil {
		t.Fatal("AugmentClosedUserPb not found")
	}
	err = core.CheckAugmentMessage(ctx.Descriptors, msg)
	if err == nil || !strings.Contains(err.Error(), "is not open") {
		t.Fatalf("error = %v, want not open", err)
	}
}

func TestCheckAugmentMessageRejectsFieldReserveViolation(t *testing.T) {
	ctx, err := testutil.CreateContext("proto2_errors")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	msg := core.ResolveMessage(ctx.Descriptors, "AugmentReservedUserPb")
	if msg == nil {
		t.Fatal("AugmentReservedUserPb not found")
	}
	err = core.CheckAugmentMessage(ctx.Descriptors, msg)
	if err == nil || !strings.Contains(err.Error(), "field_reserve") {
		t.Fatalf("error = %v, want field_reserve", err)
	}
}
