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

TypeScript sources live under `src/`. The published `bin` entry is compiled to
`dist/bin/webpb-protoc-ts.js`; native plugin binaries are built from the Go
plugin in `plugin/cmd/webpb-ts` into `vendor/`.

```bash
cd plugin/npm/webpb-protoc-ts
npm ci
npm run check    # lint, test, compile TypeScript
npm run build    # compile + vendor binary for host platform
npm run pack     # host platform vendor binary + dist/*.tgz
npm run publish  # all platforms + npm publish (requires NODE_AUTH_TOKEN)
```
