package org.wenyan.pong.setup.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.wenyan.pong.Pong;

import java.util.Map;

public final class PongLanguageProvider extends LanguageProvider {
    private static final Map<String, String> EN_US = Map.ofEntries(
            Map.entry("item.pong.champagne_bottle", "香槟瓶"),
            Map.entry("item.pong.champagne_bottle.tooltip", "手持摇晃蓄压，也可用香槟刀开瓶。"),
            Map.entry("item.pong.champagne_sabre", "香槟刀"),
            Map.entry("item.pong.champagne_sabre.tooltip", "与未开封香槟瓶一同手持即可开瓶。"),
            Map.entry("item.pong.goblet", "香槟杯"),
            Map.entry("item.pong.goblet.tooltip", "从已开封的香槟瓶中倒酒后饮用。"),
            Map.entry("item.pong.plug", "香槟塞"),
            Map.entry("item.pong.plug.tooltip", "香槟开瓶时飞出的软木塞。"),
            Map.entry("item.pong.debug_rod", "调试棒"),
            Map.entry("item.pong.debug_rod.tooltip", "用于测试香槟架的开发工具。"),
            Map.entry("block.pong.champagne_bottle", "香槟瓶"),
            Map.entry("block.pong.champagne_rack", "香槟架"),
            Map.entry("item.pong.champagne_rack.tooltip", "可存放并展示至多四瓶未开封香槟。"),
            Map.entry("block.pong.champagne_fluid_block", "香槟"),
            Map.entry("fluid_type.pong.champagne_fluid", "香槟"),
            Map.entry("entity.pong.plug", "香槟塞"),
            Map.entry("effect.pong.drunk", "酒醉身姿似百合")
    );

    private static final Map<String, String> ZH_CN = Map.ofEntries(
            Map.entry("item.pong.champagne_bottle", "香槟瓶"),
            Map.entry("item.pong.champagne_bottle.tooltip", "手持摇晃蓄压，也可用香槟刀开瓶。"),
            Map.entry("item.pong.champagne_sabre", "香槟刀"),
            Map.entry("item.pong.champagne_sabre.tooltip", "与未开封香槟瓶一同手持即可开瓶。"),
            Map.entry("item.pong.goblet", "香槟杯"),
            Map.entry("item.pong.goblet.tooltip", "从已开封的香槟瓶中倒酒后饮用。"),
            Map.entry("item.pong.plug", "香槟塞"),
            Map.entry("item.pong.plug.tooltip", "香槟开瓶时飞出的软木塞。"),
            Map.entry("item.pong.debug_rod", "调试棒"),
            Map.entry("item.pong.debug_rod.tooltip", "用于测试香槟架的开发工具。"),
            Map.entry("block.pong.champagne_bottle", "香槟瓶"),
            Map.entry("block.pong.champagne_rack", "香槟架"),
            Map.entry("item.pong.champagne_rack.tooltip", "可存放并展示至多四瓶未开封香槟。"),
            Map.entry("block.pong.champagne_fluid_block", "香槟"),
            Map.entry("fluid_type.pong.champagne_fluid", "香槟"),
            Map.entry("entity.pong.plug", "香槟塞"),
            Map.entry("effect.pong.drunk", "酒醉身姿似百合")
    );

    private final Map<String, String> translations;

    public PongLanguageProvider(PackOutput output, String locale) {
        super(output, Pong.MODID, locale);
        this.translations = "zh_cn".equals(locale) ? ZH_CN : EN_US;
    }

    @Override
    protected void addTranslations() {
        this.translations.forEach(this::add);
    }
}
