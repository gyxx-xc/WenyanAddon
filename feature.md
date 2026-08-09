# Wenyan Addon 功能总结

## 项目概览

Wenyan Addon（吾有一术：新秩序）是「吾有一术」生态的实验性附属模组，基于 [Wenyan Programming](https://github.com/gyxx-xc/WenyanNature) 本体构建，用于测试和提供新颖、特别的功能扩展。稳定的功能会被提升至本体。

本项目采用快速迭代模式，包含不稳定代码，功能可能随时被删除或修改。

- **目标环境**：Minecraft `26.1.2` / NeoForge `26.1.2.71` / Java `25`
- **内含模块**：`wenyan_addon`（文言功能扩展）、`pong`（独立香槟小游戏）

---

## 一、文言功能方块

每个功能方块通过 capability 绑定一组文言函数（Handler 包），玩家可用文言脚本调用。部分方块同时提供「物品投掷」版本的函数（以 `ThrowEntityContext` 为上下文）。

### 功能方块总览

| 方块 ID | 名称 | 文言函数 | 功能 |
|---------|------|---------|------|
| `example_block` | 範例石 | 「example」 | 示例功能入口 |
| `projectile_spawner_block` | 投射石 | 「箭」「煙火」「雪丸」「火丸」 | 生成箭、烟花火箭、雪球、小火球 |
| `fluid_block` | 流体石 | 「水源」「熔岩」「清除流体」「冻水成冰」 | 放置水/熔岩、清除流体、冻结水成冰 |
| `world_interaction_block` | 交感石 | 「催生」「点燃」「熄灭」 | 骨粉催生、点燃火焰、扑灭火焰 |
| `entity_manipulation_block` | 移形石 | 「传送」「闪」「施力」 | 相对传送、视线瞬移、施加动量 |
| `music_block` | 音符石 | 「奏乐」 | 演奏指定音高的音符盒音效 |
| `read_write_block` | 文本读写石 | 「读」「写」 | 读取/写入告示牌、讲台书籍文本 |
| `naming_block` | 命名石 | 「命名」 | 为指定位置的实体命名 |
| `particle_block` | 粒子石 | 「粒子放出」 | 生成指定颜色和数量的尘埃粒子 |
| `dye_block` | 染色石 | 「染白」…「染黑」（16 色） | 对羊、告示牌、方块染色 |
| `marker_block` | 标记石 | 「标点」「警」「往」「敌」 | 向玩家发送普通/警告/前往/敌意坐标标记 |
| `potion_block` | 药水石 | 「给予效果」「祛除效果」 | 为实体添加/移除药水效果 |
| `block_edit_block` | 方块操作石 | 「置」「破」「替」 | 放置、破坏、替换方块（保留属性） |
| `enchant_block` | 附魔石 | 「附魔」「祛魔」 | 为附近玩家主手物品附魔/祛魔 |
| `message_block` | 消息石 | 「告」 | 向范围内玩家发送消息 |
| `time_block` | 时间石 | 「时间戳」「游戏刻」「时间」 | 系统时间戳、服务器游戏刻、主世界时间 |

> 注：`entity_status_block`、`entity_spawn_block` 已注册但未加入创造模式物品栏。

---

## 二、数据磁盘系统（数据持久化）

- **数据磁盤**（`data_disk`）：持久化数据存储物品，每个磁盘拥有唯一 UUID。
- **符咒收纳柜**（`storage_rune_block`）：可存放 9 张数据磁盘的方块，带 GUI 容器界面（`StorageRuneMenu` / `StorageRuneScreen`），方块被破坏时掉落磁盘。
- **存储方式**：数据以 NBT 文件保存至世界存档目录 `wenyan_addon/data_disks/<uuid>.nbt`，使用临时文件 + 原子移动写入。
- **NBT 编解码**（`WenyanNbtCodec`）：文言值 ↔ NBT 双向转换，支持 `bool / int / num / str / list / map / vec3 / block / entity / player / itemslot / runner` 等类型。
- **新值类型**：`WenyanMapValue`（「圖」），提供「取」「置」「有」「删」「鍵」「長」等属性操作。
- **文言函数**：「列出」（列出磁盘 UUID）、「读取磁盘」「写入磁盘」（按槽位读写数据）、「磁盘数」（已插入磁盘数量）。

---

## 三、Pong 模块（香槟小游戏）

独立小游戏模块，模拟香槟开瓶、倾倒、饮用的完整流程。

| 物品/方块 | 名称 | 功能 |
|-----------|------|------|
| `champagne_bottle` | 香槟瓶 | 手持摇晃蓄压，模型随压力分阶段变化；可用香槟刀开瓶 |
| `champagne_sabre` | 香槟刀 | 与未开封香槟瓶一同手持即可开瓶 |
| `goblet` | 香槟杯 | 从已开封香槟瓶中倒酒，饮用后获得醉意 |
| `plug` | 香槟塞 | 开瓶时飞出的软木塞投射物（`PlugEntity`） |
| `champagne_rack` | 香槟架 | 存放并展示至多四瓶未开封香槟；被投射物击中或收到红石信号时爆炸释放香槟 |
| `champagne_fluid_block` | 香槟 | 香槟液体方块（源/流动） |
| `drunk` | 酒醉身姿似百合 | 醉酒效果，随饮酒量递进：加速 → 急迫/跳跃 → 反胃/虚弱/霉运 → 死亡 |
| `splash_particles` | 飞溅粒子 | 开瓶时的香槟飞溅粒子效果 |
| `champagne_open` | 开瓶音效 | 开瓶时的专用音效 |
| `debug_rod` | 调试棒 | 开发工具，可触发香槟架爆炸 |

---

## 四、客户端与基础设施

- **GUI**：符咒收纳柜容器界面（`StorageRuneScreen`）
- **渲染**：香槟架渲染（`RackRender`）、香槟塞渲染（`PlugRender`）
- **创造模式物品栏**：独立的「吾有一術：新秩序」标签页，包含所有功能方块、数据磁盘及 Pong 模块物品
- **数据生成**：方块状态、模型、语言文件（简中/繁中）、配方、物品标签
- **错误处理**：文言异常文本均通过 i18n 本地化（如 `wenyan_addon.error.no_loading` 等）
