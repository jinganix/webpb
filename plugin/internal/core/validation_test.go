package core_test

import (
	"strings"
	"testing"

	"github.com/jinganix/webpb/plugin/internal/core"
	"github.com/jinganix/webpb/plugin/internal/testutil"
	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protoreflect"
)

func loadTestField(t *testing.T, message, field string) (protoreflect.FileDescriptor, protoreflect.FieldDescriptor) {
	t.Helper()
	ctx, err := testutil.CreateContext("proto2_generator_options")
	if err != nil {
		t.Fatalf("create context: %v", err)
	}
	for _, fd := range ctx.Descriptors {
		msgs := fd.Messages()
		for i := 0; i < msgs.Len(); i++ {
			if string(msgs.Get(i).Name()) != message {
				continue
			}
			fields := msgs.Get(i).Fields()
			for j := 0; j < fields.Len(); j++ {
				if string(fields.Get(j).Name()) == field {
					return fd, fields.Get(j)
				}
			}
		}
	}
	t.Fatalf("field %s.%s not found", message, field)
	return nil, nil
}

func validOf(build func(v *webpb.FieldValidation)) *webpb.FieldValidation {
	v := &webpb.FieldValidation{}
	build(v)
	return v
}

func mustContain(t *testing.T, got []string, want string) {
	t.Helper()
	for _, s := range got {
		if s == want {
			return
		}
	}
	t.Fatalf("expected %q in %v", want, got)
}

func TestRenderJavaSizeMerging(t *testing.T) {
	_, field := loadTestField(t, "PrimitiveTypes", "uint32_field")
	mapping := &webpb.JavaValidationMapping{
		SizeTemplate: proto.String(core.DefaultJavaSizeTemplate),
	}
	both := validOf(func(v *webpb.FieldValidation) {
		v.MinLen = proto.Int64(1)
		v.MaxLen = proto.Int64(5)
	})
	mustContain(t, core.RenderJavaFieldValidation(field, both, mapping), "@Size(min = 1, max = 5)")

	minOnly := validOf(func(v *webpb.FieldValidation) {
		v.MinLen = proto.Int64(1)
	})
	mustContain(t, core.RenderJavaFieldValidation(field, minOnly, mapping), "@Size(min = 1)")

	maxOnly := validOf(func(v *webpb.FieldValidation) {
		v.MaxLen = proto.Int64(5)
	})
	mustContain(t, core.RenderJavaFieldValidation(field, maxOnly, mapping), "@Size(max = 5)")
}

func TestRenderJavaFloatUsesDecimal(t *testing.T) {
	_, floatField := loadTestField(t, "PrimitiveTypes", "float_field")
	_, intField := loadTestField(t, "PrimitiveTypes", "uint32_field")
	mapping := &webpb.JavaValidationMapping{
		MinTemplate: proto.String(core.DefaultJavaMinTemplate),
		MaxTemplate: proto.String(core.DefaultJavaMaxTemplate),
	}
	floatValid := validOf(func(v *webpb.FieldValidation) {
		v.Min = proto.String("0.5")
		v.Max = proto.String("9.5")
	})
	got := core.RenderJavaFieldValidation(floatField, floatValid, mapping)
	mustContain(t, got, `@DecimalMin(value = "0.5")`)
	mustContain(t, got, `@DecimalMax(value = "9.5")`)

	intValid := validOf(func(v *webpb.FieldValidation) {
		v.Min = proto.String("1")
	})
	mustContain(t, core.RenderJavaFieldValidation(intField, intValid, mapping), "@Min(1)")
}

func TestRenderJavaPatternFlags(t *testing.T) {
	_, field := loadTestField(t, "PrimitiveTypes", "uint32_field")
	mapping := &webpb.JavaValidationMapping{
		PatternTemplate: proto.String(core.DefaultJavaPatternTemplate),
	}
	valid := validOf(func(v *webpb.FieldValidation) {
		v.Pattern = proto.String("^[a-z]+$")
		v.PatternFlags = proto.String("i")
	})
	got := core.RenderJavaFieldValidation(field, valid, mapping)
	if len(got) != 1 || !strings.Contains(got[0], `regexp = "^[a-z]+$"`) || !strings.Contains(got[0], "CASE_INSENSITIVE") {
		t.Fatalf("unexpected pattern rendering: %v", got)
	}
}

