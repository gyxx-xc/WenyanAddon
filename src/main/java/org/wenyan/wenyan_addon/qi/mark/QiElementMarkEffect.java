package org.wenyan.wenyan_addon.qi.mark;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * 灵气属性标记效果（每属性独立注册）：标记生物携带的灵气属性。
 * 显示名（语言 key：effect.wenyan_addon.qi_element_mark_&lt;id&gt;）与粒子颜色随属性变化。
 */
public class QiElementMarkEffect extends MobEffect {
    private final ElementAttribute attribute;

    public QiElementMarkEffect(ElementAttribute attribute) {
        super(MobEffectCategory.BENEFICIAL, attribute.color());
        this.attribute = attribute;
    }

    public ElementAttribute attribute() {
        return attribute;
    }

    @Override
    public String getDescriptionId() {
        return "effect." + WenyanAddon.MODID + ".qi_element_mark";
    }

    /**
     * 自动匹配显示名：占位符模板（语言文件：灵气侵蚀(%s)），缺省 fallback 直接中文填充。
     */
    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatableWithFallback(
                getDescriptionId(),
                "灵气侵蚀(" + attribute.displayName() + ")",
                attribute.displayName());
    }

    @Override
    public int getColor() {
        return attribute.color();
    }

    @Override
    public ParticleOptions createParticleOptions(MobEffectInstance mobEffectInstance) {
        return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, attribute.color());
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return false;
    }
}
