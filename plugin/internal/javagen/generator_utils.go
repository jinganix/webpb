package javagen

import (
	"google.golang.org/protobuf/reflect/protoreflect"
	"google.golang.org/protobuf/types/descriptorpb"
)

// GetJavaPackage returns the java_package option for a descriptor's file.
func GetJavaPackage(descriptor protoreflect.Descriptor) string {
	opts, ok := descriptor.ParentFile().Options().(*descriptorpb.FileOptions)
	if !ok {
		return ""
	}
	return opts.GetJavaPackage()
}
