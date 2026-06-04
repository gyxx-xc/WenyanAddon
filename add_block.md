# 添加方块 — COT

## Chain of Thought

1. **决定是否需要自定义方块类**
    - 若方块无特殊行为（交互、容器、tick 逻辑等），可直接用 `registerBlock` + 匿名 lambda 调 `.mapColor().strength().sound()`，无需单独文件。
    - 若需要自定义逻辑（如 tick、随机刻、玩家交互），创建继承 `Block` 的类并重写对应方法，继续下一步。

2. **编写自定义方块类**
    - 构造函数接收 `Properties`（`Block` 内继承的类型，非 `BlockBehaviour.Properties`），在其上链式调用 `.mapColor()` `.strength()` `.sound()` 等。
    - 切勿在注册处写 Properties；始终在方块类内部设置。
    - 重写 `tick(BlockState, ServerLevel, BlockPos, RandomSource)` 等行为方法。
    ```java
    public class MyBlock extends Block {
        public MyBlock(Properties properties) {
            super(properties
                    .mapColor(MapColor.STONE)
                    .strength(2.0f)
                    .sound(SoundType.STONE));
        }

        @Override
        public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            // 自定义 tick 逻辑
        }
    }
    ```

3. **注册方块本体**
    - 使用 `BLOCKS.registerBlock("方块id", 方块类::new)`。
    - `registerBlock` 会自动生成默认 `Properties` 传入构造函数，方块类负责在其上修饰。
    - 返回 `DeferredBlock<T>`（T 为自定义方块类型）。
    ```java
    public static final DeferredBlock<MyBlock> MY_BLOCK = BLOCKS.registerBlock("my_block", MyBlock::new);
    ```

4. **注册对应的方块物品**
    - 调用 `ITEMS.registerSimpleBlockItem("id", 步骤3的DeferredBlock对象)`。
    - 物品自动关联到方块，ID 与方块一致。
    ```java
    public static final DeferredItem<BlockItem> MY_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("my_block", MY_BLOCK);
    ```

5. **加入创造模式物品栏**
    - 在 `displayItems` lambda 中 `output.accept(方块物品.get())`。
    ```java
    output.accept(MY_BLOCK_ITEM.get());
    ```

## 额外注意事项

- **model / blockstate / loot_table**：使用数据生成（`runData`）自动产出，不需要手写 JSON。
- **Properties 一律写在方块类内部**，绝不写在注册处。
- 若方块无自定义类，直接用 `registerBlock("id", Block::new, p -> p.mapColor(...).strength(...))` 的三参数重载（`Function<Properties, Block>` + `Consumer<Properties>`）。
