package org.wenyan.wenyan_addon.qi.async;

import org.wenyan.wenyan_addon.qi.chunk.ChunkQiData;

import java.util.Map;

/**
 * 区块灵气只读快照：异步计算时读取环境数据，不接触 Minecraft 主线程对象。
 */
public final class ChunkQiSnapshot {
    private final Map<String, Double> proportions;
    private final double remainingRatio;
    private final boolean depleted;
    private final int veinStage;

    private ChunkQiSnapshot(Map<String, Double> proportions, double remainingRatio,
                            boolean depleted, int veinStage) {
        this.proportions = Map.copyOf(proportions);
        this.remainingRatio = remainingRatio;
        this.depleted = depleted;
        this.veinStage = veinStage;
    }

    public static ChunkQiSnapshot of(ChunkQiData chunk) {
        return new ChunkQiSnapshot(
                new java.util.HashMap<>(chunk.proportions()),
                chunk.remainingRatio(),
                chunk.isDepleted(),
                chunk.veinStage());
    }

    /**
     * 该属性在区块结构中的占比（衍生/无属性为 0）。
     */
    public double ratio(String elementId) {
        return proportions.getOrDefault(elementId, 0.0);
    }

    public double remainingRatio() {
        return remainingRatio;
    }

    public boolean depleted() {
        return depleted;
    }

    public int veinStage() {
        return veinStage;
    }
}
