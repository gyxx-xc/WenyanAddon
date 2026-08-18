package org.wenyan.wenyan_addon.qi.damage;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;

/**
 * 五行伤害类型：每属性对应一个 DamageType（wenyan_addon:qi_<属性id>），
 * 由 datagen 生成 data/wenyan_addon/damage_type/qi_<id>.json。
 */
public final class QiDamageTypes {
    private QiDamageTypes() {
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.DAMAGE_TYPE, helper -> {
            for (ElementAttribute attribute : ElementRegistry.all()) {
                helper.register(keyOf(attribute), damageType(attribute));
            }
        });
    }

    /**
     * 该属性的伤害类型 key（动态生成，不依赖注册表缓存）。
     */
    public static ResourceKey<DamageType> keyOf(ElementAttribute attribute) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "qi_" + attribute.id()));
    }

    /**
     * 该属性的 DamageType 实例（datagen 输出的 JSON 内容）。
     */
    public static DamageType damageType(ElementAttribute attribute) {
        return new DamageType("qi_" + attribute.id(), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0.1f, DamageEffects.HURT, DeathMessageType.DEFAULT);
    }

    /**
     * 该属性的伤害类型是否已加载（datapack JSON 是否生效）。
     */
    public static boolean isRegistered(Registry<DamageType> registry, ElementAttribute attribute) {
        return registry.get(keyOf(attribute).identifier()) != null;
    }

    /**
     * 是否为灵气伤害（wenyan_addon:qi_*）。
     */
    public static boolean isQiDamage(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(key -> WenyanAddon.MODID.equals(key.identifier().getNamespace())
                        && key.identifier().getPath().startsWith("qi_"))
                .orElse(false);
    }
}
