package pluginutil

import (
	"bytes"
	"io"
	"testing"

	"google.golang.org/protobuf/proto"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

func TestNewFile(t *testing.T) {
	file := NewFile("a.java", "content")
	if file.GetName() != "a.java" || file.GetContent() != "content" {
		t.Fatalf("unexpected file: %#v", file)
	}
}

func TestRunWritesGeneratedFiles(t *testing.T) {
	req := &pluginpb.CodeGeneratorRequest{}
	reqData, err := proto.Marshal(req)
	if err != nil {
		t.Fatalf("marshal: %v", err)
	}

	var out bytes.Buffer
	oldStdin, oldStdout := stdin, stdout
	stdin = bytes.NewReader(reqData)
	stdout = &out
	t.Cleanup(func() {
		stdin = oldStdin
		stdout = oldStdout
	})

	Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return []*pluginpb.CodeGeneratorResponse_File{NewFile("out.txt", "ok")}, nil
	})

	resp := &pluginpb.CodeGeneratorResponse{}
	if err := proto.Unmarshal(out.Bytes(), resp); err != nil {
		t.Fatalf("unmarshal response: %v", err)
	}
	if len(resp.File) != 1 || resp.File[0].GetContent() != "ok" {
		t.Fatalf("unexpected response: %#v", resp)
	}
}

type errReader struct{}

func (errReader) Read([]byte) (int, error) { return 0, io.ErrUnexpectedEOF }

type errWriter struct{}

func (errWriter) Write([]byte) (int, error) { return 0, io.ErrClosedPipe }

func TestRunPanicsWhenStdinFails(t *testing.T) {
	oldStdin := stdin
	stdin = errReader{}
	t.Cleanup(func() { stdin = oldStdin })
	defer func() {
		if recover() == nil {
			t.Fatal("expected panic when stdin read fails")
		}
	}()
	Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return nil, nil
	})
}

func TestRunPanicsWhenRequestIsInvalid(t *testing.T) {
	oldStdin := stdin
	stdin = bytes.NewReader([]byte("not-a-proto"))
	t.Cleanup(func() { stdin = oldStdin })
	defer func() {
		if recover() == nil {
			t.Fatal("expected panic when request is invalid")
		}
	}()
	Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return nil, nil
	})
}

func TestRunPanicsWhenStdoutWriteFails(t *testing.T) {
	req := &pluginpb.CodeGeneratorRequest{}
	reqData, _ := proto.Marshal(req)
	oldStdin, oldStdout := stdin, stdout
	stdin = bytes.NewReader(reqData)
	stdout = errWriter{}
	t.Cleanup(func() {
		stdin = oldStdin
		stdout = oldStdout
	})
	defer func() {
		if recover() == nil {
			t.Fatal("expected panic when stdout write fails")
		}
	}()
	Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return []*pluginpb.CodeGeneratorResponse_File{NewFile("out.txt", "ok")}, nil
	})
}

func TestRunWritesErrorResponse(t *testing.T) {
	req := &pluginpb.CodeGeneratorRequest{}
	reqData, _ := proto.Marshal(req)

	var out bytes.Buffer
	oldStdin, oldStdout := stdin, stdout
	stdin = bytes.NewReader(reqData)
	stdout = &out
	t.Cleanup(func() {
		stdin = oldStdin
		stdout = oldStdout
	})

	Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return nil, io.ErrClosedPipe
	})

	resp := &pluginpb.CodeGeneratorResponse{}
	if err := proto.Unmarshal(out.Bytes(), resp); err != nil {
		t.Fatalf("unmarshal response: %v", err)
	}
	if resp.GetError() == "" {
		t.Fatal("expected error response")
	}
}
