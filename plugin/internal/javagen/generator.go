package javagen

import (
	"fmt"
	"strings"
	"sync"

	"github.com/jinganix/webpb/plugin/internal/template"
	"google.golang.org/protobuf/reflect/protoreflect"
)

var (
	engineOnce sync.Once
	sharedEngine *template.Engine
	engineErr error
)

func sharedJavaEngine() (*template.Engine, error) {
	engineOnce.Do(func() {
		sharedEngine, engineErr = template.NewJavaEngine()
	})
	return sharedEngine, engineErr
}

// Generator generates Java source files.
type Generator struct{}

// NewGenerator creates a Java generator.
func NewGenerator() *Generator {
	return &Generator{}
}

// Generate generates Java files for a protobuf file.
func (g *Generator) Generate(fd protoreflect.FileDescriptor) (map[string]string, error) {
	if _, err := sharedJavaEngine(); err != nil {
		return nil, err
	}
	javaPackage := GetJavaPackage(fd)
	if shouldIgnore(javaPackage) {
		return map[string]string{}, nil
	}
	fileMap := map[string]string{}
	messages := fd.Messages()
	for i := 0; i < messages.Len(); i++ {
		descriptor := messages.Get(i)
		msgGen, err := NewMessageGenerator(fd)
		if err != nil {
			return nil, fmt.Errorf("failed to parse: %s: %w", fd.Path(), err)
		}
		content, err := msgGen.Generate(descriptor)
		if err != nil {
			return nil, fmt.Errorf("failed to parse: %s message %s: %w", fd.Path(), descriptor.Name(), err)
		}
		fileMap[javaFilename(javaPackage, string(descriptor.Name()))] = content
	}
	enums := fd.Enums()
	for i := 0; i < enums.Len(); i++ {
		enumDescriptor := enums.Get(i)
		name := string(enumDescriptor.Name())
		for _, spec := range []struct {
			suffix string
			tmpl   string
		}{
			{"", "enum"},
			{"Values", "enum.values"},
			{"Names", "enum.names"},
		} {
			enumGen, err := NewEnumGenerator(fd)
			if err != nil {
				return nil, fmt.Errorf("failed to parse: %s: %w", fd.Path(), err)
			}
			content, err := enumGen.Generate(enumDescriptor, spec.tmpl)
			if err != nil {
				return nil, fmt.Errorf("failed to parse: %s: %w", fd.Path(), err)
			}
			fileMap[javaFilename(javaPackage, name+spec.suffix)] = content
		}
	}
	return fileMap, nil
}

func shouldIgnore(packageName string) bool {
	return strings.HasPrefix(packageName, "com.google.protobuf") ||
		strings.HasPrefix(packageName, "io.github.jinganix.webpb.utilities.descriptor")
}

func javaFilename(javaPackage, className string) string {
	return strings.ReplaceAll(javaPackage, ".", "/") + "/" + className + ".java"
}
