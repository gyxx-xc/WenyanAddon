package org.wenyan.wenyan_addon.qi.element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 物品属性配置文件：config/wenyan_addon/item_attributes.json。
 * 格式：物品 ID → 属性 ID 数组（可复数，支持五行/阴阳/无属性与衍生属性）。
 * 文件不存在时创建默认配置；解析失败回退默认配置。
 */
public final class ItemAttributeConfig {
    private static final Logger log = LoggerFactory.getLogger(ItemAttributeConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, List<String>>>() {
    }.getType();

    private static final Path PATH = FMLPaths.CONFIGDIR.get()
            .resolve("wenyan_addon")
            .resolve("item_attributes.json");

    private ItemAttributeConfig() {
    }

    /**
     * 加载配置：文件不存在则创建默认配置并返回；读取/解析失败回退默认。
     */
    public static Map<String, List<String>> load() {
        if (!Files.exists(PATH)) {
            writeDefaults();
            return defaults();
        }
        try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            Map<String, List<String>> map = GSON.fromJson(reader, MAP_TYPE);
            if (map == null || map.isEmpty()) {
                log.warn("物品属性配置为空，使用默认配置: {}", PATH);
                return defaults();
            }
            return map;
        } catch (Exception e) {
            log.error("读取物品属性配置失败，使用默认配置: {}", e.getMessage());
            return defaults();
        }
    }

    private static void writeDefaults() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(defaults(), writer);
            }
            log.info("已创建默认物品属性配置: {}", PATH);
        } catch (IOException e) {
            log.error("写入物品属性配置失败: {}", e.getMessage());
        }
    }

    /**
     * 默认配置（示例）：五行/阴阳/无属性 + 衍生属性（冰、雷）均可作为标记。
     */
    private static Map<String, List<String>> defaults() {
        Map<String, List<String>> defaults = new LinkedHashMap<>();
        defaults.put("minecraft:iron_ingot", List.of("metal"));
        defaults.put("minecraft:iron_block", List.of("metal"));
        defaults.put("minecraft:gold_ingot", List.of("metal"));
        defaults.put("minecraft:copper_ingot", List.of("metal"));
        defaults.put("minecraft:netherite_ingot", List.of("metal"));
        defaults.put("minecraft:diamond", List.of("metal"));

        defaults.put("minecraft:oak_log", List.of("wood"));
        defaults.put("minecraft:spruce_log", List.of("wood"));
        defaults.put("minecraft:stick", List.of("wood"));
        defaults.put("minecraft:oak_sapling", List.of("wood"));

        defaults.put("minecraft:water_bucket", List.of("water"));
        defaults.put("minecraft:heart_of_the_sea", List.of("water"));
        defaults.put("minecraft:ice", List.of("water", "ice"));
        defaults.put("minecraft:blue_ice", List.of("water", "ice"));

        defaults.put("minecraft:torch", List.of("fire"));
        defaults.put("minecraft:blaze_rod", List.of("fire"));
        defaults.put("minecraft:coal", List.of("fire"));
        defaults.put("minecraft:lava_bucket", List.of("fire"));

        defaults.put("minecraft:cobblestone", List.of("earth"));
        defaults.put("minecraft:stone", List.of("earth"));
        defaults.put("minecraft:dirt", List.of("earth"));
        defaults.put("minecraft:sand", List.of("earth"));

        defaults.put("minecraft:soul_sand", List.of("yin"));
        defaults.put("minecraft:glowstone_dust", List.of("yang"));

        defaults.put("minecraft:lightning_rod", List.of("lightning"));
        return defaults;
    }
}