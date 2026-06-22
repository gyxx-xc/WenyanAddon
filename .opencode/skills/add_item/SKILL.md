---
name: add_item
description: Use when adding or registering custom Minecraft items, registering simple items, creating item models, or adding items to creative mode inventory. Triggers: add item, register item, custom item, Item, DeferredItem, registerSimpleItem, item model, creative tab, item texture.
---

# 添加物品

1. **注册物品**
   - 使用 `ITEMS.registerSimpleItem("item_id")` 注册简单物品。
   - 返回 `DeferredItem<Item>`。
   ```java
   public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item");
   ```

2. **加入创造模式物品栏**
   - 在 `displayItems` lambda 中 `output.accept(物品.get())`。
   ```java
   output.accept(EXAMPLE_ITEM.get());
   ```

3. **添加语言文件**
   - 在 `assets/<modid>/lang/en_us.json` 和 `zh_cn.json` 中添加翻译。
   ```json
   "item.wenyan_addon.example_item": "Example Item"
   ```

4. **构建验证**
   - 运行 `./gradlew build` 确保编译通过。

## 注意事项
- 若需自定义物品行为（右键交互、耐久等），创建继承 `Item` 的类并使用 `ITEMS.register("item_id", () -> new MyItem(new Item.Properties()))`。
- 若无自定义纹理，可复用 `minecraft:item/paper` 等原版纹理作为占位。
- 添加 item 后务必更新 en_us.json 和 zh_cn.json。
