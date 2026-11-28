package pluginutil

import (
	"errors"
	"fmt"
	"io"
	"os"

	"google.golang.org/protobuf/proto"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

var (
	stdin  io.Reader = os.Stdin
	stdout io.Writer = os.Stdout
)

// SupportedFeatures is the protoc feature bitmask advertised by every
// webpb plugin. Keep it in one place so all plugins stay in sync.
const SupportedFeatures = uint64(pluginpb.CodeGeneratorResponse_FEATURE_PROTO3_OPTIONAL)

// GenerateFunc produces generated files from a protoc request.
type GenerateFunc func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error)

// Run executes a protoc plugin using the standard stdin/stdout protocol.
// It never panics: internal failures and panics from generate are reported
// via CodeGeneratorResponse.Error.
func Run(generate GenerateFunc) {
	resp := run(generate)
	if err := writeResponse(resp); err != nil {
		writeResponse(&pluginpb.CodeGeneratorResponse{Error: proto.String(err.Error())})
	}
}

func run(generate GenerateFunc) (resp *pluginpb.CodeGeneratorResponse) {
	defer func() {
		if r := recover(); r != nil {
			var err error
			switch v := r.(type) {
			case error:
				err = v
			case string:
				err = errors.New(v)
			default:
				err = fmt.Errorf("%v", v)
			}
			resp = &pluginpb.CodeGeneratorResponse{Error: proto.String(err.Error())}
		}
	}()
	data, err := io.ReadAll(stdin)
	if err != nil {
		return &pluginpb.CodeGeneratorResponse{Error: proto.String(err.Error())}
	}
	req := &pluginpb.CodeGeneratorRequest{}
	if err := proto.Unmarshal(data, req); err != nil {
		return &pluginpb.CodeGeneratorResponse{Error: proto.String(err.Error())}
	}
	files, err := generate(req)
	if err != nil {
		return &pluginpb.CodeGeneratorResponse{Error: proto.String(err.Error())}
	}
	return &pluginpb.CodeGeneratorResponse{File: files}
}

func writeResponse(resp *pluginpb.CodeGeneratorResponse) error {
	resp.SupportedFeatures = proto.Uint64(resp.GetSupportedFeatures() | SupportedFeatures)
	out, err := proto.Marshal(resp)
	if err != nil {
		return err
	}
	if _, err := stdout.Write(out); err != nil {
		return err
	}
	return nil
}

// NewFile creates a generated file entry.
func NewFile(name, content string) *pluginpb.CodeGeneratorResponse_File {
	return &pluginpb.CodeGeneratorResponse_File{
		Name:    proto.String(name),
		Content: proto.String(content),
	}
}
