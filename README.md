# 将 Neoforge MDK 修改为文言附属模组的方法

本仓库从 Neoforge MDK (Mod Development Kit) 模板出发，经过以下步骤修改为文言编程语言的附属模组模板。

---

## 1. 添加文言编程依赖

### 1.1 放置 jar 包

将以下依赖 jar 放入 `libs/` 目录：

- `libs/wenyan_programming-1.0.0.jar` — 文言编程模组本体

### 1.2 修改 `build.gradle`

在 `repositories` 块中添加：
```groovy
    flatDir {
    dirs 'libs'
}
```

在 `dependencies` 块中添加：

```groovy
implementation "indi.wenyan:judou:1.0.0"
implementation "indi.wenyan:wenyan_programming:1.0.0"

runtimeOnly "org.antlr:antlr4-runtime:4.13.1"
runtimeOnly "com.github.houbb:opencc4j:1.14.0"
```

---

## 2. 修改 `gradle.properties`

`mod_id=wenyan_addon`、`mod_name=Wenyan Addon` 等按需调整。

---

## 3. 修改 `neoforge.mods.toml`

### 3.1 添加文言编程作为必需前置

在依赖列表末尾新增：

```toml
[[dependencies.${mod_id}]]
modId = "wenyan_programming"
type = "required"
versionRange = "[1.0.0,)"
ordering = "NONE"
side = "BOTH"
```

---

## 5. 创建 `Capabilities.java` — 注册文言方块设备的函数

新建 `src/main/java/org/wenyan/wenyan_addon/Capabilities.java`，核心内容：

### 5.1 事件订阅

```java
@EventBusSubscriber(modid = MODID)
public enum Capabilities {
    ;

    @SubscribeEvent
    public static void registerCapabilities(@NonNull RegisterCapabilitiesEvent event) {
        event.registerBlock(
            WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
            simpleDevice("包名", handlerPackage),
            目标方块...  // 可注册到多个方块
        );
    }
}
```

### 5.2 创建设备提供者

使用 `simpleDevice(String name, RawHandlerPackage handlerPackage)` 工厂方法，返回 `IBlockCapabilityProvider<IWenyanBlockDevice, Void>` 实现，负责：

- 提供方块的 `BlockState` 和 `BlockPos`
- 通过 `getExecPackage()` 返回文言函数包（`RawHandlerPackage`）
- 通过 `getPackageName()` 返回文言风格的包名（自动加书名号「」）

### 5.3 构建文言函数包

使用 `HandlerPackageBuilder` 定义可在文言脚本中调用的函数：

```java
HandlerPackageBuilder.create()
    .handler(ChineseUtils.bracketOf("函数名"), ctx -> {
        // 函数实现
    })
    .build()
```

---

## 7. 其他调整

根据[neoforged 文档](https://docs.neoforged.net/)按需要调整
