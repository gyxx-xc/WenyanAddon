package org.wenyan.wenyan_addon.qi.async;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步计算结果：各属性增量与漏气比例，携带源快照版本号。
 */
public final class RestoreResult {
    private final Map<ElementAttribute, Double> gains;
    private final double leakScale;
    private final long sourceVersion;

    private RestoreResult(Map<ElementAttribute, Double> gains, double leakScale, long sourceVersion) {
        this.gains = Map.copyOf(gains);
        this.leakScale = leakScale;
        this.sourceVersion = sourceVersion;
    }

    public static RestoreResult of(Map<ElementAttribute, Double> gains, double leakScale, long sourceVersion) {
        return new RestoreResult(gains, leakScale, sourceVersion);
    }

    public static RestoreResult empty(long sourceVersion) {
        return new RestoreResult(Map.of(), 1.0, sourceVersion);
    }

    public Map<ElementAttribute, Double> gains() {
        return gains;
    }

    /**
     * 漏气比例（1.0 = 不漏气）。
     */
    public double leakScale() {
        return leakScale;
    }

    public long sourceVersion() {
        return sourceVersion;
    }

    public boolean isEmpty() {
        return gains.isEmpty();
    }

    public static final class Builder {
        private final Map<ElementAttribute, Double> gains = new HashMap<>();
        private double leakScale = 1.0;
        private final long sourceVersion;

        public Builder(long sourceVersion) {
            this.sourceVersion = sourceVersion;
        }

        public Builder addGain(ElementAttribute element, double amount) {
            gains.merge(element, amount, Double::sum);
            return this;
        }

        public Builder setLeakScale(double leakScale) {
            this.leakScale = leakScale;
            return this;
        }

        public RestoreResult build() {
            return RestoreResult.of(gains, leakScale, sourceVersion);
        }
    }
}
