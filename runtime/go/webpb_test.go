package webpb_test

import (
	"reflect"
	"testing"

	webpb "github.com/jinganix/webpb/runtime/go"
)

type Color int32

const (
	ColorRED   Color = 0
	ColorGREEN Color = 1
)

type Nested struct {
	Note string `webpb:"a" json:"a"`
}

func (Nested) WebpbClass() string   { return "Nested" }
func (Nested) WebpbContext() string { return "" }
func (Nested) WebpbMethod() string  { return "" }
func (Nested) WebpbPath() string    { return "" }

type Demo struct {
	Active   bool              `webpb:"c" json:"c"`
	Blob     []byte            `webpb:"e" json:"e"`
	Children []*Nested         `webpb:"i" json:"i"`
	Color    Color             `webpb:"f" json:"f"`
	Count    int32             `webpb:"b" json:"b"`
	Id       string            `webpb:"a" json:"a"`
	Labels   map[string]string `webpb:"h" json:"h"`
	Nested   *Nested           `webpb:"g" json:"g"`
	Tags     []string          `webpb:"d" json:"d"`
}

func (Demo) WebpbClass() string   { return "Demo" }
func (Demo) WebpbContext() string { return "ctx" }
func (Demo) WebpbMethod() string  { return "POST" }
func (Demo) WebpbPath() string    { return "/demo" }

func newDemo() *Demo {
	return &Demo{
		Active: true,
		Blob:   []byte{1, 2, 3},
		Color:  ColorGREEN,
		Count:  7,
		Id:     "x",
		Labels: map[string]string{"k": "v"},
		Nested: &Nested{Note: "n"},
		Tags:   []string{"t1", "t2"},
	}
}

func TestOf(t *testing.T) {
	meta := webpb.Of(newDemo())
	if meta != (webpb.Meta{Class: "Demo", Context: "ctx", Method: "POST", Path: "/demo"}) {
		t.Fatalf("unexpected meta %+v", meta)
	}
}

func TestToAlias(t *testing.T) {
	alias := webpb.ToAlias(newDemo())
	if alias["a"] != "x" || alias["b"] != int32(7) || alias["c"] != true {
		t.Fatalf("unexpected scalars %+v", alias)
	}
	if !reflect.DeepEqual(alias["d"], []any{"t1", "t2"}) {
		t.Fatalf("unexpected list %+v", alias["d"])
	}
	if !reflect.DeepEqual(alias["e"], []byte{1, 2, 3}) {
		t.Fatalf("unexpected bytes %+v", alias["e"])
	}
	if alias["f"] != ColorGREEN {
		t.Fatalf("unexpected enum %+v", alias["f"])
	}
	if !reflect.DeepEqual(alias["g"], map[string]any{"a": "n"}) {
		t.Fatalf("unexpected nested %+v", alias["g"])
	}
	if !reflect.DeepEqual(alias["h"], map[string]any{"k": "v"}) {
		t.Fatalf("unexpected map %+v", alias["h"])
	}
}

func TestRoundTrip(t *testing.T) {
	alias := webpb.ToAlias(newDemo())
	restored := &Demo{}
	if err := webpb.Populate(alias, restored); err != nil {
		t.Fatalf("populate: %v", err)
	}
	if !reflect.DeepEqual(newDemo(), restored) {
		t.Fatalf("round trip mismatch\nwant %+v\ngot  %+v", newDemo(), restored)
	}
}

func TestPopulateCoercesWireTypes(t *testing.T) {
	restored := &Demo{}
	err := webpb.Populate(map[string]any{
		"a": "id",
		"b": float64(3),
		"d": []any{"one"},
		"e": "AQID",
		"f": int64(1),
		"g": map[string]any{"a": "note"},
		"i": []any{map[string]any{"a": "child"}},
	}, restored)
	if err != nil {
		t.Fatalf("populate: %v", err)
	}
	if restored.Count != 3 || restored.Id != "id" {
		t.Fatalf("unexpected scalars %+v", restored)
	}
	if !reflect.DeepEqual(restored.Tags, []string{"one"}) {
		t.Fatalf("unexpected tags %+v", restored.Tags)
	}
	if !reflect.DeepEqual(restored.Blob, []byte{1, 2, 3}) {
		t.Fatalf("unexpected blob %+v", restored.Blob)
	}
	if restored.Color != ColorGREEN || restored.Nested.Note != "note" {
		t.Fatalf("unexpected enum/nested %+v", restored)
	}
	if len(restored.Children) != 1 || restored.Children[0].Note != "child" {
		t.Fatalf("unexpected children %+v", restored.Children)
	}
}

func TestPopulateRejectsNonPointer(t *testing.T) {
	if err := webpb.Populate(map[string]any{}, Demo{}); err == nil {
		t.Fatal("expected error for non-pointer message")
	}
}

func TestQuery(t *testing.T) {
	got := webpb.Query("?", map[string]any{
		"a":   "b c",
		"ids": []string{"1", "2"},
		"map": map[string]string{"k": "v"},
	})
	want := "?a=b%20c&ids=1%2C2&map=k%2Cv"
	if got != want {
		t.Fatalf("query mismatch\nwant %s\ngot  %s", want, got)
	}
	if webpb.Query("?", map[string]any{}) != "" {
		t.Fatal("expected empty query")
	}
}

func TestGetter(t *testing.T) {
	data := map[string]any{"a": map[string]any{"b": "c"}}
	if webpb.Getter(data, "a.b") != "c" {
		t.Fatal("unexpected getter result")
	}
	if webpb.Getter(data, "a.missing") != nil {
		t.Fatal("expected nil for missing path")
	}
}

func TestMapValues(t *testing.T) {
	out := webpb.MapValues(map[string]any{"a": 1, "b": 2}, func(value any) any {
		return value.(int) * 2
	})
	if out["a"] != 2 || out["b"] != 4 {
		t.Fatalf("unexpected map values %+v", out)
	}
}

func TestSubTypeRegistry(t *testing.T) {
	registry := webpb.NewSubTypeRegistry()
	registry.Register("Base", "demo", func(data map[string]any) (webpb.Message, error) {
		message := &Demo{}
		return message, webpb.Populate(data, message)
	})
	created, err := registry.Create("Base", "demo", map[string]any{"a": "id"})
	if err != nil {
		t.Fatalf("create: %v", err)
	}
	if created.(*Demo).Id != "id" {
		t.Fatalf("unexpected created %+v", created)
	}
	if missing, _ := registry.Create("Base", "unknown", nil); missing != nil {
		t.Fatal("expected nil for unknown subtype")
	}
}
