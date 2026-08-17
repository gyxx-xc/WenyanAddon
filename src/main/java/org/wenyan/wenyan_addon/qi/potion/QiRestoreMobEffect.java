package org.wenyan.wenyan_addon.qi.potion;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;

/**
 * 灵气缓释恢复效果（每属性独立注册）：每 tick 缓慢恢复标记属性的灵气。
 */
public class QiRestoreMobEffect extends MobEffect {
    private final ElementAttribute attribute;

    public QiRestoreMobEffect(ElementAttribute attribute) {
        super(MobEffectCategory.BENEFICIAL, attribute.color());
        this.attribute = attribute;
    }

    public ElementAttribute attribute() {
        return attribute;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tickCount > 0 && tickCount % 20 == 0; // 每秒结算一次
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplification) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            var qi = PlayerQi.of(player);
            qi.add(attribute, qi.cap(attribute) * 0.05); // 每秒恢复上限 5%
            PlayerQi.markDirty(player);
        }
        return true;
    }
}
