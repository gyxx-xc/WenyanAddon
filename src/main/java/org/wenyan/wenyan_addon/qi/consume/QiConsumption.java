package org.wenyan.wenyan_addon.qi.consume;

import net.minecraft.world.entity.player.Player;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

import java.util.HashMap;
import java.util.Map;

public final class QiConsumption {
    private QiConsumption() {
    }

    /**
     * 按设备声明的灵气构成执行消耗（原子操作：全部足量才扣除，否则不扣）。
     */
    public static ConsumptionResult tryConsume(Player player, QiConsumable consumable) {
        PlayerQiData qi = PlayerQi.of(player);
        for (QiCost cost : consumable.qiCosts()) {
            if (!qi.has(cost.element(), cost.amount())) {
                return ConsumptionResult.insufficient();
            }
        }
        Map<ElementType, Double> deducted = new HashMap<>();
        for (QiCost cost : consumable.qiCosts()) {
            qi.consume(cost.element(), cost.amount());
            deducted.put(cost.element(), cost.amount());
        }
        PlayerQi.markDirty(player);
        return ConsumptionResult.success(deducted);
    }
}