func TestRenderJavaPatternEscapesDescriptorValue(t *testing.T) {
	// Descriptor value (single backslashes): the regex engine must receive
	// exactly this string from every renderer. Java needs one extra level in
	// the source file; the TS validation object (strconv.Quote) and the
	// `@Matches(/.../)` decorator already deliver it.
	_, field := loadTestField(t, "PrimitiveTypes", "uint32_field")
	javaMapping := &webpb.JavaValidationMapping{
		PatternTemplate: proto.String(core.DefaultJavaPatternTemplate),
	}
	valid := validOf(func(v *webpb.FieldValidation) {
		v.Pattern = proto.String(`^\d+\.\d+$`)
	})
	got := core.RenderJavaFieldValidation(field, valid, javaMapping)
	if len(got) != 1 || !strings.Contains(got[0], `regexp = "^\\d+\\.\\d+$"`) {
		t.Fatalf("java must escape descriptor backslashes, got: %v", got)
	}

	tsGot := core.FormatTsValidationRule(core.TsValidationRule(valid))
	if !strings.Contains(tsGot, `pattern: "^\\d+\\.\\d+$"`) {
		t.Fatalf("ts validation object must quote descriptor backslashes, got: %q", tsGot)
	}
	fd, _ := loadTestField(t, "PrimitiveTypes", "uint32_field")
	tsMapping := core.ResolveTsValidationMapping(fd)
	decGot := core.RenderTsFieldValidation(field, valid, tsMapping)
	if !strings.Contains(strings.Join(decGot, " "), `/^\d+\.\d+$/)`) {
		t.Fatalf("ts decorator must carry the raw descriptor value, got: %v", decGot)
	}
}

func TestRenderTsStringVsCollection(t *testing.T) {
	_, single := loadTestField(t, "PrimitiveTypes", "uint32_field")
	_, repeated := loadTestField(t, "PrimitiveTypes", "repeated_float")
	fd, _ := loadTestField(t, "PrimitiveTypes", "uint32_field")
	mapping := core.ResolveTsValidationMapping(fd)

	singleValid := validOf(func(v *webpb.FieldValidation) {
		v.MinLen = proto.Int64(1)
		v.MaxLen = proto.Int64(5)
	})
	mustContain(t, core.RenderTsFieldValidation(single, singleValid, mapping), "@Length(1, 5)")

	collectionValid := validOf(func(v *webpb.FieldValidation) {
		v.MinLen = proto.Int64(1)
		v.MaxLen = proto.Int64(5)
	})
	got := core.RenderTsFieldValidation(repeated, collectionValid, mapping)
	mustContain(t, got, "@ArrayMinSize(1)")
	mustContain(t, got, "@ArrayMaxSize(5)")
}

func TestRenderTsValidEachForRepeated(t *testing.T) {
	_, repeated := loadTestField(t, "PrimitiveTypes", "repeated_float")
	fd, _ := loadTestField(t, "PrimitiveTypes", "uint32_field")
	mapping := core.ResolveTsValidationMapping(fd)
	valid := validOf(func(v *webpb.FieldValidation) {
		v.Valid = proto.Bool(true)
	})
	mustContain(t, core.RenderTsFieldValidation(repeated, valid, mapping), "@ValidateNested({ each: true })")
}

