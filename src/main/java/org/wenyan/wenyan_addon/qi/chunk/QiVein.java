package org.wenyan.wenyan_addon.qi.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 灵脉：等级无上限，每 20 级 = 1 阶段（stage = (level-1)/20 + 1）。
 * 每次养护（30 分钟）等级 +1；达到 3 阶段后每个等级都会尝试向外延伸。
 */
public record QiVein(int level, Set<String> covered, long lastNurtureTime) {
    public static final int LEVELS_PER_STAGE = 20;
    public static final int EXPAND_STAGE = 3;

    public static final Codec<QiVein> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("level", 1).forGetter(QiVein::level),
            Codec.STRING.listOf().fieldOf("covered")
                    .xmap(list -> (java.util.Set<String>) new HashSet<>(list), set -> new ArrayList<>(set))
                    .forGetter(QiVein::covered),
            Codec.LONG.fieldOf("lastNurtureTime").forGetter(QiVein::lastNurtureTime)
    ).apply(instance, QiVein::new));

    /**
     * 阶段：等级每 20 级为一个阶段（1-20 级 = 1 阶段，21-40 = 2 阶段...）。
     */
    public int stage() {
        return (level - 1) / LEVELS_PER_STAGE + 1;
    }

    /**
     * 是否达到可向外延伸的阶段（3 阶段后每级尝试延伸）。
     */
    public boolean canExpand() {
        return stage() >= EXPAND_STAGE;
    }

    /**
     * 养护提升一级。
     */
    public QiVein nurtureUp(long gameTime) {
        return new QiVein(level + 1, covered, gameTime);
    }
}
