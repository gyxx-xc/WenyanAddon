package org.wenyan.wenyan_addon.qi.spell;

import net.minecraft.server.level.ServerPlayer;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;
import org.wenyan.wenyan_addon.qi.storage.QiContainer;

import java.util.List;
import java.util.Map;

/**
 * 符咒施法上下文：方法体可读匹配结果，并登记自定义灵力消耗。
 * 登记的消耗在方法体成功返回后由包装层统一扣除（容器优先、玩家兜底）；
 * 方法体未登记时按注解 baseCost × 匹配系数执行默认消耗。
 * 方法体抛异常时，登记不生效（不扣费）。
 */
public interface QiSpellContext {
    ServerPlayer caster();

    PlayerQiData qi();

    /**
     * 标签匹配结果（五行相生相克增益/减益系数）。
     */
    QiMatch match();

    /**
     * 支付链容器（手持/背包/附近方块容器）。
     */
    List<QiContainer> containers();

    /**
     * 容器 + 玩家合计是否足够（方法体可自行预检查）。
     */
    boolean has(ElementAttribute element, double amount);

    /**
     * 登记自定义消耗：方法体完成后统一扣除指定属性灵气（可多次调用累加）。
     * 登记后注解默认消耗不再执行。
     */
    void require(ElementAttribute element, double amount);

    /**
     * 登记自定义消耗：按属性集合一次登记。
     */
    void require(Map<ElementAttribute, Double> need);

    /**
     * 是否已登记自定义消耗（true 时跳过默认消耗）。
     */
    boolean registered();
}
