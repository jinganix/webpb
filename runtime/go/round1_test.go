package webpb_test

import (
	"testing"

	webpb "github.com/jinganix/webpb/runtime/go"
)

type IntKeyMap struct {
	Counts map[int32]string `webpb:"a" json:"a"`
}

func (IntKeyMap) WebpbClass() string   { return "IntKeyMap" }
func (IntKeyMap) WebpbContext() string { return "" }
func (IntKeyMap) WebpbMethod() string  { return "" }
func (IntKeyMap) WebpbPath() string    { return "" }

type MsgMap struct {
	Items map[string]*Nested `webpb:"m" json:"m"`
}

func (MsgMap) WebpbClass() string   { return "MsgMap" }
func (MsgMap) WebpbContext() string { return "" }
func (MsgMap) WebpbMethod() string  { return "" }
func (MsgMap) WebpbPath() string    { return "" }

func TestPopulateIntMapKey(t *testing.T) {
	msg := &IntKeyMap{}
	if err := webpb.Populate(map[string]any{"a": map[string]any{"1": "x", "2": "y"}}, msg); err != nil {
		t.Fatalf("populate: %v", err)
	}
	if msg.Counts[1] != "x" || msg.Counts[2] != "y" {
		t.Fatalf("unexpected map %+v", msg.Counts)
	}
}

func TestPopulateIntMapKeyInvalidReturnsError(t *testing.T) {
	msg := &IntKeyMap{}
	defer func() {
		if r := recover(); r != nil {
			t.Fatalf("panicked: %v", r)
		}
	}()
	if err := webpb.Populate(map[string]any{"a": map[string]any{"bad": "x"}}, msg); err == nil {
		t.Fatal("expected error for bad int key")
	}
}

func TestQuerySorted(t *testing.T) {
	// Keep parity with runtime/ts/test/round1.test.ts: same params same URL.
	got := webpb.Query("?", map[string]any{"b": "2", "a": "1", "c": "3"})
	want := "?a=1&b=2&c=3"
	if got != want {
		t.Fatalf("query mismatch\nwant %s\ngot  %s", want, got)
	}
	got = webpb.Query("", map[string]any{"sort": map[string]string{"b": "asc", "a": "desc"}})
	// inner keys sorted: a before b
	want = "sort=a%2Cdesc&sort=b%2Casc"
	if got != want {
		t.Fatalf("inner sort mismatch\nwant %s\ngot  %s", want, got)
	}
}

func TestQueryGenericSliceParity(t *testing.T) {
	// Cross-language lock: same as TS query("?", { ids: ["1", "2"] }).
	got := webpb.Query("?", map[string]any{"ids": []any{"1", "2"}})
	want := "?ids=1%2C2"
	if got != want {
		t.Fatalf("generic slice mismatch\nwant %s\ngot  %s", want, got)
	}
	got = webpb.Query("?", map[string]any{"ids": []int{1, 2}})
	if got != want {
		t.Fatalf("int slice mismatch\nwant %s\ngot  %s", want, got)
	}
	got = webpb.Query("?", map[string]any{"ids": [2]string{"1", "2"}})
	if got != want {
		t.Fatalf("array mismatch\nwant %s\ngot  %s", want, got)
	}
	if webpb.Query("?", map[string]any{"ids": []any{}}) != "" {
		t.Fatal("expected empty query for empty generic slice")
	}
}

func TestToAliasNonStringKeyUsesStringMap(t *testing.T) {
	msg := &IntKeyMap{Counts: map[int32]string{2: "y", 1: "x"}}
	alias := webpb.ToAlias(msg)
	inner, ok := alias["a"].(map[string]any)
	if !ok {
		t.Fatalf("expected map[string]any, got %T (%v)", alias["a"], alias["a"])
	}
	if inner["1"] != "x" || inner["2"] != "y" {
		t.Fatalf("unexpected int-key map %+v", inner)
	}
}

func TestQueryGenericMapParity(t *testing.T) {
	// M1 lock with runtime/ts/test/round1.test.ts: same params same URL.
	// TS: query("?", { sort: { a: "desc" } }) === "?sort=a%2Cdesc".
	got := webpb.Query("?", map[string]any{"sort": map[string]any{"a": "desc"}})
	want := "?sort=a%2Cdesc"
	if got != want {
		t.Fatalf("generic map mismatch\nwant %s\ngot  %s", want, got)
	}
	// Arbitrary value Sprint: int values encode like TS String(value).
	got = webpb.Query("", map[string]any{"sort": map[string]int{"a": 1}})
	want = "sort=a%2C1"
	if got != want {
		t.Fatalf("generic int map mismatch\nwant %s\ngot  %s", want, got)
	}
	// Empty (""/nil) inner values only push key, like TS null/"" branch.
	got = webpb.Query("", map[string]any{"sort": map[string]any{"a": "", "b": nil}})
	want = "sort=a&sort=b"
	if got != want {
		t.Fatalf("generic empty map mismatch\nwant %s\ngot  %s", want, got)
	}
}

func TestQuerySkipsFuncChan(t *testing.T) {
	// S1 lock with TS: function params are omitted.
	got := webpb.Query("", map[string]any{
		"a":  1,
		"fn": func() {},
		"ch": make(chan int),
		"b":  2,
	})
	want := "a=1&b=2"
	if got != want {
		t.Fatalf("func/chan skip mismatch\nwant %s\ngot  %s", want, got)
	}
}

func TestToAliasMapMessageParity(t *testing.T) {
	// S2 lock with TS map<string,Message>: nested messages inside maps recurse.
	msg := &MsgMap{Items: map[string]*Nested{"k": {Note: "n"}}}
	alias := webpb.ToAlias(msg)
	inner, ok := alias["m"].(map[string]any)
	if !ok {
		t.Fatalf("expected map[string]any, got %T (%v)", alias["m"], alias["m"])
	}
	if len(inner) != 1 {
		t.Fatalf("unexpected map len %+v", inner)
	}
	nested, ok := inner["k"].(map[string]any)
	if !ok {
		t.Fatalf("expected nested map[string]any, got %T (%v)", inner["k"], inner["k"])
	}
	if nested["a"] != "n" {
		t.Fatalf("unexpected nested message %+v", nested)
	}
}
