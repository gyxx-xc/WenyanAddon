package org.wenyan.wenyan_addon.qi.async;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.HashSet;
import java.util.Set;

/**
 * 灵气恢复异步计算器：纯数学运算，不接触任何 Minecraft 对象。
 * 输入：玩家快照 + 区块快照 + 恢复源集合；输出：{@link RestoreResult}。
 */
public final class QiRestoreCalculator {
    /**
     * 匮乏漏气速率（每秒 0.5%）。
     */
    public static final double LEAK_RATE_PER_SECOND = 0.005;

    private QiRestoreCalculator() {
    }

    /**
     * 计算玩家灵气恢复（含无属性并行恢复与匮乏漏气）。
     *
     * @param perTick    每 tick 量（1/20）
     * @param sources    恢复源（无属性 + 已解锁属性 + 装备）
     */
    public static RestoreResult calculate(QiSnapshot player, ChunkQiSnapshot chunk,
                                          Set<ElementAttribute> sources, double perTick) {
        RestoreResult.Builder builder = new RestoreResult.Builder(player.version());
        if (chunk.depleted()) {
            // 匮乏：漏气
            builder.setLeakScale(1.0 - LEAK_RATE_PER_SECOND / 20.0);
            return builder.build();
        }
        for (ElementAttribute source : sources) {
            ElementCoefficients c = player.coefficients(source);
            double veinBoost = 1.0 + c.veinStageGain() * chunk.veinStage();
            double n = chunk.ratio(source.id());
            double m = chunk.remainingRatio();
            double gain = c.environmentGainBase() * c.environmentRatioWeight() * n * m;
            double base = c.restoreAmount() * c.restoreRate();
            double env = base * gain;
            double total = (base + env) * veinBoost;
            ElementType generated = ElementRelations.generates(source);
            if (generated != null && player.cap(generated) > 0) {
                double sub = total * c.environmentSubRatio();
                builder.addGain(source, (total - sub) * perTick);
                builder.addGain(generated, sub * perTick);
            } else {
                builder.addGain(source, total * perTick);
            }
        }
        return builder.build();
    }

    /**
     * 收集恢复源（纯计算：已解锁属性；装备源由调用方传入）。
     */
    public static Set<ElementAttribute> collectUnlocked(QiSnapshot player) {
        Set<ElementAttribute> sources = new HashSet<>();
        for (ElementAttribute element : ElementRegistry.all()) {
            if (element == ElementType.YIN || element == ElementType.YANG) {
                continue;
            }
            if (player.cap(element) > 0) {
                sources.add(element);
            }
        }
        return sources;
    }
}
