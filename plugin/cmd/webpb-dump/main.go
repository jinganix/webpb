package main

import (
	"io"
	"os"

	"google.golang.org/protobuf/proto"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

func main() {
	data, err := io.ReadAll(os.Stdin)
	if err != nil {
		panic(err)
	}
	req := &pluginpb.CodeGeneratorRequest{}
	if err := proto.Unmarshal(data, req); err != nil {
		panic(err)
	}
	content := string(data)
	resp := &pluginpb.CodeGeneratorResponse{
		File: []*pluginpb.CodeGeneratorResponse_File{
			{
				Name:    proto.String("test.dump"),
				Content: &content,
			},
		},
	}
	out, err := proto.Marshal(resp)
	if err != nil {
		panic(err)
	}
	if _, err := os.Stdout.Write(out); err != nil {
		panic(err)
	}
}
