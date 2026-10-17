---
name: upgrade-deps
description: >-
  升级 webpb 仓库全部依赖（Gradle + npm）。用户要求升级依赖、dependencyUpdates、
  npm-check-updates、ncu、bump deps、更新 package.json / gradle.properties 版本时使用。
  升级后必须跑 lint/build 与测试并修到通过。
---

# 升级所有依赖（webpb）

在仓库根目录执行。升级后**必须** lint/build + 测试通过再收尾；未要求时不要 commit / push。

## 进度清单

```
- [ ] 1. Gradle：dependencyUpdates → 更新版本
- [ ] 2. npm：runtime/ts + sample/frontend
- [ ] 3. 安装 lockfile（各 npm 根）
- [ ] 4. lint / build（含 typecheck）
- [ ] 5. 测试（含 Go 插件）
- [ ] 6. 修复失败直至通过；向用户汇报
```

---

## 1. Gradle 依赖

检查可用更新：

```bash
./gradlew dependencyUpdates --no-parallel
```

根据报告，在根目录 [`gradle.properties`](../../../gradle.properties)（及必要时 `buildSrc` / 子模块声明）中把可升级的 `version*` 升到报告中的稳定版本。

约定：

- 版本集中在 `gradle.properties` 的 `versionXxx = …`
- 已配置拒绝「当前稳定 → 候选非稳定」（见 [`conventions.versioning.gradle.kts`](../../../buildSrc/src/main/kotlin/conventions.versioning.gradle.kts)）；优先升稳定版
- 改完后若涉及 protobuf / webpb / 生成物相关依赖，按需重新生成：
  - `./gradlew build`（含各模块 `generateProto`）
  - `sample/frontend` 的 `npm run proto`（webpb CLI 生成物）
- Gradle 始终在仓库根跑 `./gradlew`，不要 `cd` 到子模块

---

## 2. npm 依赖

本仓库独立 npm 根（各自有 `package.json` / lockfile；仓库根已无 npm 管理，仅 Gradle + shell hooks）：

| 目录 | 命令 |
|------|------|
| `runtime/ts` | `npx npm-check-updates -u` |
| `sample/frontend` | `npx npm-check-updates -u` |

每个根目录升级 `package.json` 后执行对应 `npm install`，提交 lockfile 变更。

---

## 3. 验证（必须）

按 [`ai-kit`](../../../ai-kit/) 约定：源码或测试相关依赖变更后，跑对应 lint/build；失败先尝试 auto-fix。

### lint / build（含 typecheck）

```bash
# TypeScript runtime
cd runtime/ts && npm run check && npm run build

# Sample frontend（check 已含 proto + lint + test）
cd sample/frontend && npm run check

# 后端（含 Spotless check）
./gradlew spotlessApply && ./gradlew build -PskipGoTest=true
```

前端 lint 失败时先试 `npm run lint:fix`；后端格式失败时先跑 `./gradlew spotlessApply`。

若只改了某一侧，至少对该侧跑上述检查；全量升级则 runtime + frontend + 后端都跑。

### 测试

```bash
# 后端（含 Go 插件单测，Windows 外）
./gradlew build
cd plugin && go test ./...

# TypeScript / 前端（如 check 未覆盖）
cd runtime/ts && npm test
cd sample/frontend && npm test
```

失败则修复（兼容性改动、peer 冲突、API breaking）后重跑，直到通过。无法安全升级的包：回退该包版本并在汇报中说明原因。

---

## 4. 汇报

向用户简要说明：

1. Gradle：升了哪些 `version*`（或「无可用稳定更新」）
2. npm：各根目录主要 bump
3. lint / build / 测试结果
4. 刻意跳过或回退的依赖及原因

---

## 注意

- Gradle 始终在仓库根跑 `./gradlew`，不要 `cd` 到子模块
- 大版本 breaking 时优先修代码适配；实在不合适则 pin 并说明
- 临时探测输出放 `.tmp/upgrade-deps/`，勿写进源码树 / `.tmp/` 之外位置
- 改完依赖后若涉及 protobuf / 生成物，按需重新生成（见 §1）
- 提交依赖升级用本仓 scope `deps`（例如 `chore(deps): bump spring-boot to 4.2`）；未要求时不要 commit / push
