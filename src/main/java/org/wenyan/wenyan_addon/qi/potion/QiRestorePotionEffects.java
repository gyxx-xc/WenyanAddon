package org.wenyan.wenyan_addon.qi.potion;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;

/**
 * 灵气缓释恢复效果注册器：为每个已注册属性注册独立缓释效果（qi_restore_&lt;id&gt;）。
 */
public final class QiRestorePotionEffects {
    private QiRestorePotionEffects() {
    }

    public static void register(RegisterEvent event) {
        event.register(net.minecraft.core.registries.Registries.MOB_EFFECT, helper -> {
            for (ElementAttribute attribute : ElementRegistry.all()) {
                Identifier id = Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "qi_restore_" + attribute.id());
                helper.register(id, new QiRestoreMobEffect(attribute));
            }
        });
    }

    /**
     * 该属性的缓释效果 Holder（未注册返回 null）。
     */
    public static Holder<MobEffect> holderOf(ElementAttribute attribute) {
        Identifier id = Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "qi_restore_" + attribute.id());
        return BuiltInRegistries.MOB_EFFECT.get(id).get();
    }

    /**
     * 从效果反查属性。
     */
    public static ElementAttribute attributeOf(MobEffect effect) {
        if (!(effect instanceof QiRestoreMobEffect restoreEffect)) {
            return null;
        }
        return restoreEffect.attribute();
    }
}
