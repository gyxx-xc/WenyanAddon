# AGENTS.md — Wenyan Nature

> 激进的使用skill, 有很多有用的其他信息

## 项目概览
Minecraft NeoForge 模组，让玩家用文言文语言在游戏中编写魔法程序。
- **Mod ID**: `wenyan_programming`  |  **Group**: `indi.wenyan`
- **Java 25**  |  **Minecraft 26.1**  |  **NeoForge 26.1.2**

## 构建与运行

**禁止直接执行 `./gradlew`。** 始终使用 MCP 工具（`local-server_build_project` / `local-server_execute_run_configuration`）。

```bash
# 验证编译——用 MCP 构建（等价于 ./gradlew build，但用工具）
local-server_build_project  # 编译整个项目

# 仅编译改动的文件
local-server_build_project(filesToRebuild=["path/to/ChangedFile.java"])
```

## 代码风格

- **接口命名**：`I` 前缀（`IWenyanValue`、`IWenyanFunction`）
- **领域类命名**：`Wenyan` 前缀（`WenyanCompiler`、`WenyanFrame`）
- **值对象**：优先使用 `record`，而非 class
- **Switch**：使用 switch 表达式（Java 17+），不用传统 switch 语句
- **Nullability**：参数和返回值使用 `@NotNull`/`@Nullable`（JetBrains）
- **Lombok**：`@Getter`/`@Setter` 仅限 GUI/DTO 代码，核心逻辑中禁用
- **异常**：定义自定义异常层次；方法显式声明 `throws`
- **注释**：核心库（judou）对公开 API 写 Javadoc；业务代码（src/main）注释稀疏
- **命名**：标准英文，标识符中不含拼音/中文
- **包结构**：按功能领域划分（compiler, runtime, utils），而非技术分层

## 所有查 Neoforge/Minecraft 代码

目标：搞清楚 `Either` 怎么用，有哪些方法，签名是什么。

```
SnippetWidget 里 import 了 Either，行3列38。
让我查一下这玩意儿的定义：

local-server_get_symbol_info(filePath="SnippetWidget.java", line=3, column=38)

→ 返回了 abstract class Either<L, R>，在 datafixerupper 的 sources jar 里。
  好的，直接读完整源码看看都有什么方法：

local-server_read_file(file_path=".../datafixerupper-9.0.19-sources.jar!/com/mojang/datafixers/util/Either.java")

→ 248 行，看到了。
  抽象方法有：map(), ifLeft(), ifRight(), left(), right(), mapBoth()
  默认方法有：mapLeft(), mapRight(), swap(), flatMap(), orThrow()
  静态工厂有：left(), right()
  还有个 static unwrap() 可以把 Either<U,U> 拆成 U

  最关键的是 map():
    <T> T map(Function<L, T> l, Function<R, T> r)
  两边射成同一个类型——这就是 Either 的 fold 操作。

  ifLeft / ifRight 返回 this，可以链式调用。
  left() / right() 返回 Optional，不想用 Consumer 的话可以直接取值。

现在我想看看 Either 实现的 App 接口是什么：
local-server_search_symbol(q="com.mojang.datafixers.kinds.App")

→ 找到了，去看一眼。

如果后续开发中我只想快速确认 ifRight 的签名：
local-server_search_symbol(q="Either.ifRight")
→ 定位到方法声明。
```
