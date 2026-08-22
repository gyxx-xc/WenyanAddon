package org.wenyan.wenyan_addon.qi.element;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物品属性标记注册表：从 {@link ItemAttributeConfig} 加载「物品 → 属性集合」映射。
 * 一个物品可标记多个属性（含五行/阴阳/无属性与衍生属性），
 * 作为祭坛合成仪式、淬体仪式与施法的五行属性素材来源。
 * 服务端启动时由 {@link #reload()} 加载；首次查询未加载时自动加载。
 */
public final class ItemAttributeRegistry {
    private static final Logger log = LoggerFactory.getLogger(ItemAttributeRegistry.class);

    private static volatile Map<Item, Set<ElementAttribute>> attributes = Map.of();
    private static volatile boolean loaded = false;

    private ItemAttributeRegistry() {
    }

    /**
     * 从配置文件重新加载物品属性映射。未知物品/属性 ID 记录警告并跳过。
     */
    public static void reload() {
        Map<String, List<String>> raw = ItemAttributeConfig.load();
        Map<Item, Set<ElementAttribute>> map = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.getKey()));
            if (item == null || item == Items.AIR) {
                log.warn("物品属性配置：未知物品 ID，已跳过 {}", entry.getKey());
                continue;
            }
            Set<ElementAttribute> attrs = new HashSet<>();
            for (String id : entry.getValue()) {
                ElementAttribute attribute = ElementRegistry.byId(id);
                if (attribute == null) {
                    log.warn("物品属性配置：未知元素属性 ID，已跳过 {} -> {}", entry.getKey(), id);
                    continue;
                }
                attrs.add(attribute);
            }
            if (!attrs.isEmpty()) {
                map.put(item, Set.copyOf(attrs));
            }
        }
        attributes = Map.copyOf(map);
        loaded = true;
        log.info("已加载物品属性标记：{} 种物品", attributes.size());
    }

    /**
     * 物品携带的全部属性标记（未配置返回空列表）。
     */
    public static List<ElementAttribute> of(Item item) {
        ensureLoaded();
        return attributes.getOrDefault(item, Set.of()).stream().toList();
    }

    /**
     * 物品栈携带的全部属性标记（按物品判定，与数量无关）。
     */
    public static List<ElementAttribute> of(ItemStack stack) {
        return of(stack.getItem());
    }

    /**
     * 物品是否携带指定属性标记。
     */
    public static boolean has(Item item, ElementAttribute attribute) {
        return of(item).contains(attribute);
    }

    private static void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }
}