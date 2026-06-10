# Testing conventions

> Read this document before editing tests.  
> Java style: [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md)  
> Go plugin golden tests: [GO_TEST_CONVENTIONS.md](GO_TEST_CONVENTIONS.md)  
> Run tests from repository root: `./gradlew build`

---

## 1. Scope

- Test classes with real logic (generators, processors, runtime clients, utilities with branching).
- Skip pure model/mapping/pass-through code without branches.
- **Codegen golden tests** live in `plugin` (Go). Do not add duplicate golden JUnit suites in legacy plugin modules.

---

## 2. Behavior-first organization

Applies to **all** test types (unit, integration):

- Organize tests around **observable behavior** (outcome + condition), not around mirroring source method names.
- Prefer flat `@Test` methods on the outer class. Use `@Nested` only when a group shares setup or clearly scopes one behavior slice.
- At most **one** `@Nested` level. Do not nest further for sub-scenarios.
- `@Nested` `@DisplayName` and test names use BDD wording (`should ... when ...`), not Java method names as the primary structure.
- Do **not** require one `@Nested` class per public method on the class under test.

---

## 3. Layout and naming

### Java (JUnit 5)

- Test package mirrors source package under `{module}/src/test/java/io/github/jinganix/webpb/`.
- Test class: `{ClassName}Test`.
- Prefer concise BDD format for `@DisplayName`: `should ... when ...`.
- Keep `@DisplayName` text lowercase at the start (do not start with uppercase).
- Test method names: `shouldReturn{Outcome}When{Condition}` (or `shouldThrow...When...`).

Do not use `Given ... -> should ...` in `@DisplayName`; use `should ... when ...` only. When touching older tests, migrate names and structure toward the BDD format above.

### TypeScript (Vitest)

- Test files: `{module}/test/**/*.test.ts` or `{module}/src/**/*.test.ts`.
- Use `describe` for the unit under test; use `it('should ... when ...', ...)` for cases.
- Prefer table-driven `it.each` / parameterized cases when inputs differ only by data.
- Keep `// Given`, `// When`, `// Then` comments for non-trivial scenarios.

---

## 4. Unit tests

For library and plugin utility classes:

- Focus on **observable outcomes** through the public API (return values, exceptions, generated output).
- Keep tests flat by default; add one `@Nested` group only when several cases share the same Given setup.
- Group by behavior (for example `when import path is invalid`), not by Java method name alone.
- Keep `// Given`, `// When`, `// Then` comments.
- Cover representative branches per behavior, including at least one failure path and one success path when the code branches.

Example:

```java
@DisplayName("ImportPath")
class ImportPathTest {

  @Test
  @DisplayName("should throw when path contains consecutive dots")
  void shouldThrowWhenPathContainsConsecutiveDots() {
    // Given — (none)

    // When / Then
    assertThatThrownBy(() -> new ImportPath("a..b"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Invalid import path: a..b");
  }
}
```

---

## 5. Integration tests (Spring sample)

For tests under `sample/backend` that exercise HTTP or Spring context:

- Use `@SpringBootTest` / MockMvc only when wiring is the behavior under test.
- Reset external state in `@BeforeEach` when tests share mutable resources.
- Organize by user-visible behavior (status codes, response bodies), not by one `@Nested` per controller method.

---

## 6. Parameterized tests

Use `@ParameterizedTest` when several cases differ only by inputs or collaborators:

- Put the `@MethodSource` / `@ArgumentsSource` provider directly above the related parameterized test.
- Name each case with concise BDD wording (`should ... when ...`).
- Prefer shared providers instead of duplicating setup.

---

## 7. Codegen golden tests (Go)

Golden output for Java and TypeScript protoc plugins is verified in `plugin`:

- Fixtures: proto under `lib/tests/src/proto/test/{case}/`, dumps under `lib/tests/src/main/resources/{case}/dump/test.dump`.
- Expected outputs: `plugin/testdata/java/{case}/` (Java), `plugin/testdata/ts/{case}/` (TypeScript).
- Run: `./gradlew :plugin:testGo` or `cd plugin && go test ./...`.

See [GO_TEST_CONVENTIONS.md](GO_TEST_CONVENTIONS.md) for details.

---

## 8. Test infrastructure

| Location | Role |
|----------|------|
| `lib/utilities/.../test/TestUtils.java` | Create protoc requests from dumps, read/write golden files |
| `lib/tests/.../Dump.java` | Enum of fixture cases for Java tests |
| `plugin/internal/testutil` | Load dumps and expected outputs for Go golden tests |
| `runtime/processor/.../model/*` | Compile-testing fixture sources |

Shared helpers belong in the package that owns the concern. Do not copy dump-loading or latch boilerplate into every test class.

---

## 9. Assertions and helpers

- Use AssertJ (`assertThat`, `assertThatThrownBy`, `assertThatCode`).
- Use Mockito for spies/mocks/verification when isolating collaborators.
- Prefer `usingRecursiveComparison()` for value objects with nested fields.
- Use `CountDownLatch` / `Awaitility` for async timing; avoid fixed sleeps except when measuring elapsed time is the assertion.

---

## 10. Commands

```bash
./gradlew spotlessApply   # auto-fix Java formatting — run first when Spotless fails
./gradlew build           # full check including Go plugin tests
./gradlew :plugin:testGo
cd plugin/npm/webpb-protoc-ts && npm run pack
./gradlew :lib:utilities:test
./gradlew :runtime:java:test
./gradlew :runtime:processor:test
./gradlew :sample:backend:test
```

After editing tests, run **build** (Spotless + compile + tests). Prefer `spotlessApply` before manual formatting fixes.

For TypeScript runtime and sample frontend:

```bash
./gradlew :runtime:ts:check
cd sample/frontend && npm ci && npm run check
```

---

## 11. Quick checklist

- Behavior-first: names and structure reflect outcomes/conditions, not a 1:1 map to source methods.
- Unit tests: branch coverage via observable behavior; flat by default; at most one `@Nested` level.
- Codegen goldens: Go tests in `plugin` only; update expected files under `plugin/testdata/{java,ts}/`.
- Parameterized tests: provider above the test; BDD case descriptions.
- Prefer short BDD descriptions: `should ... when ...`.
- Use AssertJ + Mockito; shared helpers instead of duplicated setup.
