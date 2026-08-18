package org.wenyan.wenyan_addon.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageType;
import org.wenyan.wenyan_addon.qi.damage.QiDamageTypes;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;

import java.util.concurrent.CompletableFuture;

/**
 * 元素伤害类型 JSON 构建器：为所有已注册属性生成
 * data/wenyan_addon/damage_type/qi_<属性id>.json（datagen 时自动输出）。
 */
public final class QiDamageTypeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public QiDamageTypeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "damage_type");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
                ElementRegistry.all().stream()
                        .map(attribute -> writeDamageType(cache, attribute))
                        .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> writeDamageType(CachedOutput cache, ElementAttribute attribute) {
        Identifier id = QiDamageTypes.keyOf(attribute).identifier();
        DamageType damageType = QiDamageTypes.damageType(attribute);
        JsonElement json = DamageType.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, damageType)
                .result().orElseThrow(() -> new IllegalStateException("无法序列化伤害类型: " + id));
        return DataProvider.saveStable(cache, json, pathProvider.json(id));
    }

    @Override
    public String getName() {
        return "Qi Damage Types";
    }
}
