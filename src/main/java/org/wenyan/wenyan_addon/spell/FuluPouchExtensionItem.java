package org.wenyan.wenyan_addon.spell;

import net.minecraft.world.item.Item;

/**
 * 符咒拓展包：可存放符咒与符咒石的容器。
 * 与符咒包不同：运行时可被扫描（SpellEnvironmentScanner），
 * 其内部符咒/符咒石的代码会编译为环境函数（IMPORT 可导入）。
 */
public class FuluPouchExtensionItem extends FuluPouchItem {
    public FuluPouchExtensionItem(Item.Properties properties) {
        super(properties);
    }
}