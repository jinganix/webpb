package pluginutil

import (
	"bytes"
	"errors"
	"io"
	"testing"

	"google.golang.org/protobuf/proto"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

func runWithIO(t *testing.T, reqData []byte, generate GenerateFunc) *pluginpb.CodeGeneratorResponse {
	t.Helper()
	var out bytes.Buffer
	oldStdin, oldStdout := stdin, stdout
	stdin = bytes.NewReader(reqData)
	stdout = &out
	t.Cleanup(func() {
		stdin = oldStdin
		stdout = oldStdout
	})
	Run(generate)
	resp := &pluginpb.CodeGeneratorResponse{}
	if err := proto.Unmarshal(out.Bytes(), resp); err != nil {
		t.Fatalf("unmarshal response: %v", err)
	}
	return resp
}

func mustMarshalRequest(t *testing.T) []byte {
	t.Helper()
	reqData, err := proto.Marshal(&pluginpb.CodeGeneratorRequest{})
	if err != nil {
		t.Fatalf("marshal: %v", err)
	}
	return reqData
}

func TestRunAdvertisesProto3Optional(t *testing.T) {
	resp := runWithIO(t, mustMarshalRequest(t), func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return []*pluginpb.CodeGeneratorResponse_File{NewFile("out.txt", "ok")}, nil
	})
	if resp.GetSupportedFeatures()&uint64(pluginpb.CodeGeneratorResponse_FEATURE_PROTO3_OPTIONAL) == 0 {
		t.Fatalf("expected FEATURE_PROTO3_OPTIONAL, got %d", resp.GetSupportedFeatures())
	}
}

func TestRunAdvertisesProto3OptionalOnError(t *testing.T) {
	resp := runWithIO(t, mustMarshalRequest(t), func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return nil, io.ErrClosedPipe
	})
	if resp.GetSupportedFeatures()&uint64(pluginpb.CodeGeneratorResponse_FEATURE_PROTO3_OPTIONAL) == 0 {
		t.Fatalf("expected FEATURE_PROTO3_OPTIONAL on error, got %d", resp.GetSupportedFeatures())
	}
}

func TestNewFile(t *testing.T) {
	file := NewFile("a.java", "content")
	if file.GetName() != "a.java" || file.GetContent() != "content" {
		t.Fatalf("unexpected file: %#v", file)
	}
}

func TestRunWritesGeneratedFiles(t *testing.T) {
	resp := runWithIO(t, mustMarshalRequest(t), func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return []*pluginpb.CodeGeneratorResponse_File{NewFile("out.txt", "ok")}, nil
	})
	if len(resp.File) != 1 || resp.File[0].GetContent() != "ok" {
		t.Fatalf("unexpected response: %#v", resp)
	}
}

type errReader struct{}

func (errReader) Read([]byte) (int, error) { return 0, io.ErrUnexpectedEOF }

type errWriter struct{}

func (errWriter) Write([]byte) (int, error) { return 0, io.ErrClosedPipe }

func TestRunWritesErrorWhenStdinFails(t *testing.T) {
	var out bytes.Buffer
	oldStdin, oldStdout := stdin, stdout
	stdin = errReader{}
	stdout = &out
	t.Cleanup(func() {
		stdin = oldStdin
		stdout = oldStdout
	})
	Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return nil, nil
	})
	resp := &pluginpb.CodeGeneratorResponse{}
	if err := proto.Unmarshal(out.Bytes(), resp); err != nil {
		t.Fatalf("unmarshal response: %v", err)
	}
	if resp.GetError() == "" {
		t.Fatal("expected error response when stdin fails")
	}
}

func TestRunWritesErrorWhenRequestIsInvalid(t *testing.T) {
	resp := runWithIO(t, []byte("not-a-proto"), func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return nil, nil
	})
	if resp.GetError() == "" {
		t.Fatal("expected error response for invalid request")
	}
}

func TestRunDoesNotPanicWhenStdoutWriteFails(t *testing.T) {
	reqData := mustMarshalRequest(t)
	oldStdin, oldStdout := stdin, stdout
	stdin = bytes.NewReader(reqData)
	stdout = errWriter{}
	t.Cleanup(func() {
		stdin = oldStdin
		stdout = oldStdout
	})
	Run(func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return []*pluginpb.CodeGeneratorResponse_File{NewFile("out.txt", "ok")}, nil
	})
}

func TestRunWritesErrorResponse(t *testing.T) {
	resp := runWithIO(t, mustMarshalRequest(t), func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		return nil, io.ErrClosedPipe
	})
	if resp.GetError() == "" {
		t.Fatal("expected error response")
	}
}

func TestRunRecoversGeneratePanic(t *testing.T) {
	resp := runWithIO(t, mustMarshalRequest(t), func(req *pluginpb.CodeGeneratorRequest) ([]*pluginpb.CodeGeneratorResponse_File, error) {
		panic(errors.New("boom"))
	})
	if resp.GetError() == "" {
		t.Fatal("expected error response for generate panic")
	}
}
