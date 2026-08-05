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
        BLOCK_NAMES.put("fluid_block", "流体石");
        BLOCK_NAMES.put("world_interaction_block", "交感石");
        BLOCK_NAMES.put("entity_manipulation_block", "移形石");
        BLOCK_NAMES.put("music_block", "音符石");
        BLOCK_NAMES.put("read_write_block", "文本读写石");
        BLOCK_NAMES.put("naming_block", "命名石");
        BLOCK_NAMES.put("particle_block", "粒子石");
        BLOCK_NAMES.put("dye_block", "染色石");
        BLOCK_NAMES.put("marker_block", "标记石");
//        BLOCK_NAMES.put("entity_status_block", "實體狀態石");
//        BLOCK_NAMES.put("entity_spawn_block", "實體召喚石");
        BLOCK_NAMES.put("potion_block", "药水石");
        BLOCK_NAMES.put("block_edit_block", "方块操作石");
        BLOCK_NAMES.put("enchant_block", "附魔石");
        BLOCK_NAMES.put("storage_rune_block", "符咒收纳柜");
        BLOCK_NAMES.put("message_block", "消息石");

        ITEM_TOOLTIPS.put("example_block", "範例功能入口");
        ITEM_TOOLTIPS.put("projectile_spawner_block", "发射一些投射物");
        ITEM_TOOLTIPS.put("fluid_block", "操控流体");
        ITEM_TOOLTIPS.put("world_interaction_block", "与世界进行交互");
        ITEM_TOOLTIPS.put("entity_manipulation_block", "移動附近實體");
        ITEM_TOOLTIPS.put("note_block_function_block", "操控音符声音");
        ITEM_TOOLTIPS.put("read_write_block", "读写文本内容，告示牌，讲台");
        ITEM_TOOLTIPS.put("naming_block", "命名目标物件");
        ITEM_TOOLTIPS.put("particle_block", "生成粒子效果");
        ITEM_TOOLTIPS.put("dye_block", "对目标进行染色");
        ITEM_TOOLTIPS.put("marker_block", "标记位置资料");
//        ITEM_TOOLTIPS.put("entity_status_block", "修改實體狀態");
//        ITEM_TOOLTIPS.put("entity_spawn_block", "召喚指定實體");
        ITEM_TOOLTIPS.put("potion_block", "施加药水效果");
        ITEM_TOOLTIPS.put("block_edit_block", "对方块的一些操作，例如破坏，放置");
        ITEM_TOOLTIPS.put("enchant_block", "附魔还有祛魔");
        ITEM_TOOLTIPS.put("storage_rune_block", "用来存储数据磁盘的柜子，还有着发现更隐蔽信息的功能");
        ITEM_TOOLTIPS.put("data_disk", "持久化储存数据");
        ITEM_TOOLTIPS.put("message_block", "给玩家发送消息");
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

        add("wenyan_addon.error.no_loading", "未加载");
        add("wenyan_addon.error.no_read", "无法读取");
        add("wenyan_addon.error.no_write", "无法写入");
        add("wenyan_addon.error.address_to_loog", "距离太远");

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
