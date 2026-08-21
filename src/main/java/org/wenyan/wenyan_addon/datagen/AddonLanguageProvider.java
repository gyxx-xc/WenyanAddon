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
        ITEM_NAMES.put("qi_vessel", "灵珠");
        ITEM_NAMES.put("yin_crystal", "阴灵气结晶");
        ITEM_NAMES.put("yang_crystal", "阳灵气结晶");
        ITEM_NAMES.put("spirit_stone", "灵石");
        ITEM_NAMES.put("spirit_stone_impure", "杂质灵石");
        ITEM_NAMES.put("spirit_stone_refined", "精纯灵石");
        ITEM_NAMES.put("qi_liquid_bottle", "灵液水瓶");
        ITEM_NAMES.put("qi_restore_potion_small", "小型灵气恢复药水");
        ITEM_NAMES.put("qi_restore_potion_medium", "中型灵气恢复药水");
        ITEM_NAMES.put("qi_restore_potion_large", "大型灵气恢复药水");
        ITEM_NAMES.put("qi_restore_potion_sustained", "缓释灵气恢复药水");
        ITEM_NAMES.put("spell_sword", "法术剑");
        ITEM_NAMES.put("fulu_pouch", "符咒包");
        ITEM_NAMES.put("fulu_pouch_extension", "符咒拓展包");

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
        BLOCK_NAMES.put("time_block", "时间石");
        BLOCK_NAMES.put("qi_block", "灵气石");
        BLOCK_NAMES.put("qi_storage_block", "灵气池");
        BLOCK_NAMES.put("qi_ritual_block", "淬体仪座");
        BLOCK_NAMES.put("qi_liquid_collector_block", "灵液收集器");
        BLOCK_NAMES.put("qi_liquid_purifier_block", "灵液纯化器");
        BLOCK_NAMES.put("qi_gathering_array_block", "聚灵阵");

        ITEM_TOOLTIPS.put("example_block", "範例功能入口");
        ITEM_TOOLTIPS.put("projectile_spawner_block", "发射一些投射物，例如箭，雪球，火球，烟花");
        ITEM_TOOLTIPS.put("fluid_block", "操控流体");
        ITEM_TOOLTIPS.put("world_interaction_block", "与世界进行交互，例如挖掘，破坏，交换方块位置");
        ITEM_TOOLTIPS.put("entity_manipulation_block", "操控实体");
        ITEM_TOOLTIPS.put("music_block", "操控声音，可以尝试演奏一曲");
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
        ITEM_TOOLTIPS.put("time_block", "获取时间，以及修改时间表达的方式");
        ITEM_TOOLTIPS.put("qi_block", "五行灵气的查询与调试");
        ITEM_TOOLTIPS.put("qi_storage_block", "长时间驻扎时使用的灵气容器");
        ITEM_TOOLTIPS.put("qi_vessel", "随身携带的灵气容器（灵珠）");
        ITEM_TOOLTIPS.put("yin_crystal", "阴之地强化怪物的掉落物，用于合成获取阴灵气");
        ITEM_TOOLTIPS.put("yang_crystal", "阳之地强化生物的掉落物，用于合成获取阳灵气");
        ITEM_TOOLTIPS.put("spirit_stone", "强化生物掉落的一次性灵气容器，灵气用光后消失");
        ITEM_TOOLTIPS.put("qi_liquid_bottle", "收集器产出的灵液，经纯化器可变为灵气恢复药水");
        ITEM_TOOLTIPS.put("qi_liquid_collector_block", "从区块提取灵气制灵液，空瓶右键接取");
        ITEM_TOOLTIPS.put("qi_liquid_purifier_block", "放入灵液水瓶，纯化为对应属性的灵气恢复药水");
        ITEM_TOOLTIPS.put("qi_gathering_array_block", "抽取区块主属性灵气，填充附近玩家灵气条（5% 上限）");
        ITEM_TOOLTIPS.put("qi_ritual_block", "淬体仪式核心：献祭物品与经验，淬炼自身");
    }

    public AddonLanguageProvider(PackOutput output, String locale) {
        super(output, WenyanAddon.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.wenyan_addon", "吾有一術：新秩序");
        add("container." + WenyanAddon.MODID + ".storage_rune", "符咒收纳柜");
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
        // 灵气属性标记效果：占位符模板（%s = 属性名，运行时自动填充）
        add("effect." + WenyanAddon.MODID + ".qi_element_mark", "灵气侵蚀(%s)");
        // 属性伤害死亡消息（message_id = qi_<属性id>，key = death.attack.qi_<id>）
        add("death.attack.qi_metal", "%1$s 被金气穿身，金锐之气贯体而亡");
        add("death.attack.qi_wood", "%1$s 被木气缠绕，生机尽失而亡");
        add("death.attack.qi_water", "%1$s 被水气吞没，溺于汪洋之中");
        add("death.attack.qi_fire", "%1$s 被火气焚身，化为灰烬");
        add("death.attack.qi_earth", "%1$s 被土气压顶，碾入大地之中");
        add("death.attack.qi_yin", "%1$s 被阴气蚀骨，魂归九幽");
        add("death.attack.qi_yang", "%1$s 被阳气灼体，形销神灭");
        add("death.attack.qi_neutral", "%1$s 被无主灵气吞没");
        add("death.attack.qi_ice", "%1$s 被冰气冻结，碎为冰晶");
        add("death.attack.qi_lightning", "%1$s 被雷气轰击，化作焦炭");
    }
}