func TestRenderGoTag(t *testing.T) {
	_, single := loadTestField(t, "PrimitiveTypes", "uint32_field")
	_, repeated := loadTestField(t, "PrimitiveTypes", "repeated_float")
	fd, _ := loadTestField(t, "PrimitiveTypes", "uint32_field")
	mapping := core.ResolveGoValidationMapping(fd)

	singleValid := validOf(func(v *webpb.FieldValidation) {
		v.Required = proto.Bool(true)
		v.Min = proto.String("1")
		v.Max = proto.String("10")
	})
	if got := core.RenderGoValidateTag(single, singleValid, mapping); got != "required,gte=1,lte=10" {
		t.Fatalf("unexpected go tag: %q", got)
	}

	// valid=true on a singular scalar emits no dive; only collections dive.
	if got := core.RenderGoValidateTag(single, validOf(func(v *webpb.FieldValidation) {
		v.Valid = proto.Bool(true)
	}), mapping); got != "" {
		t.Fatalf("expected empty go tag for singular valid, got %q", got)
	}
	if got := core.RenderGoValidateTag(repeated, validOf(func(v *webpb.FieldValidation) {
		v.Valid = proto.Bool(true)
	}), mapping); got != "dive" {
		t.Fatalf("expected dive for repeated valid, got %q", got)
	}
}

func TestMappingOverridePrecedence(t *testing.T) {
	_, field := loadTestField(t, "PrimitiveTypes", "uint32_field")
	mapping := &webpb.JavaValidationMapping{
		RequiredTemplate: proto.String("@CustomNotNull"),
		SizeTemplate:     proto.String(core.DefaultJavaSizeTemplate),
	}
	valid := validOf(func(v *webpb.FieldValidation) {
		v.Required = proto.Bool(true)
	})
	mustContain(t, core.RenderJavaFieldValidation(field, valid, mapping), "@CustomNotNull")
}

func TestTsValidationRuleObject(t *testing.T) {
	if got := core.TsValidationRule(nil); got != nil {
		t.Fatalf("nil should give nil rule, got %+v", got)
	}
	empty := validOf(func(v *webpb.FieldValidation) {})
	if got := core.TsValidationRule(empty); got != nil {
		t.Fatalf("empty should give nil rule, got %+v", got)
	}
	full := validOf(func(v *webpb.FieldValidation) {
		v.Required = proto.Bool(true)
		v.NotBlank = proto.Bool(true)
		v.MinLen = proto.Int64(1)
		v.MaxLen = proto.Int64(64)
		v.Min = proto.String("0")
		v.Max = proto.String("150")
		v.Pattern = proto.String("^[a-z]+$")
		v.PatternFlags = proto.String("i")
		v.Email = proto.Bool(true)
		v.Valid = proto.Bool(true)
	})
	got := core.FormatTsValidationRule(core.TsValidationRule(full))
	for _, want := range []string{
		"required: true", "notBlank: true", "minLen: 1", "maxLen: 64",
		"min: 0", "max: 150", `pattern: "^[a-z]+$"`, `patternFlags: "i"`,
		"email: true", "valid: true",
	} {
		if !strings.Contains(got, want) {
			t.Fatalf("expected %q in %q", want, got)
		}
	}
	// Unparseable bounds stay quoted; blank flags are dropped.
	odd := validOf(func(v *webpb.FieldValidation) {
		v.Min = proto.String("abc")
		v.Pattern = proto.String("x")
	})
	oddGot := core.FormatTsValidationRule(core.TsValidationRule(odd))
	if !strings.Contains(oddGot, `min: "abc"`) {
		t.Fatalf("expected quoted min in %q", oddGot)
	}
	if strings.Contains(oddGot, "patternFlags") {
		t.Fatalf("blank flags should be dropped in %q", oddGot)
	}
}

func TestValidateFieldValidation(t *testing.T) {
	_, field := loadTestField(t, "PrimitiveTypes", "uint32_field")
	badRange := validOf(func(v *webpb.FieldValidation) {
		v.MinLen = proto.Int64(5)
		v.MaxLen = proto.Int64(1)
	})
	if err := core.ValidateFieldValidation(field, badRange); err == nil {
		t.Fatal("expected min_len > max_len error")
	}
	badPattern := validOf(func(v *webpb.FieldValidation) {
		v.Pattern = proto.String("([")
	})
	if err := core.ValidateFieldValidation(field, badPattern); err == nil {
		t.Fatal("expected bad pattern error")
	}
	if err := core.ValidateFieldValidation(field, nil); err != nil {
		t.Fatalf("nil should pass: %v", err)
	}
}
