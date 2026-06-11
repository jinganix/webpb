# Contributing

## Development docs

- Java code: [ai-kit/CODE_CONVENTIONS.md](ai-kit/CODE_CONVENTIONS.md)
- Tests: [ai-kit/TEST_CONVENTIONS.md](ai-kit/TEST_CONVENTIONS.md)
- Go plugin tests: [ai-kit/GO_TEST_CONVENTIONS.md](ai-kit/GO_TEST_CONVENTIONS.md)

The TypeScript protoc plugin is published to Maven Central as `webpb-protoc-ts` (`plugin/ts`).

The TypeScript runtime library is published to npm as [`webpb`](runtime/ts).

Run the full check from the repository root:

```bash
./gradlew build
```

Run module tests only:

```bash
./gradlew :lib:utilities:test
./gradlew :plugin:testGo
```

Generate coverage reports for all subprojects with tests:

```bash
./gradlew coverageReport
```

JaCoCo XML reports are written under each Java module's `build/reports/jacoco/test/`. The Go plugin writes `plugin/build/reports/coverage/coverage.out`. TypeScript modules use Vitest/Jest (`runtime/ts/coverage/`, `sample/frontend/coverage/`).

## Conventional Commits

Check if your commit messages meet the [conventional commit format](https://conventionalcommits.org).

The conventional config extends from [config-conventional](https://github.com/conventional-changelog/commitlint/tree/master/%40commitlint/config-conventional).

## Create a commit

Run `npm install` in root directory, then you will get [Commitizen](https://github.com/commitizen-tools/commitizen) installed.

Use `npm run cz` or `npx cz` create a commit.

## Workflow validation

Commit message will be validated by workflow. If the validation is fail, amend the commit and rerun validation action.
