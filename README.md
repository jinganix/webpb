[![CI](https://github.com/jinganix/webpb/actions/workflows/ci.yml/badge.svg)](https://github.com/jinganix/webpb/actions/workflows/ci.yml)
[![Coverage Status](https://coveralls.io/repos/github/jinganix/webpb/badge.svg?branch=master)](https://coveralls.io/github/jinganix/webpb?branch=master)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

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

Install [node.js](https://nodejs.org/en/) first, then use `npm` or `yarn` to run the command:

```shell
cd sample/frontend
npm install
npm start
```

Visit the frontend: http://localhost:4200

## Contributing

If you are interested in reporting/fixing issues and contributing directly to the code base, please see [CONTRIBUTING.md](CONTRIBUTING.md) for more information on what we're looking for and how to get started.
