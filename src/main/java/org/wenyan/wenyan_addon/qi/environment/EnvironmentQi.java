package org.wenyan.wenyan_addon.qi.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.EnumMap;

public final class EnvironmentQi {
    public static final int SCAN_RADIUS = 16;
    public static final double DOMINANT_THRESHOLD = 0.05;
    private static final double FIRE_BIOME_BONUS = 0.2;

    private EnvironmentQi() {
    }

    public static EnumMap<ElementType, Double> concentration(Level level, BlockPos center) {
        int total = 0;
        int metal = 0;
        int wood = 0;
        int water = 0;
        int fire = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            total++;
            BlockState state = level.getBlockState(pos);
            if (isMetalOre(state)) {
                metal++;
            } else if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                wood++;
            } else if (level.getFluidState(pos).is(FluidTags.WATER)) {
                water++;
            } else if (state.is(BlockTags.FIRE) || state.is(BlockTags.CAMPFIRES) || state.is(Blocks.LAVA)) {
                fire++;
            }
        }
        double fireConcentration = fire / (double) total + (isHotBiome(level.getBiome(center)) ? FIRE_BIOME_BONUS : 0);
        double earthConcentration = Math.clamp((256 - center.getY()) / 256.0, 0, 1);

        EnumMap<ElementType, Double> result = new EnumMap<>(ElementType.class);
        result.put(ElementType.METAL, metal / (double) total);
        result.put(ElementType.WOOD, wood / (double) total);
        result.put(ElementType.WATER, water / (double) total);
        result.put(ElementType.FIRE, fireConcentration);
        result.put(ElementType.EARTH, earthConcentration);
        return result;
    }

    public static ElementType dominantElement(Level level, BlockPos center) {
        EnumMap<ElementType, Double> concentration = concentration(level, center);
        ElementType dominant = null;
        double max = 0;
        for (ElementType element : ElementRelations.ELEMENTS) {
            double value = concentration.get(element);
            if (value > max) {
                max = value;
                dominant = element;
            }
        }
        return dominant != null && max > DOMINANT_THRESHOLD ? dominant : null;
    }

    private static boolean isMetalOre(BlockState state) {
        return state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.COAL_ORES);
    }

    private static boolean isHotBiome(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_NETHER)
                || biome.is(net.minecraft.world.level.biome.Biomes.DESERT)
                || biome.is(net.minecraft.world.level.biome.Biomes.BADLANDS)
                || biome.is(net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS)
                || biome.is(net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS);
    }
}
