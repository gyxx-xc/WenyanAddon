# 文言附属模组 — 新增 5 个功能方块计划

## TL;DR (For humans)
为文言附属模组新增 5 个方块及其文言函数集，覆盖：玩家状态操作、实体生成/伤害、药水效果、方块编辑、附魔。

## 实现策略
每个功能域 = 1个新方块 + 1个Handler类 + Capabilities注册 + 加入创造模式物品栏。

---

## 任务列表

### 1. 方块定义与注册 (WenyanAddon.java)

##### ✅ T1: 在 WenyanAddon.java 中注册 5 个新方块

**接受条件：**
- 每个方块有 `DeferredBlock<Block>` + `DeferredItem<BlockItem>` 静态字段
- 方块使用合适的 MapColor 和 SoundType
- 新方块加入创造模式物品栏的 `displayItems` lambda 中
- 5 个方块的基本属性：
  - `entity_status_block` — MapColor.COLOR_RED, SoundType.STONE (愈)
  - `entity_spawn_block` — MapColor.COLOR_PURPLE, SoundType.STONE (召)
  - `potion_block` — MapColor.COLOR_CYAN, SoundType.GLASS (藥)
  - `block_edit_block` — MapColor.STONE, SoundType.STONE (地)
  - `enchant_block` — MapColor.COLOR_YELLOW, SoundType.STONE (靈)

**参考文件：** `WenyanAddon.java` 中现有的 `ENTITY_MANIPULATION_BLOCK` 模式

##### ✅ T2: 新 Handler 类：EntityStatusHandlers.java
路径：`src/main/java/org/wenyan/wenyan_addon/device/handler/EntityStatusHandlers.java`

注册 4 个文言函数到 `entity_status_block`：
| 文言函数 | 参数 | 功能 |
|---------|------|------|
| 「療」 | 无 | 将方块相邻的玩家恢复满生命值 |
| 「飽」 | 无 | 将方块相邻的玩家恢复满饱食度 |
| 「賜經驗」 | level: double | 给予相邻玩家指定数量经验等级 |
| 「告」 | msg: string | 向相邻玩家发送聊天消息 |

**实现方案：** 获取方块周围 1.5 格半径内最近的 `Player`，调用对应方法。
- 治疗：`player.heal(player.getMaxHealth())`
- 饱食：`player.getFoodData().eat(20, 20)`
- 经验：`player.giveExperienceLevels((int) args.get(0))`
- 消息：`player.sendSystemMessage(Component.literal(args.get(0)))`

##### ✅ T3: 新 Handler 类：SpawnHandlers.java
路径：`src/main/java/org/wenyan/wenyan_addon/device/handler/SpawnHandlers.java`

注册 2 个文言函数到 `entity_spawn_block`：
| 文言函数 | 参数 | 功能 |
|---------|------|------|
| 「召」 | type: string, x: double, y: double, z: double | 在相对坐标处生成指定类型的实体 |
| 「傷」 | x, y, z, amount | 对范围内实体造成伤害 |

**实现方案：**
- 「召」：使用 `BuiltInRegistries.ENTITY_TYPE` 根据名称获取 `EntityType`，然后 `entityType.spawn()`
- 「傷」：获取方块相对坐标周围 0.5 格内的实体，调用 `entity.hurt()`

##### ✅ T4: 新 Handler 类：PotionHandlers.java
路径：`src/main/java/org/wenyan/wenyan_addon/device/handler/PotionHandlers.java`

注册 2 个文言函数到 `potion_block`：
| 文言函数 | 参数 | 功能 |
|---------|------|------|
| 「賜效」 | effect: string, duration: double, amplifier: double | 给相邻实体添加药水效果 |
| 「驅效」 | effect: string | 移除指定药水效果 |

**实现方案：**
- `BuiltInRegistries.MOB_EFFECT` 根据名称获取 `MobEffect`
- `livingEntity.addEffect(new MobEffectInstance(effect, duration, amplifier))`
- `livingEntity.removeEffect(effect)`

##### ✅ T5: 新 Handler 类：BlockEditHandlers.java
路径：`src/main/java/org/wenyan/wenyan_addon/device/handler/BlockEditHandlers.java`

注册 3 个文言函数到 `block_edit_block`：
| 文言函数 | 参数 | 功能 |
|---------|------|------|
| 「置」 | x, y, z, block_id: string | 在相对坐标处放置指定方块 |
| 「毀」 | x, y, z | 破坏相对坐标处的方块 |
| 「替」 | x, y, z, to_block_id: string | 替换方块（保持原方块状态属性） |

**实现方案：**
- `BuiltInRegistries.BLOCK` 根据名称获取 `Block`
- `level.setBlock(pos, block.defaultBlockState(), 3)`
- `level.destroyBlock(pos, true)`
- `level.setBlock(pos, targetBlock.withPropertiesOf(oldState), 3)`

##### ✅ T6: 新 Handler 类：EnchantHandlers.java
路径：`src/main/java/org/wenyan/wenyan_addon/device/handler/EnchantHandlers.java`

注册 2 个文言函数到 `enchant_block`：
| 文言函数 | 参数 | 功能 |
|---------|------|------|
| 「附靈」 | enchant: string, level: double | 附魔玩家主手物品 |
| 「去靈」 | enchant: string | 去除玩家主手物品的指定附魔 |

**实现方案：**
- `BuiltInRegistries.ENCHANTMENT` 根据名称获取 `Enchantment`
- `itemStack.enchant(enchantment, level)`
- 需要获取方块前最近的玩家，操作其主手物品

##### ✅ T7: 在 Capabilities.java 中注册所有新方块的能力

在 `Capabilities.registerCapabilities()` 中为 5 个新方块注册对应的函数包。

---

## Final Verification Wave

### ✅ F1: 构建检查
- ✅ `./gradlew build` 通过 (0 errors, only 2 pre-existing deprecation warnings in StorageRuneBlockEntity)

### ✅ F2: 代码审查
- ✅ 所有新文件符合 ADDON 模板风格
- ✅ 无硬编码魔法值（使用 `# CONFIG` 注释标注可配置项）
- ✅ 所有外部依赖用 `# NEED` 注释标注
- ✅ 无 TODO/FIXME/HACK/xxx 残留

### ✅ F3: 功能验证
- ✅ 每个方块在创造模式物品栏中可见（通过 `displayItems` 添加）
- ✅ 每个文言函数遵循现有 handler 模式，可被正确调用
- ✅ 参数解析使用 `WenyanArgsResolver`，与现有实现一致

### ✅ F4: 文言 API 兼容性
- ✅ 使用的 API 在 wenyan_programming-1.0.0.jar 中确实存在
- ✅ 没有引入多余的外部依赖
