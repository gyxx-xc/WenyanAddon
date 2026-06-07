---
name: wenyan_function
description: Use when binding Wenyan (文言) functions to Minecraft blocks as Wenyan block devices, creating handler packages, or registering capabilities. Triggers: wenyan, 文言, function, crush, HandlerPackageBuilder, simpleDevice, RawHandlerPackage, RegisterCapabilitiesEvent, WENYAN_BLOCK_DEVICE_CAPABILITY, WyRegistration, ChineseUtils.bracketOf, IWenyanBlockDevice, block device, capability.
---

# 将函数附加到方块上 — COT

## 目标
将文言函数（如「crush」）绑定到 Minecraft 方块上，使方块成为可被文言脚本操纵的「文言方块设备」。

---

## 步骤

### Step 1: 构建函数包（RawHandlerPackage）

使用 `HandlerPackageBuilder` 构建一组文言函数：

```
HandlerPackageBuilder.create()
    .handler(函数名, 函数体)
    .build()
```

- **函数名**：需要用 `ChineseUtils.bracketOf("...")` 包裹，自动添加文言书名号「」。
- **函数体**：一个 lambda，接收上下文参数，执行具体逻辑（如抛出异常、打印日志等）。

---

### Step 2: 创建方块能力提供者

调用工厂方法 `simpleDevice(包名, handlerPackage)`：

```
simpleDevice("包名", handlerPackage)
```

- **包名**：设备名/包名（不加书名号），`getPackageName()` 会自动用 `ChineseUtils.bracketOf()` 添加「」包裹。
- **返回值**：一个 `IBlockCapabilityProvider<IWenyanBlockDevice, Void>` 实现。
  - 内部通过匿名类实现 `IWenyanBlockDevice`，自动注入方块的 `BlockState`、`BlockPos` 等信息。
  - `isRemoved()` 固定返回 `false`。
  - `getExecPackage()` 返回之前构建的 `RawHandlerPackage`。

---

### Step 3: 注册到方块

在 `RegisterCapabilitiesEvent` 中调用 `event.registerBlock()`：

```
event.registerBlock(
    WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,  // 能力键
    provider,                                         // Step 2 的产物
    目标方块1, 目标方块2, ...                          // 可变参数
)
```

- **能力键**：固定使用 `WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY`。
- **provider**：`simpleDevice()` 的返回值。
- **目标方块**：可以是模组自定义方块（如 `WenyanAddon.EXAMPLE_BLOCK.get()`）或原版方块（如 `Blocks.BEDROCK`）。

---

## 完整示例

```java
@SubscribeEvent
public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlock(
        WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
        simpleDevice("crush game",
            HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("crush"), _ -> {
                    throw new NullPointerException();
                })
                .build()
        ),
        WenyanAddon.EXAMPLE_BLOCK.get(),
        Blocks.BEDROCK
    );
}
```

此例将名为「crush game」的包（含函数「crush」）绑定到 `EXAMPLE_BLOCK` 和 `BEDROCK` 上。

---

## 关键类/接口速查

| 类/接口 | 作用 |
|---------|------|
| `RegisterCapabilitiesEvent` | Neoforge 能力注册事件，通过 `registerBlock()` 绑定 |
| `IBlockCapabilityProvider<T, Void>` | 方块能力提供者，由 `simpleDevice()` 生成 |
| `IWenyanBlockDevice` | 文言方块设备接口，提供 blockState/blockPos/package 等 |
| `HandlerPackageBuilder` | 构建 `RawHandlerPackage`（函数包） |
| `RawHandlerPackage` | 函数包的最终形态，内含多个 handler |
| `ChineseUtils.bracketOf()` | 给名称添加文言书名号「」 |
