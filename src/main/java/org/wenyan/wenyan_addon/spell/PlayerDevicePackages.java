package org.wenyan.wenyan_addon.spell;

import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 背包物品设备的玩家版函数包注册表：法术剑（玩家施法）调用背包设备时，
 * 优先使用注册的玩家版函数包（以玩家为施法主体，{@link org.wenyan.wenyan_addon.qi.spell.PlayerCastContext} 签名）；
 * 未注册时回落到设备自身包（通用 {@link indi.wenyan.judou.api.exec.structure.IHandleContext} 签名天然兼容玩家环境）。
 */
public final class PlayerDevicePackages {
    private static final Map<Item, Function<ItemStack, RawHandlerPackage>> PACKAGES = new HashMap<>();

    private PlayerDevicePackages() {
    }

    public static void register(Item item, Function<ItemStack, RawHandlerPackage> playerPackage) {
        PACKAGES.put(item, playerPackage);
    }

    public static Function<ItemStack, RawHandlerPackage> of(Item item) {
        return PACKAGES.get(item);
    }
}