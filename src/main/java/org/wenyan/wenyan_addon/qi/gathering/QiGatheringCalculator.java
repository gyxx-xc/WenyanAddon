package org.wenyan.wenyan_addon.qi.gathering;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.Map;

/**
 * 聚灵阵抽取计算（纯函数，异步线程执行，不修改任何数据）：
 * 抽取量 = 玩家灵气条上限的 5%，从区块主属性灵气中扣除；
 * 恢复效率：对应属性 100%、无属性 80%、其它已解锁属性 50%。
 */
public final class QiGatheringCalculator {
    public static final double DRAIN_RATIO = 0.05;

    private QiGatheringCalculator() {
    }

    /**
     * 主线程读取的只读快照。
     *
     * @param dominant  区块主导属性
     * @param available 区块该属性当前储量
     * @param totalCap  玩家灵气条总上限
     * @param caps      玩家各属性上限（拷贝，防并发修改）
     * @param reserves  玩家各属性当前储量（拷贝，防并发修改）
     */
    public record GatherSnapshot(ElementType dominant, double available, double totalCap,
                                 Map<String, Double> caps, Map<String, Double> reserves) {
    }

    /**
     * 计算后的抽取结果（主线程应用：区块扣除 + 玩家增加）。
     *
     * @param dominant  被抽取的区块属性
     * @param extracted 实际抽取量（&lt;=0 表示跳过）
     * @param target    玩家恢复属性
     * @param amount    玩家实际获得量
     */
    public record GatherResult(ElementType dominant, double extracted,
                               ElementAttribute target, double amount) {
    }

    public static GatherResult calculate(GatherSnapshot snapshot) {
        double drain = snapshot.totalCap() * DRAIN_RATIO;
        if (snapshot.available() <= 0 || drain <= 0) {
            return skip(snapshot);
        }
        double extracted = Math.min(drain, snapshot.available());
        // 恢复目标按优先级：对应属性 100% → 无属性 80% → 其它已解锁属性 50%
        // 目标条已满则降级到下一优先级；全部已解锁属性已满 → 停止抽取
        ElementAttribute target = null;
        double efficiency = 0;
        if (canRestore(snapshot, snapshot.dominant())) {
            target = snapshot.dominant();
            efficiency = 1.0;
        } else if (canRestore(snapshot, ElementType.NEUTRAL)) {
            target = ElementType.NEUTRAL;
            efficiency = 0.8;
        } else {
            for (ElementAttribute attribute : ElementRegistry.all()) {
                if (attribute == ElementType.YIN || attribute == ElementType.YANG
                        || attribute == ElementType.NEUTRAL) {
                    continue;
                }
                if (canRestore(snapshot, attribute)) {
                    target = attribute;
                    efficiency = 0.5;
                    break;
                }
            }
        }
        if (target == null) {
            return skip(snapshot);
        }
        // 按目标剩余空间精确抽取：玩家获得 min(计划量, 剩余空间)，区块只扣对应部分
        double capacity = snapshot.caps().getOrDefault(target.id(), 0.0)
                - snapshot.reserves().getOrDefault(target.id(), 0.0);
        double amount = Math.min(extracted * efficiency, capacity);
        if (amount <= 0) {
            return skip(snapshot);
        }
        return new GatherResult(snapshot.dominant(), amount / efficiency, target, amount);
    }

    /**
     * 该属性是否为可恢复目标（已解锁上限 > 0 且尚未恢复至上限）。
     */
    private static boolean canRestore(GatherSnapshot snapshot, ElementAttribute element) {
        double cap = snapshot.caps().getOrDefault(element.id(), 0.0);
        double reserve = snapshot.reserves().getOrDefault(element.id(), 0.0);
        return cap > 0 && reserve < cap;
    }

    private static GatherResult skip(GatherSnapshot snapshot) {
        return new GatherResult(snapshot.dominant(), 0, ElementType.NEUTRAL, 0);
    }
}
