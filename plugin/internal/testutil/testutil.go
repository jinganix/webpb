package testutil

import (
	"os"
	"path/filepath"
	"runtime"

	"github.com/jinganix/webpb/plugin/internal/core"
	"google.golang.org/protobuf/proto"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

// DumpName returns the dump resource path for a test case.
func DumpName(name string) string {
	return filepath.Join("lib", "tests", "src", "main", "resources", name, "dump", "test.dump")
}

// RepoRoot returns the webpb repository root.
func RepoRoot() string {
	_, file, _, _ := runtime.Caller(0)
	return filepath.Clean(filepath.Join(filepath.Dir(file), "..", "..", ".."))
}

func resolveDumpPath(dump string) string {
	root := RepoRoot()
	primary := filepath.Join(root, DumpName(dump))
	if _, err := os.Stat(primary); err == nil {
		return primary
	}
	return filepath.Join(
		root,
		"lib", "tests", "build", "generated", "sources", "proto", dump, "dump", "test.dump",
	)
}

// LoadRequest loads a CodeGeneratorRequest from a dump file.
func LoadRequest(dump string) (*pluginpb.CodeGeneratorRequest, error) {
	path := resolveDumpPath(dump)
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, err
	}
	req := &pluginpb.CodeGeneratorRequest{}
	if err := proto.Unmarshal(data, req); err != nil {
		return nil, err
	}
	return req, nil
}

// CreateContext builds a request context from a dump file.
func CreateContext(dump string) (*core.RequestContext, error) {
	req, err := LoadRequest(dump)
	if err != nil {
		return nil, err
	}
	return core.NewRequestContext(req)
}

// ReadExpected reads an expected golden file from plugin testdata.
func ReadExpected(lang, dump, key string) (string, error) {
	path := filepath.Join(RepoRoot(), "plugin", "testdata", lang, dump, key)
	data, err := os.ReadFile(path)
	if err != nil {
		return "", err
	}
	return string(data), nil
}
