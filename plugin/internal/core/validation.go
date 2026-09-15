package core

import (
	"fmt"
	"regexp"
	"strconv"
	"strings"

	webpb "github.com/jinganix/webpb/plugin/gen/webpb"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protoreflect"
)

// Built-in Java templates (jakarta.validation style).
const (
	DefaultJavaRequiredTemplate = "@NotNull"
	DefaultJavaNotBlankTemplate = "@NotBlank"
	DefaultJavaSizeTemplate     = "@Size(min = {{min}}, max = {{max}})"
	DefaultJavaMinTemplate      = "@Min({{value}})"
	DefaultJavaMaxTemplate      = "@Max({{value}})"
	DefaultJavaDecimalMin       = "@DecimalMin(value = \"{{value}}\")"
	DefaultJavaDecimalMax       = "@DecimalMax(value = \"{{value}}\")"
	DefaultJavaPatternTemplate  = "@Pattern(regexp = \"{{value}}\")"
	DefaultJavaEmailTemplate    = "@Email"
	DefaultJavaValidTemplate    = "@Valid"
)

// Built-in TS templates (class-validator style).
const (
	DefaultTsRequiredTemplate      = "@IsDefined()"
	DefaultTsNotBlankTemplate      = "@IsNotEmpty()"
	DefaultTsLengthTemplate        = "@Length({{min}}, {{max}})"
	DefaultTsMinLengthTemplate     = "@MinLength({{value}})"
	DefaultTsMaxLengthTemplate     = "@MaxLength({{value}})"
	DefaultTsArrayMinSizeTemplate  = "@ArrayMinSize({{value}})"
	DefaultTsArrayMaxSizeTemplate  = "@ArrayMaxSize({{value}})"
	DefaultTsMinTemplate           = "@Min({{value}})"
	DefaultTsMaxTemplate           = "@Max({{value}})"
	DefaultTsPatternTemplate       = "@Matches(/{{value}}/{{flags}})"
	DefaultTsEmailTemplate         = "@IsEmail()"
	DefaultTsValidTemplate         = "@ValidateNested()"
	DefaultTsValidateNestedEach    = "@ValidateNested({ each: true })"
)

// Built-in Go templates (go-playground/validator tag fragments).
const (
	DefaultGoRequiredTemplate = "required"
	DefaultGoNotBlankTemplate = "required"
	DefaultGoMinLenTemplate   = "min={{value}}"
	DefaultGoMaxLenTemplate   = "max={{value}}"
	DefaultGoMinTemplate      = "gte={{value}}"
	DefaultGoMaxTemplate      = "lte={{value}}"
	DefaultGoPatternTemplate  = ""
	DefaultGoEmailTemplate    = "email"
	DefaultGoDiveTemplate     = "dive"
)

// GetFieldValidation returns the language-agnostic validation of a field.
func GetFieldValidation(field protoreflect.FieldDescriptor) *webpb.FieldValidation {
	return GetFieldOpts(field, HasFieldOpt).GetOpt().GetValid()
}

// Presence helpers (protoc-gen-go for this file exposes only Getters).
func hasPresence(valid *webpb.FieldValidation, name protoreflect.Name) bool {
	if valid == nil {
		return false
	}
	fd := valid.ProtoReflect().Descriptor().Fields().ByName(name)
	return fd != nil && valid.ProtoReflect().Has(fd)
}

func hasMinLen(valid *webpb.FieldValidation) bool { return hasPresence(valid, "min_len") }
func hasMaxLen(valid *webpb.FieldValidation) bool { return hasPresence(valid, "max_len") }
func hasMin(valid *webpb.FieldValidation) bool    { return hasPresence(valid, "min") }
func hasMax(valid *webpb.FieldValidation) bool    { return hasPresence(valid, "max") }
func hasPattern(valid *webpb.FieldValidation) bool { return hasPresence(valid, "pattern") }

// HasFieldValidation reports whether a field declares (opts).opt.valid.
func HasFieldValidation(field protoreflect.FieldDescriptor) bool {
	return GetFieldValidation(field) != nil
}

