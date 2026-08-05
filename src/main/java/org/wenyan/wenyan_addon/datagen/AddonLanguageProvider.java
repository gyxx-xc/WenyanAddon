package org.wenyan.wenyan_addon.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.wenyan.wenyan_addon.WenyanAddon;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AddonLanguageProvider extends LanguageProvider {
    private static final Map<String, String> BLOCK_NAMES = new LinkedHashMap<>();
    private static final Map<String, String> ITEM_TOOLTIPS = new LinkedHashMap<>();
    private static final Map<String, String> ITEM_NAMES = new LinkedHashMap<>();

    static {
        BLOCK_NAMES.put("example_block", "範例石");
        BLOCK_NAMES.put("projectile_spawner_block", "投射石");
        BLOCK_NAMES.put("elemental_block", "元素石");
        BLOCK_NAMES.put("world_interaction_block", "交感石");
        BLOCK_NAMES.put("entity_manipulation_block", "移形石");
        BLOCK_NAMES.put("note_block_function_block", "音符石");
        BLOCK_NAMES.put("read_write_block", "符咒讀寫石");
        BLOCK_NAMES.put("naming_block", "命名石");
        BLOCK_NAMES.put("particle_block", "微塵石");
        BLOCK_NAMES.put("dye_block", "染色石");
        BLOCK_NAMES.put("marker_block", "標記石");
        BLOCK_NAMES.put("entity_status_block", "實體狀態石");
        BLOCK_NAMES.put("entity_spawn_block", "實體召喚石");
        BLOCK_NAMES.put("potion_block", "藥水石");
        BLOCK_NAMES.put("block_edit_block", "天地土木石");
        BLOCK_NAMES.put("enchant_block", "附靈石");
        BLOCK_NAMES.put("storage_rune_block", "符咒收納櫃");
        BLOCK_NAMES.put("message_block", "消息石");

        ITEM_TOOLTIPS.put("example_block", "範例功能入口");
        ITEM_TOOLTIPS.put("projectile_spawner_block", "發射投射實體");
        ITEM_TOOLTIPS.put("elemental_block", "調用元素");
        ITEM_TOOLTIPS.put("world_interaction_block", "與世界進行交互");
        ITEM_TOOLTIPS.put("entity_manipulation_block", "移動附近實體");
        ITEM_TOOLTIPS.put("note_block_function_block", "操控音符聲響");
        ITEM_TOOLTIPS.put("read_write_block", "讀寫手持物品");
        ITEM_TOOLTIPS.put("naming_block", "命名目標物件");
        ITEM_TOOLTIPS.put("particle_block", "生成粒子效果");
        ITEM_TOOLTIPS.put("dye_block", "對目標進行染色");
        ITEM_TOOLTIPS.put("marker_block", "標記位置資料");
        ITEM_TOOLTIPS.put("entity_status_block", "修改實體狀態");
        ITEM_TOOLTIPS.put("entity_spawn_block", "召喚指定實體");
        ITEM_TOOLTIPS.put("potion_block", "施加藥水效果");
        ITEM_TOOLTIPS.put("block_edit_block", "修改方塊狀態");
        ITEM_TOOLTIPS.put("enchant_block", "對物體進行附魔");
        ITEM_TOOLTIPS.put("storage_rune_block", "用來存儲包含物品、方塊等數據的符咒的櫃子");
        ITEM_TOOLTIPS.put("data_disk", "用持久化儲存資料，包括方塊，物體的數據");
        BLOCK_NAMES.put("message_block", "给玩家发送消息");
    }

    public AddonLanguageProvider(PackOutput output, String locale) {
        super(output, WenyanAddon.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.wenyan_addon", "吾有一術：新秩序");
        add("wenyan_addon.configuration.title", "吾有一術：新秩序之設");
        add("wenyan_addon.configuration.section.wenyan_addon.common.toml", "新秩序之設");
        add("wenyan_addon.configuration.section.wenyan_addon.common.toml.title", "新秩序之設");

        for (Map.Entry<String, String> entry : BLOCK_NAMES.entrySet()) {
            add("block." + WenyanAddon.MODID + "." + entry.getKey(), entry.getValue());
            add("item." + WenyanAddon.MODID + "." + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : ITEM_NAMES.entrySet()) {
            add("item." + WenyanAddon.MODID + "." + entry.getKey(), entry.getValue());
        }
        add("item." + WenyanAddon.MODID + ".data_disk", "數據磁盤");
        for (Map.Entry<String, String> entry : ITEM_TOOLTIPS.entrySet()) {
            add("item." + WenyanAddon.MODID + "." + entry.getKey() + ".tooltip", entry.getValue());
        }
    }
}
