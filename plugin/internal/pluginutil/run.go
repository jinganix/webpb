package pluginutil

import (
	"io"
	"os"

	"google.golang.org/protobuf/proto"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

var (
	stdin  io.Reader = os.Stdin
	stdout io.Writer = os.Stdout
)

// GenerateFunc produces generated files from a protoc request.
type GenerateFunc func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error)

// Run executes a protoc plugin using the standard stdin/stdout protocol.
func Run(generate GenerateFunc) {
	data, err := io.ReadAll(stdin)
	if err != nil {
		panic(err)
	}
	req := &pluginpb.CodeGeneratorRequest{}
	if err := proto.Unmarshal(data, req); err != nil {
		panic(err)
	}
	files, err := generate(req)
	if err != nil {
		writeResponse(&pluginpb.CodeGeneratorResponse{Error: proto.String(err.Error())})
		return
	}
	writeResponse(&pluginpb.CodeGeneratorResponse{File: files})
}

func writeResponse(resp *pluginpb.CodeGeneratorResponse) {
	out, err := proto.Marshal(resp)
	if err != nil {
		panic(err)
	}
	if _, err := stdout.Write(out); err != nil {
		panic(err)
	}
}

// NewFile creates a generated file entry.
func NewFile(name, content string) *pluginpb.CodeGeneratorResponse_File {
	return &pluginpb.CodeGeneratorResponse_File{
		Name:    proto.String(name),
		Content: proto.String(content),
	}
}
