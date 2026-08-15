package org.wenyan.wenyan_addon.qi.async;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家灵气只读快照：异步线程计算时使用，完全隔离主线程数据。
 * 携带数据版本号，结果应用前比对（防旧数据覆盖新数据）。
 */
public final class QiSnapshot {
    private final Map<String, Double> reserves;
    private final Map<String, Double> caps;
    private final Map<String, ElementCoefficients> coefficients;
    private final long version;
    private final long createdTick;

    private QiSnapshot(Map<String, Double> reserves, Map<String, Double> caps,
                       Map<String, ElementCoefficients> coefficients, long version, long createdTick) {
        this.reserves = Map.copyOf(reserves);
        this.caps = Map.copyOf(caps);
        this.coefficients = Map.copyOf(coefficients);
        this.version = version;
        this.createdTick = createdTick;
    }

    public static QiSnapshot of(PlayerQiData qi, long createdTick) {
        return new QiSnapshot(
                new HashMap<>(qi.reserves()),
                new HashMap<>(qi.capMap()),
                new HashMap<>(qi.coefficientMap()),
                qi.version(), createdTick);
    }

    public double get(ElementAttribute element) {
        return reserves.getOrDefault(element.id(), 0.0);
    }

    public double cap(ElementAttribute element) {
        return caps.getOrDefault(element.id(), 0.0);
    }

    public ElementCoefficients coefficients(ElementAttribute element) {
        ElementCoefficients c = coefficients.get(element.id());
        return c != null ? c : element.defaultCoefficients();
    }

    public boolean depleted() {
        double total = 0;
        for (double v : reserves.values()) {
            total += v;
        }
        return total <= 0;
    }

    public long version() {
        return version;
    }
}