// ResolveJavaValidationMapping merges WebpbOptions defaults, file-level
// overrides and built-in defaults into a complete mapping.
func ResolveJavaValidationMapping(fd protoreflect.FileDescriptor) *webpb.JavaValidationMapping {
	webpbMapping := GetWebpbFileOpts(fd, HasFileJava).GetJava().GetValidation()
	fileMapping := GetFileOpts(fd, HasFileJava).GetJava().GetValidation()
	pick := func(webpbVal, fileVal, def string) string {
		if fileVal != "" {
			return fileVal
		}
		if webpbVal != "" {
			return webpbVal
		}
		return def
	}
	return &webpb.JavaValidationMapping{
		RequiredTemplate:  proto.String(pick(webpbMapping.GetRequiredTemplate(), fileMapping.GetRequiredTemplate(), DefaultJavaRequiredTemplate)),
		NotBlankTemplate:  proto.String(pick(webpbMapping.GetNotBlankTemplate(), fileMapping.GetNotBlankTemplate(), DefaultJavaNotBlankTemplate)),
		SizeTemplate:      proto.String(pick(webpbMapping.GetSizeTemplate(), fileMapping.GetSizeTemplate(), DefaultJavaSizeTemplate)),
		MinTemplate:       proto.String(pick(webpbMapping.GetMinTemplate(), fileMapping.GetMinTemplate(), DefaultJavaMinTemplate)),
		MaxTemplate:       proto.String(pick(webpbMapping.GetMaxTemplate(), fileMapping.GetMaxTemplate(), DefaultJavaMaxTemplate)),
		PatternTemplate:   proto.String(pick(webpbMapping.GetPatternTemplate(), fileMapping.GetPatternTemplate(), DefaultJavaPatternTemplate)),
		EmailTemplate:     proto.String(pick(webpbMapping.GetEmailTemplate(), fileMapping.GetEmailTemplate(), DefaultJavaEmailTemplate)),
		ValidTemplate:     proto.String(pick(webpbMapping.GetValidTemplate(), fileMapping.GetValidTemplate(), DefaultJavaValidTemplate)),
	}
}

// ResolveTsValidationDecoratorsEnabled reports whether class-validator
// decorators should be emitted (file-level or shared WebpbOptions opt-in).
func ResolveTsValidationDecoratorsEnabled(fd protoreflect.FileDescriptor) bool {
	return GetWebpbFileOpts(fd, HasFileTs).GetTs().GetValidationDecorators() ||
		GetFileOpts(fd, HasFileTs).GetTs().GetValidationDecorators()
}

// ResolveTsValidationObjectEnabled reports whether the framework-neutral
// `<Message>Validation` descriptor object should be emitted.
func ResolveTsValidationObjectEnabled(fd protoreflect.FileDescriptor) bool {
	return GetWebpbFileOpts(fd, HasFileTs).GetTs().GetValidationObject() ||
		GetFileOpts(fd, HasFileTs).GetTs().GetValidationObject()
}

// TsFieldValidationRule is the framework-neutral view of FieldValidation
// used by the TS `<Message>Validation` descriptor object. Only constraints
// actually declared are populated; the TS renderer omits the rest.
type TsFieldValidationRule struct {
	Required, NotBlank, Email, Valid bool
	HasMinLen, HasMaxLen             bool
	MinLen, MaxLen                   int64
	HasMin, HasMax                   bool
	Min, Max                         string
	HasPattern                       bool
	Pattern, PatternFlags            string
}

