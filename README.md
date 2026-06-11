[![CI](https://github.com/jinganix/webpb/actions/workflows/ci.yml/badge.svg)](https://github.com/jinganix/webpb/actions/workflows/ci.yml)
[![lib coverage](https://codecov.io/gh/jinganix/webpb/flags/lib/graph/badge.svg?branch=master)](https://codecov.io/gh/jinganix/webpb/flags/lib)
[![runtime-java coverage](https://codecov.io/gh/jinganix/webpb/flags/runtime-java/graph/badge.svg?branch=master)](https://codecov.io/gh/jinganix/webpb/flags/runtime-java)
[![frontend coverage](https://codecov.io/gh/jinganix/webpb/flags/frontend/graph/badge.svg?branch=master)](https://codecov.io/gh/jinganix/webpb/flags/frontend)
[![runtime-ts coverage](https://codecov.io/gh/jinganix/webpb/flags/runtime-ts/graph/badge.svg?branch=master)](https://codecov.io/gh/jinganix/webpb/flags/runtime-ts)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# webpb

Generate api definitions for web from protocol buffers file

## Run samples

Checkout source code from the repo

### Run `backend`

Run command in the root directory:

```shell
./gradlew sample:backend:bootRun
```

or in `Windows`

```shell
./gradlew.bat sample:backend:bootRun
```

### Run `frontend`

Install [Node.js](https://nodejs.org/) and [protoc](https://grpc.io/docs/protoc-installation/), then:

```shell
cd sample/frontend
npm install
npm start
```

Visit the frontend: http://localhost:4200

## Contributing

If you are interested in reporting/fixing issues and contributing directly to the code base, please see [CONTRIBUTING.md](CONTRIBUTING.md) for more information on what we're looking for and how to get started.
