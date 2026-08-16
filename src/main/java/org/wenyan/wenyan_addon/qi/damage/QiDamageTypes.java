package org.wenyan.wenyan_addon.qi.damage;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DeathMessageType;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 五行伤害类型：自动扫描所有已注册属性，为其注册对应 {@link DamageType}。
 * 伤害来源 id：wenyan_addon:qi_<属性id>。
 */
public final class QiDamageTypes {
    private static final Map<String, ResourceKey<DamageType>> KEYS = new HashMap<>();

    private QiDamageTypes() {
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.DAMAGE_TYPE, helper -> {
            for (ElementAttribute attribute : ElementRegistry.all()) {
                Identifier id = Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "qi_" + attribute.id());
                ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, id);
                helper.register(key, new DamageType(
                        id.toLanguageKey(), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                        0.1f, DamageEffects.HURT, DeathMessageType.DEFAULT));
                KEYS.put(attribute.id(), key);
            }
        });
    }

    /**
     * 该属性的伤害类型 key（未注册返回 null）。
     */
    public static ResourceKey<DamageType> keyOf(ElementAttribute attribute) {
        return KEYS.get(attribute.id());
    }

    public static DamageType damageType(ElementAttribute attribute) {
        return new DamageType("qi_" + attribute.id(), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0.1f, DamageEffects.HURT, DeathMessageType.DEFAULT);
    }
}
