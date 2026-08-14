package org.wenyan.wenyan_addon.qi.ritual;

import com.mojang.serialization.Codec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 淬体仪式配方加载器：从 data/wenyan_addon/qi_ritual/*.json 读取，
 * 随服务端资源重载（/reload）刷新。
 */
public class QiRitualRecipes extends SimpleJsonResourceReloadListener<QiRitualRecipe> {
    private static final Logger log = LoggerFactory.getLogger(QiRitualRecipes.class);
    private static final Codec<QiRitualRecipe> CODEC = QiRitualRecipe.CODEC;

    private static volatile List<QiRitualRecipe> recipes = List.of();

    public QiRitualRecipes() {
        super(CODEC, FileToIdConverter.json("qi_ritual"));
    }

    @Override
    protected void apply(Map<Identifier, QiRitualRecipe> map, ResourceManager manager, ProfilerFiller profiler) {
        recipes = Collections.unmodifiableList(List.copyOf(map.values()));
        log.info("Loaded {} qi ritual recipes", recipes.size());
    }

    public static List<QiRitualRecipe> recipes() {
        return recipes;
    }
}
