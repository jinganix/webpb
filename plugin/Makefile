PROTO_ROOT := ../../lib/proto/src/main/resources
GEN_DIR := gen/webpb
GOPATH_BIN := $(shell go env GOPATH)/bin

.PHONY: generate build test

generate:
	@mkdir -p $(GEN_DIR)
	@PATH="$(GOPATH_BIN):$$PATH" protoc -I $(PROTO_ROOT) \
		--go_out=$(GEN_DIR) --go_opt=module=github.com/jinganix/webpb/plugin/gen/webpb \
		--go_opt=Mwebpb/WebpbExtend.proto=github.com/jinganix/webpb/plugin/gen/webpb \
		$(PROTO_ROOT)/webpb/WebpbExtend.proto
	@for d in auto_alias enumeration error_test extends_test import_test test1 test2 test3; do \
		mkdir -p ../../lib/tests/src/main/resources/$$d/dump; \
		if [ -f ../../lib/tests/build/generated/sources/proto/$$d/dump/test.dump ]; then \
			cp ../../lib/tests/build/generated/sources/proto/$$d/dump/test.dump ../../lib/tests/src/main/resources/$$d/dump/test.dump; \
		fi; \
	done

build:
	@go build -o bin/webpb-protoc-dump ./cmd/webpb-dump
	@go build -o bin/webpb-protoc-java ./cmd/webpb-java
	@go build -o bin/webpb-protoc-ts ./cmd/webpb-ts

test:
	@go test ./...