// TsValidationRule extracts the neutral rule for a field, or nil when the
// field declares no validation constraints.
func TsValidationRule(valid *webpb.FieldValidation) *TsFieldValidationRule {
	if valid == nil {
		return nil
	}
	rule := &TsFieldValidationRule{}
	empty := true
	if valid.GetRequired() {
		rule.Required = true
		empty = false
	}
	if valid.GetNotBlank() {
		rule.NotBlank = true
		empty = false
	}
	if hasMinLen(valid) {
		rule.HasMinLen = true
		rule.MinLen = valid.GetMinLen()
		empty = false
	}
	if hasMaxLen(valid) {
		rule.HasMaxLen = true
		rule.MaxLen = valid.GetMaxLen()
		empty = false
	}
	if hasMin(valid) {
		rule.HasMin = true
		rule.Min = valid.GetMin()
		empty = false
	}
	if hasMax(valid) {
		rule.HasMax = true
		rule.Max = valid.GetMax()
		empty = false
	}
	if hasPattern(valid) {
		rule.HasPattern = true
		rule.Pattern = valid.GetPattern()
		rule.PatternFlags = valid.GetPatternFlags()
		empty = false
	}
	if valid.GetEmail() {
		rule.Email = true
		empty = false
	}
	if valid.GetValid() {
		rule.Valid = true
		empty = false
	}
	if empty {
		return nil
	}
	return rule
}

// formatTsNumber renders a proto string bound as a TS numeric literal when
// it parses as a finite number, otherwise as a quoted string.
func formatTsNumber(raw string) string {
	if num, err := strconv.ParseFloat(strings.TrimSpace(raw), 64); err == nil {
		return strconv.FormatFloat(num, 'g', -1, 64)
	}
	return strconv.Quote(raw)
}

// FormatTsValidationRule renders a rule as a TS object literal, e.g.
// `{ required: true, minLen: 1, maxLen: 64 }`.
func FormatTsValidationRule(rule *TsFieldValidationRule) string {
	var parts []string
	if rule.Required {
		parts = append(parts, "required: true")
	}
	if rule.NotBlank {
		parts = append(parts, "notBlank: true")
	}
	if rule.HasMinLen {
		parts = append(parts, "minLen: "+strconv.FormatInt(rule.MinLen, 10))
	}
	if rule.HasMaxLen {
		parts = append(parts, "maxLen: "+strconv.FormatInt(rule.MaxLen, 10))
	}
	if rule.HasMin {
		parts = append(parts, "min: "+formatTsNumber(rule.Min))
	}
	if rule.HasMax {
		parts = append(parts, "max: "+formatTsNumber(rule.Max))
	}
	if rule.HasPattern {
		parts = append(parts, "pattern: "+strconv.Quote(rule.Pattern))
		if strings.TrimSpace(rule.PatternFlags) != "" {
			parts = append(parts, "patternFlags: "+strconv.Quote(rule.PatternFlags))
		}
	}
	if rule.Email {
		parts = append(parts, "email: true")
	}
	if rule.Valid {
		parts = append(parts, "valid: true")
	}
	return "{ " + strings.Join(parts, ", ") + " }"
}

// ResolveTsValidationMapping merges TS validation templates.
func ResolveTsValidationMapping(fd protoreflect.FileDescriptor) *webpb.TsValidationMapping {
	webpbMapping := GetWebpbFileOpts(fd, HasFileTs).GetTs().GetValidation()
	fileMapping := GetFileOpts(fd, HasFileTs).GetTs().GetValidation()
	pick := func(webpbVal, fileVal, def string) string {
		if fileVal != "" {
			return fileVal
		}
		if webpbVal != "" {
			return webpbVal
		}
		return def
	}
	return &webpb.TsValidationMapping{
		RequiredTemplate:        proto.String(pick(webpbMapping.GetRequiredTemplate(), fileMapping.GetRequiredTemplate(), DefaultTsRequiredTemplate)),
		NotBlankTemplate:        proto.String(pick(webpbMapping.GetNotBlankTemplate(), fileMapping.GetNotBlankTemplate(), DefaultTsNotBlankTemplate)),
		LengthTemplate:          proto.String(pick(webpbMapping.GetLengthTemplate(), fileMapping.GetLengthTemplate(), DefaultTsLengthTemplate)),
		MinLengthTemplate:       proto.String(pick(webpbMapping.GetMinLengthTemplate(), fileMapping.GetMinLengthTemplate(), DefaultTsMinLengthTemplate)),
		MaxLengthTemplate:       proto.String(pick(webpbMapping.GetMaxLengthTemplate(), fileMapping.GetMaxLengthTemplate(), DefaultTsMaxLengthTemplate)),
		ArrayMinSizeTemplate:    proto.String(pick(webpbMapping.GetArrayMinSizeTemplate(), fileMapping.GetArrayMinSizeTemplate(), DefaultTsArrayMinSizeTemplate)),
		ArrayMaxSizeTemplate:    proto.String(pick(webpbMapping.GetArrayMaxSizeTemplate(), fileMapping.GetArrayMaxSizeTemplate(), DefaultTsArrayMaxSizeTemplate)),
		MinTemplate:             proto.String(pick(webpbMapping.GetMinTemplate(), fileMapping.GetMinTemplate(), DefaultTsMinTemplate)),
		MaxTemplate:             proto.String(pick(webpbMapping.GetMaxTemplate(), fileMapping.GetMaxTemplate(), DefaultTsMaxTemplate)),
		PatternTemplate:         proto.String(pick(webpbMapping.GetPatternTemplate(), fileMapping.GetPatternTemplate(), DefaultTsPatternTemplate)),
		EmailTemplate:           proto.String(pick(webpbMapping.GetEmailTemplate(), fileMapping.GetEmailTemplate(), DefaultTsEmailTemplate)),
		ValidTemplate:           proto.String(pick(webpbMapping.GetValidTemplate(), fileMapping.GetValidTemplate(), DefaultTsValidTemplate)),
	}
}

