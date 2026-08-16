package org.wenyan.wenyan_addon.qi.mark;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * 灵气属性标记实例：携带标记属性与等级（amplifier 0-4 = 1-5 级）。
 * 服务端逻辑使用（附加属性伤害/抗性）；客户端同步仅需标准字段。
 */
public class ElementMarkInstance extends MobEffectInstance {
    private final ElementAttribute attribute;
    private final int markLevel;

    public ElementMarkInstance(Holder<MobEffect> effect, int amplifier, ElementAttribute attribute) {
        super(effect, MobEffectInstance.INFINITE_DURATION, amplifier);
        this.attribute = attribute;
        this.markLevel = Math.min(5, amplifier + 1);
    }

    public ElementAttribute attribute() {
        return attribute;
    }

    /**
     * 标记等级（1-5）。
     */
    public int markLevel() {
        return markLevel;
    }

    /**
     * 附加属性伤害比例：等级越高比例越高（如 5%/级）。
     */
    public double bonusRatio() {
        return 0.05 * markLevel;
    }

    /**
     * 属性抗性：等级越高抗性越高（如 4%/级，上限 20%）。
     */
    public double resistanceRatio() {
        return 0.04 * markLevel;
    }
}
