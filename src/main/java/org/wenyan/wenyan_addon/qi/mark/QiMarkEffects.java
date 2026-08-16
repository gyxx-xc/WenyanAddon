package org.wenyan.wenyan_addon.qi.mark;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;

/**
 * 灵气标记效果注册器：为每个已注册属性（五行 + 加载期注册的衍生属性）注册独立效果。
 * key：wenyan_addon:qi_element_mark_&lt;属性id&gt;；显示名与粒子颜色随属性变化。
 */
public final class QiMarkEffects {
    private QiMarkEffects() {
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.MOB_EFFECT, helper -> {
            for (ElementAttribute attribute : ElementRegistry.all()) {
                Identifier id = Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "qi_element_mark_" + attribute.id());
                helper.register(id, new QiElementMarkEffect(attribute));
            }
        });
    }

    /**
     * 该属性的标记效果 Holder（未注册返回 null）。
     */
    public static Holder<MobEffect> holderOf(ElementAttribute attribute) {
        Identifier id = Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "qi_element_mark_" + attribute.id());
        return BuiltInRegistries.MOB_EFFECT.get(id).get();
    }

    /**
     * 从效果 Holder 反查属性（客户端同步后仅持有 Holder）。
     */
    public static ElementAttribute attributeOf(Holder<MobEffect> effect) {
        Identifier id = effect.unwrapKey()
                .map(ResourceKey::identifier)
                .orElse(null);
        if (id == null) {
            return null;
        }
        String raw = id.getPath();
        if (!raw.startsWith("qi_element_mark_")) {
            return null;
        }
        return ElementRegistry.byId(raw.substring("qi_element_mark_".length()));
    }
}