// ResolveGoValidationMapping merges Go validation templates.
func ResolveGoValidationMapping(fd protoreflect.FileDescriptor) *webpb.GoValidationMapping {
	webpbMapping := GetWebpbFileOpts(fd, HasFileGo).GetGo().GetValidation()
	fileMapping := GetFileOpts(fd, HasFileGo).GetGo().GetValidation()
	pick := func(webpbVal, fileVal, def string) string {
		if fileVal != "" {
			return fileVal
		}
		if webpbVal != "" {
			return webpbVal
		}
		return def
	}
	return &webpb.GoValidationMapping{
		RequiredTemplate:  proto.String(pick(webpbMapping.GetRequiredTemplate(), fileMapping.GetRequiredTemplate(), DefaultGoRequiredTemplate)),
		NotBlankTemplate:  proto.String(pick(webpbMapping.GetNotBlankTemplate(), fileMapping.GetNotBlankTemplate(), DefaultGoNotBlankTemplate)),
		MinLenTemplate:    proto.String(pick(webpbMapping.GetMinLenTemplate(), fileMapping.GetMinLenTemplate(), DefaultGoMinLenTemplate)),
		MaxLenTemplate:    proto.String(pick(webpbMapping.GetMaxLenTemplate(), fileMapping.GetMaxLenTemplate(), DefaultGoMaxLenTemplate)),
		MinTemplate:       proto.String(pick(webpbMapping.GetMinTemplate(), fileMapping.GetMinTemplate(), DefaultGoMinTemplate)),
		MaxTemplate:       proto.String(pick(webpbMapping.GetMaxTemplate(), fileMapping.GetMaxTemplate(), DefaultGoMaxTemplate)),
		PatternTemplate:   proto.String(pick(webpbMapping.GetPatternTemplate(), fileMapping.GetPatternTemplate(), DefaultGoPatternTemplate)),
		EmailTemplate:     proto.String(pick(webpbMapping.GetEmailTemplate(), fileMapping.GetEmailTemplate(), DefaultGoEmailTemplate)),
		ValidTemplate:     proto.String(pick(webpbMapping.GetValidTemplate(), fileMapping.GetValidTemplate(), DefaultGoDiveTemplate)),
	}
}

var missingParamPattern = regexp.MustCompile(`,?\s*[A-Za-z_][A-Za-z0-9_]*\s*=\s*"?\{\{[^}]+\}\}"?`)

