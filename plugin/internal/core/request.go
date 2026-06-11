package core

import (
	"strings"

	"google.golang.org/protobuf/reflect/protodesc"
	"google.golang.org/protobuf/reflect/protoreflect"
	"google.golang.org/protobuf/reflect/protoregistry"
	pluginpb "google.golang.org/protobuf/types/pluginpb"
)

func normalizeProtoPath(path string) string {
	return strings.ReplaceAll(path, "\\", "/")
}

type filesResolver struct {
	files map[string]protoreflect.FileDescriptor
}

func (r *filesResolver) FindFileByPath(path string) (protoreflect.FileDescriptor, error) {
	if fd, ok := r.files[normalizeProtoPath(path)]; ok {
		return fd, nil
	}
	return nil, protoregistry.NotFound
}

func (r *filesResolver) FindDescriptorByName(name protoreflect.FullName) (protoreflect.Descriptor, error) {
	return nil, protoregistry.NotFound
}

// RequestContext holds parsed file descriptors from a code generator request.
type RequestContext struct {
	Descriptors       []protoreflect.FileDescriptor
	TargetDescriptors []protoreflect.FileDescriptor
}

// NewRequestContext builds descriptors without extension registry, matching Java behavior.
func NewRequestContext(req *pluginpb.CodeGeneratorRequest) (*RequestContext, error) {
	all, targets, err := BuildDescriptors(req)
	if err != nil {
		return nil, err
	}
	return &RequestContext{
		Descriptors:       all,
		TargetDescriptors: targets,
	}, nil
}

// BuildDescriptors constructs file descriptors from a plugin request.
func BuildDescriptors(req *pluginpb.CodeGeneratorRequest) ([]protoreflect.FileDescriptor, []protoreflect.FileDescriptor, error) {
	filesMap := make(map[string]protoreflect.FileDescriptor)
	resolver := &filesResolver{files: filesMap}
	var all []protoreflect.FileDescriptor

	for _, pb := range req.ProtoFile {
		name := normalizeProtoPath(pb.GetName())
		pb.Name = &name
		fd, err := protodesc.FileOptions{AllowUnresolvable: true}.New(pb, resolver)
		if err != nil {
			return nil, nil, err
		}
		filesMap[name] = fd
		all = append(all, fd)
	}

	var targets []protoreflect.FileDescriptor
	for _, name := range req.FileToGenerate {
		targets = append(targets, filesMap[normalizeProtoPath(name)])
	}
	return all, targets, nil
}
