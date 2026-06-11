# webpb-protoc-ts

protoc plugin that generates TypeScript from webpb proto files.

Requires [protoc](https://grpc.io/docs/protoc-installation/) on your PATH.

## Install

```bash
npm install --save-dev webpb-protoc-ts
```

## Usage

```bash
protoc \
  --plugin=protoc-gen-ts=./node_modules/.bin/webpb-protoc-ts \
  --ts_out=. \
  path/to/file.proto
```

Generated code imports the [webpb](https://www.npmjs.com/package/webpb) runtime.

## Development

Binaries are built from the Go plugin in `plugin/cmd/webpb-ts`.

```bash
cd plugin/npm/webpb-protoc-ts
npm run pack      # host platform vendor binary + dist/*.tgz
npm run publish   # all platforms + npm publish (requires NODE_AUTH_TOKEN)
```