// renderTemplate substitutes {{value}}/{{min}}/{{max}}/{{flags}} and drops
// key-value pairs whose placeholder had no value. It also cleans up
// dangling commas left behind.
func renderTemplate(template string, values map[string]string) string {
	out := template
	for key, value := range values {
		out = strings.ReplaceAll(out, "{{"+key+"}}", value)
	}
	// Drop pairs referencing missing placeholders.
	out = missingParamPattern.ReplaceAllString(out, "")
	// Clean dangling commas inside parens/brackets.
	out = regexp.MustCompile(`\(\s*,\s*`).ReplaceAllString(out, "(")
	out = regexp.MustCompile(`\[\s*,\s*`).ReplaceAllString(out, "[")
	out = regexp.MustCompile(`\s*,\s*\)`).ReplaceAllString(out, ")")
	out = regexp.MustCompile(`\s*,\s*\]`).ReplaceAllString(out, "]")
	out = regexp.MustCompile(`,\s*,`).ReplaceAllString(out, ",")
	// Empty regex flags cleanup for TS /{{value}}/{{flags}} -> /{{value}}/
	out = strings.ReplaceAll(out, "//", "/")
	out = regexp.MustCompile(`/(\s*\))`).ReplaceAllString(out, "$1")
	out = strings.TrimSpace(out)
	return out
}

func quoteJavaString(value string) string {
	return strconv.Quote(value)
}

// isFloatKind reports whether numeric bounds should use DecimalMin/DecimalMax.
func isFloatKind(kind protoreflect.Kind) bool {
	return kind == protoreflect.FloatKind || kind == protoreflect.DoubleKind
}

// RenderJavaFieldValidation renders FieldValidation into Java annotation
// strings (still requiring ImportAnnotation for import handling).
func RenderJavaFieldValidation(field protoreflect.FieldDescriptor, valid *webpb.FieldValidation, mapping *webpb.JavaValidationMapping) []string {
	if valid == nil {
		return nil
	}
	var out []string
	if valid.GetRequired() && mapping.GetRequiredTemplate() != "" {
		out = append(out, renderTemplate(mapping.GetRequiredTemplate(), map[string]string{}))
	}
	if valid.GetNotBlank() && mapping.GetNotBlankTemplate() != "" {
		out = append(out, renderTemplate(mapping.GetNotBlankTemplate(), map[string]string{}))
	}
	if hasMinLen(valid) || hasMaxLen(valid) {
		values := map[string]string{}
		if hasMinLen(valid) {
			values["min"] = strconv.FormatInt(valid.GetMinLen(), 10)
		}
		if hasMaxLen(valid) {
			values["max"] = strconv.FormatInt(valid.GetMaxLen(), 10)
		}
		if rendered := renderTemplate(mapping.GetSizeTemplate(), values); rendered != "" {
			out = append(out, rendered)
		}
	}
	if hasMin(valid) {
		template := mapping.GetMinTemplate()
		if mapping.GetMinTemplate() == DefaultJavaMinTemplate && isFloatKind(field.Kind()) {
			template = DefaultJavaDecimalMin
		}
		out = append(out, renderTemplate(template, map[string]string{"value": valid.GetMin()}))
	}
	if hasMax(valid) {
		template := mapping.GetMaxTemplate()
		if mapping.GetMaxTemplate() == DefaultJavaMaxTemplate && isFloatKind(field.Kind()) {
			template = DefaultJavaDecimalMax
		}
		out = append(out, renderTemplate(template, map[string]string{"value": valid.GetMax()}))
	}
	if hasPattern(valid) {
		values := map[string]string{"value": valid.GetPattern()}
		javaFlags := javaPatternFlags(valid.GetPatternFlags())
		if javaFlags != "" {
			values["flags"] = javaFlags
		}
		rendered := renderTemplate(mapping.GetPatternTemplate(), values)
		// Default template has no {{flags}} slot: append flags attribute.
		if javaFlags != "" && !strings.Contains(mapping.GetPatternTemplate(), "{{flags}}") {
			rendered = strings.TrimSuffix(rendered, ")") + ", flags = " + javaFlags + ")"
		}
		if rendered != "" {
			out = append(out, rendered)
		}
	}
	if valid.GetEmail() && mapping.GetEmailTemplate() != "" {
		out = append(out, renderTemplate(mapping.GetEmailTemplate(), map[string]string{}))
	}
	if valid.GetValid() && mapping.GetValidTemplate() != "" {
		out = append(out, renderTemplate(mapping.GetValidTemplate(), map[string]string{}))
	}
	return out
}

