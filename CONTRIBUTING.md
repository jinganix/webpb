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

Commit messages must meet the [conventional commit format](https://conventionalcommits.org):

```
<type>[optional scope]: <subject>

[optional body, explain why rather than what]
```

- **subject**: imperative, short; header total length ≤ 100 characters.
- **scope**: optional; when present, use one of the table below.
- PR titles must follow the same format (validated by workflow).

### type

| type | When to use |
|------|-------------|
| `feat` | New feature or module |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no semantic change |
| `refactor` | Restructuring, no semantic change |
| `perf` | Performance related |
| `test` | Tests only |
| `build` | Build, Gradle, dependency versions |
| `ci` | CI, git hooks |
| `chore` | Miscellaneous maintenance |
| `revert` | Revert a previous commit |

### scope

| scope | Area |
|-------|------|
| `deps` | Third-party dependency bumps |
| `lib` | `lib/` shared libraries |
| `plugin` | `plugin/` protoc plugins |
| `runtime` | `runtime/` client runtimes |
| `sample` | `sample/` examples |
| `java` | Java codegen output |
| `docs` | `README.md`, `CONTRIBUTING.md`, `ai-kit/` |
| `ci` | `.githooks/`, `scripts/`, `.github/` |
| `build` | `build.gradle.kts`, `settings.gradle.kts`, Gradle wrapper |

### Examples

```
feat(plugin): support host-defined types in augment fields

fix(deps): update all non-major dependencies

chore: bump version to 0.0.41-SNAPSHOT
```

## Create a commit

No npm install is needed at the repository root. The TypeScript runtime
(`runtime/ts`) and sample frontend (`sample/frontend`) manage their own
`node_modules` via their local `package.json`.

## Local git hooks (shell, no npm)

Clone or update, then enable once (by a human, not by Agent):

```bash
./scripts/setup-git-hooks.sh
```

Trial run:

```bash
echo "feat(plugin): test message" | ./scripts/validate-commit-msg.sh /dev/stdin
```

- `commit-msg` validates the message via `scripts/validate-commit-msg.sh`.
- `pre-commit` / `pre-push` refuse direct commits/pushes to `master`.

## Workflow validation

Commit message will be validated by workflow. If the validation is fail, amend the commit and rerun validation action.
