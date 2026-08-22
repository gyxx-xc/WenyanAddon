package org.wenyan.wenyan_addon.spell;

import indi.wenyan.content.block.IWenyanDevice;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 运行时环境扫描：扫描玩家背包中符咒拓展包（FuluPouchExtensionItem）内的符咒/符咒石，
 * 收集其代码与设备函数包，构建「包名 → 代码」与「包名 → 设备」映射，
 * 供法术运行时通过观（IMPORT）导入为环境函数。
 * 黑名单中的物品被排除。符咒包（FuluPouchItem）不被扫描（计划规定）。
 */
public final class SpellEnvironmentScanner {
    private static final Logger log = LoggerFactory.getLogger(SpellEnvironmentScanner.class);

    private SpellEnvironmentScanner() {
    }

    /**
     * 扫描玩家全部物品（主背包+装备栏）中的符咒拓展包，收集其内部符咒代码与设备。
     * 只读操作，需在主线程调用（背包访问非线程安全）。
     */
    public static ScanResult scan(ServerPlayer player) {
        Map<String, String> scrolls = new HashMap<>();
        Map<String, DeviceEntry> devices = new HashMap<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof FuluPouchExtensionItem) {
                collectExtension(stack, scrolls, devices);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Spell scan for {}: scrolls={} devices={}", player.getGameProfile().name(), scrolls.keySet(), devices.keySet());
        }
        return new ScanResult(scrolls, devices);
    }

    private static void collectExtension(ItemStack pouchStack, Map<String, String> scrolls, Map<String, DeviceEntry> devices) {
        FuluPouchComponent component = pouchStack.get(SpellDataComponent.POUCH_DATA.get());
        if (component == null) {
            log.debug("Extension pouch without POUCH_DATA component");
            return;
        }
        for (ItemStack stack : component.createItems()) {
            if (stack.isEmpty() || SpellBlacklist.isBanned(stack.getItem())) {
                continue;
            }
            // 设备（符咒石等文言设备）优先：包名取设备注册名（CUSTOM_NAME 或注册 deviceName）
            IWenyanDevice device = stack.getCapability(WyRegistration.WENYAN_ITEM_DEVICE_CAPABILITY);
            if (device != null) {
                String deviceName = device.getPackageName();
                devices.putIfAbsent(deviceName, new DeviceEntry(device, stack));
                log.debug("Scanned device '{}'", deviceName);
                continue;
            }
            // 代码载体（符咒）：包名取平台名（CUSTOM_NAME 或「物品名」）
            if (SpellCodeHelper.isCodeCarrier(stack)) {
                String name = FuluPouchItem.packageNameOf(stack);
                String code = SpellCodeHelper.readCode(stack);
                if (!code.isBlank()) {
                    scrolls.putIfAbsent(name, code);
                    log.debug("Scanned scroll '{}' -> code ({} chars)", name, code.length());
                }
            }
        }
    }

    /**
     * 扫描结果：拓展包内符咒代码映射与符咒石（文言设备）映射。
     */
    public record ScanResult(Map<String, String> scrollPackages, Map<String, DeviceEntry> devicePackages) {
    }

    /**
     * 背包设备条目：设备实例 + 所属物品栈（玩家版函数包按物品查询）。
     */
    public record DeviceEntry(IWenyanDevice device, ItemStack stack) {
    }
}