// javaPatternFlags converts generic flags ("i" or "CASE_INSENSITIVE,...")
// into a Java Pattern.Flag expression.
func javaPatternFlags(flags string) string {
	flags = strings.TrimSpace(flags)
	if flags == "" {
		return ""
	}
	short := map[string]string{
		"i": "CASE_INSENSITIVE",
		"m": "MULTILINE",
		"s": "DOTALL",
		"u": "UNICODE_CASE",
		"x": "COMMENTS",
		"d": "UNIX_LINES",
	}
	var names []string
	if strings.Contains(flags, ",") || strings.Contains(flags, " ") {
		for _, part := range strings.FieldsFunc(flags, func(r rune) bool { return r == ',' || r == ' ' }) {
			part = strings.TrimSpace(part)
			if part == "" {
				continue
			}
			if full, ok := short[strings.ToLower(part)]; ok && len(part) == 1 {
				part = full
			}
			names = append(names, "Pattern.Flag."+strings.ToUpper(part))
		}
	} else if len(flags) == 1 {
		if full, ok := short[strings.ToLower(flags)]; ok {
			names = append(names, "Pattern.Flag."+full)
		} else {
			names = append(names, "Pattern.Flag."+strings.ToUpper(flags))
		}
	} else if full, ok := short[strings.ToLower(flags)]; ok {
		names = append(names, "Pattern.Flag."+full)
	} else {
		names = append(names, "Pattern.Flag."+strings.ToUpper(flags))
	}
	if len(names) == 1 {
		return names[0]
	}
	return "{ " + strings.Join(names, ", ") + " }"
}

// IsCollectionField reports whether min_len/max_len describe a collection.
func IsCollectionField(field protoreflect.FieldDescriptor) bool {
	return field.IsMap() || field.Cardinality() == protoreflect.Repeated
}

// RenderTsFieldValidation renders FieldValidation into TS decorators.
func RenderTsFieldValidation(field protoreflect.FieldDescriptor, valid *webpb.FieldValidation, mapping *webpb.TsValidationMapping) []string {
	if valid == nil {
		return nil
	}
	var out []string
	if valid.GetRequired() && mapping.GetRequiredTemplate() != "" {
		out = append(out, renderTemplate(mapping.GetRequiredTemplate(), map[string]string{}))
	}
	if valid.GetNotBlank() && mapping.GetNotBlankTemplate() != "" {
		out = append(out, renderTemplate(mapping.GetNotBlankTemplate(), map[string]string{}))
	}
	if hasMinLen(valid) || hasMaxLen(valid) {
		if IsCollectionField(field) {
			if hasMinLen(valid) {
				out = append(out, renderTemplate(mapping.GetArrayMinSizeTemplate(), map[string]string{"value": strconv.FormatInt(valid.GetMinLen(), 10)}))
			}
			if hasMaxLen(valid) {
				out = append(out, renderTemplate(mapping.GetArrayMaxSizeTemplate(), map[string]string{"value": strconv.FormatInt(valid.GetMaxLen(), 10)}))
			}
		} else if hasMinLen(valid) && hasMaxLen(valid) {
			out = append(out, renderTemplate(mapping.GetLengthTemplate(), map[string]string{
				"min": strconv.FormatInt(valid.GetMinLen(), 10),
				"max": strconv.FormatInt(valid.GetMaxLen(), 10),
			}))
		} else if hasMinLen(valid) {
			out = append(out, renderTemplate(mapping.GetMinLengthTemplate(), map[string]string{"value": strconv.FormatInt(valid.GetMinLen(), 10)}))
		} else {
			out = append(out, renderTemplate(mapping.GetMaxLengthTemplate(), map[string]string{"value": strconv.FormatInt(valid.GetMaxLen(), 10)}))
		}
	}
	if hasMin(valid) {
		out = append(out, renderTemplate(mapping.GetMinTemplate(), map[string]string{"value": valid.GetMin()}))
	}
	if hasMax(valid) {
		out = append(out, renderTemplate(mapping.GetMaxTemplate(), map[string]string{"value": valid.GetMax()}))
	}
	if hasPattern(valid) {
		rendered := renderTemplate(mapping.GetPatternTemplate(), map[string]string{
			"value": valid.GetPattern(),
			"flags": valid.GetPatternFlags(),
		})
		// Drop empty flag segment left by the default /{{value}}/{{flags}} shape.
		rendered = strings.ReplaceAll(rendered, "//)", "/)")
		if rendered != "" {
			out = append(out, rendered)
		}
	}
	if valid.GetEmail() && mapping.GetEmailTemplate() != "" {
		out = append(out, renderTemplate(mapping.GetEmailTemplate(), map[string]string{}))
	}
	if valid.GetValid() {
		if IsCollectionField(field) && mapping.GetValidTemplate() == DefaultTsValidTemplate {
			out = append(out, DefaultTsValidateNestedEach)
		} else if mapping.GetValidTemplate() != "" {
			out = append(out, renderTemplate(mapping.GetValidTemplate(), map[string]string{}))
		}
	}
	return out
}

