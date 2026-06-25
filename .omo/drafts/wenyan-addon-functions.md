# Wenyan Addon Functions - 决策记录

## 状态
status: awaiting-approval

## 背景
为文言附属模组新增 5 个功能方块（剔除本体已包含的「時」「榜」功能）。

## 决策记录
- 每个功能域一个单独的 Handler 类，按现有 `EntityHandlers.java` 模式
- 所有方块使用 `DeferredBlock<Block>`，不需要自定义方块类（无特殊行为）
- 参数解析使用现有的 `BlockHandlerHelper.singleVec3ArgsSpec` 模式 + `WenyanArgsResolver`
- 实体获取：以方块为中心，1.5 格半径范围查找最近的玩家实体
- 方块 ID / 实体类型 ID / 附魔 ID / 效果 ID：使用 Minecraft `BuiltInRegistries` 按字符串名称查找

## 不实施的内容
- 「時」(时间) — 用户排除
- 「榜」(记分板) — 用户排除
- 所有文言本体已提供的功能 — 已在之前列表中排除

## 待审批操作
写入 `.omo/plans/wenyan-addon-functions.md` 并执行。
