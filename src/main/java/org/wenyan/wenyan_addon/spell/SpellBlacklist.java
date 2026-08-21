package org.wenyan.wenyan_addon.spell;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 符咒黑名单：data/wenyan_addon/spell/blacklist.json 中的物品 ID 列表。
 * 被列入黑名单的符咒/符咒石在运行时扫描中会被排除，不提供环境函数。
 * 随服务端资源重载（/reload）刷新。
 */
public class SpellBlacklist extends SimpleJsonResourceReloadListener<List<String>> {
    private static final Logger log = LoggerFactory.getLogger(SpellBlacklist.class);
    private static final Codec<List<String>> CODEC = Codec.STRING.listOf();

    private static volatile Set<Item> banned = Set.of();

    public SpellBlacklist() {
        super(CODEC, FileToIdConverter.json("spell"));
    }

    @Override
    protected void apply(Map<Identifier, List<String> > map, ResourceManager manager, ProfilerFiller profiler) {
        Set<Item> items = new HashSet<>();
        for (List<String> ids : map.values()) {
            for (String id : ids) {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    items.add(item);
                }
            }
        }
        banned = Collections.unmodifiableSet(items);
        log.info("Loaded {} blacklisted spell items", banned.size());
    }

    /**
     * 判断物品是否被列入黑名单。
     */
    public static boolean isBanned(Item item) {
        return banned.contains(item);
    }
}