// RenderGoValidateTag renders FieldValidation into validator tag fragments.
func RenderGoValidateTag(field protoreflect.FieldDescriptor, valid *webpb.FieldValidation, mapping *webpb.GoValidationMapping) string {
	if valid == nil {
		return ""
	}
	var parts []string
	add := func(template, value string) {
		if template == "" {
			return
		}
		parts = append(parts, renderTemplate(template, map[string]string{"value": value}))
	}
	if valid.GetRequired() {
		add(mapping.GetRequiredTemplate(), "")
	}
	if valid.GetNotBlank() {
		add(mapping.GetNotBlankTemplate(), "")
	}
	if hasMinLen(valid) {
		add(mapping.GetMinLenTemplate(), strconv.FormatInt(valid.GetMinLen(), 10))
	}
	if hasMaxLen(valid) {
		add(mapping.GetMaxLenTemplate(), strconv.FormatInt(valid.GetMaxLen(), 10))
	}
	if hasMin(valid) {
		add(mapping.GetMinTemplate(), valid.GetMin())
	}
	if hasMax(valid) {
		add(mapping.GetMaxTemplate(), valid.GetMax())
	}
	if hasPattern(valid) {
		add(mapping.GetPatternTemplate(), valid.GetPattern())
	}
	if valid.GetEmail() {
		add(mapping.GetEmailTemplate(), "")
	}
	if valid.GetValid() && IsCollectionField(field) {
		add(mapping.GetValidTemplate(), "")
	}
	seen := map[string]struct{}{}
	var deduped []string
	for _, part := range parts {
		part = strings.TrimSpace(part)
		if part == "" {
			continue
		}
		if _, ok := seen[part]; ok {
			continue
		}
		seen[part] = struct{}{}
		deduped = append(deduped, part)
	}
	return strings.Join(deduped, ",")
}

// ValidateFieldValidation performs basic sanity checks on constraints.
func ValidateFieldValidation(field protoreflect.FieldDescriptor, valid *webpb.FieldValidation) error {
	if valid == nil {
		return nil
	}
	if hasMinLen(valid) && hasMaxLen(valid) && valid.GetMinLen() > valid.GetMaxLen() {
		return fmt.Errorf("invalid validation for field %s: min_len %d > max_len %d", field.Name(), valid.GetMinLen(), valid.GetMaxLen())
	}
	if hasMin(valid) && hasMax(valid) {
		if minNum, err1 := strconv.ParseFloat(valid.GetMin(), 64); err1 == nil {
			if maxNum, err2 := strconv.ParseFloat(valid.GetMax(), 64); err2 == nil && minNum > maxNum {
				return fmt.Errorf("invalid validation for field %s: min %s > max %s", field.Name(), valid.GetMin(), valid.GetMax())
			}
		}
	}
	if hasPattern(valid) {
		if _, err := regexp.Compile(valid.GetPattern()); err != nil {
			return fmt.Errorf("invalid validation pattern for field %s: %w", field.Name(), err)
		}
	}
	_ = quoteJavaString
	return nil
}
