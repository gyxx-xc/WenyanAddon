package org.wenyan.wenyan_addon.spell;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.wenyan.wenyan_addon.WenyanAddon;

/**
 * 法术数据组件：物品上存储的咒术代码。
 * 通过 ItemStack 组件读写，类似本体 PROGRAM_CODE_DATA（DataComponentType&lt;String&gt;）。
 */
public final class SpellDataComponent {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(
            Registries.DATA_COMPONENT_TYPE, WenyanAddon.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SPELL_CODE = DATA_COMPONENT_TYPES.register(
            "spell_code", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FuluPouchComponent>> POUCH_DATA = DATA_COMPONENT_TYPES.register(
            "pouch_data", () -> DataComponentType.<FuluPouchComponent>builder().persistent(FuluPouchComponent.CODEC).networkSynchronized(FuluPouchComponent.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SPELL_STEP = DATA_COMPONENT_TYPES.register(
            "spell_step", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    private SpellDataComponent() {
    }

    public static void register(IEventBus modBus) {
        DATA_COMPONENT_TYPES.register(modBus);
    }
}