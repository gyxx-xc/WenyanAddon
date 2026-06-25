[English](#wenyan-addon) | [简体中文](#吾有一术附属)

# Wenyan Addon

Wenyan Addon is an experimental companion mod for the Wenyan ecosystem.
It is built to move faster than the core Wenyan Programming mod, test unusual ideas, and ship features that may not yet fit into the main project.

The addon is currently in active development. APIs, gameplay details, data formats, and compatibility behavior may change without notice.

## Features

- **Fast Feature Iteration**: New mechanics and integrations can be prototyped here before they are suitable for the core mod.
- **Beyond Core Limits**: Wenyan Addon explores features that intentionally go outside the safer boundaries of Wenyan Programming.
- **Distinct Gameplay Experiments**: The addon can host more opinionated or specialized content, including built-in side modules such as Pong.
- **Wenyan Ecosystem Integration**: Designed as a place for future interaction with Wenyan Nature and Wenyan Programming.

## Stability Notice

Wenyan Addon does not guarantee code stability.

This repository may contain experimental code, temporary APIs, migration work, and features that are useful but not yet polished. Use it with the expectation that saves, configs, scripts, or integrations may need adjustment between versions.

## Relationship With Wenyan Programming

Wenyan Programming remains the stable core. Wenyan Addon is the experimental extension layer:

- features can be added here faster;
- risky or highly specific ideas can be tested here first;
- core limitations can be bypassed when the addon needs a sharper gameplay direction;
- successful ideas may later inform work in the wider Wenyan ecosystem.

## Development

Target environment:

- Minecraft `26.1.2`
- NeoForge `26.1.2.71`
- Java `25`

Useful commands:

```bash
./gradlew compileJava
./gradlew runData
./gradlew build
```

## License

This project is licensed under the [MIT License](LICENSE).

---

# 吾有一术：附属

Wenyan Addon 是面向“吾有一术”生态的实验性附属模组。
它的定位不是替代 Wenyan Programming 本体，而是在本体之外提供一个更快、更大胆、更容易试错的功能扩展层。

本项目仍在活跃开发中。API、玩法细节、数据格式和兼容行为都可能随版本调整。

## 特色

- **快速迭代新功能**：适合先行实现和验证尚未适合进入本体的新机制。
- **突破本体限制**：允许尝试 Wenyan Programming 本体中不方便直接实现的交互和玩法。
- **更有特色的内容实验**：可以承载更偏实验性、主题性或独立模块化的内容，例如内置的 Pong 模块。
- **面向 Wenyan 生态联动**：作为后续与 Wenyan Nature、Wenyan Programming 交互的扩展入口。

## 稳定性说明

Wenyan Addon 不保证代码稳定性。

本仓库可能包含实验代码、临时 API、迁移中的实现，以及“能用但还不优雅”的功能。使用时请预期不同版本之间可能需要调整存档、配置、脚本或联动代码。

## 与 Wenyan Programming 的关系

Wenyan Programming 是更稳定的本体；Wenyan Addon 是实验扩展层：

- 新功能可以更快落地；
- 风险更高或更具体的想法可以先在这里验证；
- 当玩法需要更鲜明的方向时，可以突破本体的一些限制；
- 成熟的实验结果未来可以反哺整个 Wenyan 生态。

## 开发

目标环境：

- Minecraft `26.1.2`
- NeoForge `26.1.2.71`
- Java `25`

常用命令：

```bash
./gradlew compileJava
./gradlew runData
./gradlew build
```

## 许可

本项目基于 [MIT License](LICENSE) 发布。
