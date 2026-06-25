# 代码规则

## 禁止使用 @SuppressWarnings

永远不要使用 `@SuppressWarnings`。如果 API 已弃用，使用非弃用的替代 API；如果编译器报警告，修复根本原因而非压制。绝不添加 `@SuppressWarnings` 注解。
