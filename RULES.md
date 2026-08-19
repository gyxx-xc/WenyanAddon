# 代码规则

## 禁止使用 @SuppressWarnings

永远不要使用 `@SuppressWarnings`。如果 API 已弃用，使用非弃用的替代 API；如果编译器报警告，修复根本原因而非压制。绝不添加 `@SuppressWarnings` 注解。

## 禁止自行编译验证

不要运行构建、检查编译产物、对比时间戳或反编译 class 来验证代码。编译验证结果由用户提供，以用户的验证结果为准。

## 功能实现范围：仅在 WenyanAddon 中实现

所有功能实现必须位于包 `org.wenyan.wenyan_addon`（服务端/公共逻辑）或 `org.wenyan.wenyan_addon` 的客户端源集（`src/client/java/org/wenyan/wenyan_addon`）之内。

- 不得新建诸如 `org.wenyan.pong` 之类的独立功能子模块。
- 新增方块、物品、实体、GUI、Mixin、数据生成等内容，一律放入 `wenyan_addon` 对应功能子包（如 `device`、`qi`、`item`）。
- 若需要上提至 [Wenyan Programming](https://github.com/gyxx-xc/WenyanNature) 本体，应提交到本体仓库，而不是在当前仓库中另立新包。
