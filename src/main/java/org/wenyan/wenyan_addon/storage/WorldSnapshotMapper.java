package org.wenyan.wenyan_addon.storage;

import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueOutput;
import org.wenyan.wenyan_addon.value.WenyanMapValue;

import java.util.Map;

public final class WorldSnapshotMapper {
    private WorldSnapshotMapper() {
    }

    public static WenyanMapValue entity(Entity entity) {
        WenyanMapValue map = new WenyanMapValue();
        map.put("類", new WenyanString(entity.getEncodeId() == null ? entity.getType().toString() : entity.getEncodeId()));
        map.put("uuid", new WenyanString(entity.getStringUUID()));
        map.put("名", new WenyanString(entity.getName().getString()));
        map.put("活", WenyanValues.of(entity.isAlive()));
        map.put("火", WenyanValues.of(entity.isOnFire()));
        map.put("地", WenyanValues.of(entity.onGround()));
        map.put("位置", position(entity.blockPosition()));
        map.put("精位", vec(entity.getX(), entity.getY(), entity.getZ()));
        map.put("組件", entityComponents(entity));
        if (entity.level() instanceof ServerLevel serverLevel) {
            try {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, serverLevel.registryAccess());
                entity.saveWithoutId(output);
                map.put("nbt", tag(output.buildResult()));
            } catch (Exception e) {
                map.put("亂", new WenyanString(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
            }
        }
        return map;
    }

    public static WenyanMapValue block(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        WenyanMapValue map = new WenyanMapValue();
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        map.put("方塊", new WenyanString(blockId == null ? state.getBlock().toString() : blockId.toString()));
        map.put("位置", position(pos));
        map.put("屬性", properties(state));
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            WenyanMapValue be = new WenyanMapValue();
            Identifier typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
            be.put("類", new WenyanString(typeId == null ? blockEntity.getType().toString() : typeId.toString()));
            be.put("組件", components(blockEntity.components()));
            try {
                CompoundTag tag = blockEntity.saveWithoutMetadata(level.registryAccess());
                be.put("nbt", tag(tag));
            } catch (Exception e) {
                be.put("亂", new WenyanString(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
            }
            map.put("方塊實體", be);
        }
        return map;
    }

    public static IWenyanValue tag(Tag tag) {
        if (tag == null) {
            return indi.wenyan.judou.api.values.WenyanNull.NULL;
        }
        if (tag instanceof CompoundTag compound) {
            WenyanMapValue map = new WenyanMapValue();
            for (Map.Entry<String, Tag> entry : compound.entrySet()) {
                map.put(entry.getKey(), tag(entry.getValue()));
            }
            return map;
        }
        return tag.asList()
                .<IWenyanValue>map(list -> {
                    WenyanList result = new WenyanList();
                    for (Tag item : list) {
                        result.add(tag(item));
                    }
                    return result;
                })
                .or(() -> tag.asString().map(WenyanString::new))
                .or(() -> tag.asBoolean().map(WenyanValues::of))
                .or(() -> tag.asNumber().map(number -> new indi.wenyan.judou.api.values.primitive.WenyanDouble(number.doubleValue())))
                .orElseGet(() -> new WenyanString(tag.toString()));
    }

    private static WenyanMapValue position(BlockPos pos) {
        WenyanMapValue map = new WenyanMapValue();
        map.put("x", WenyanValues.of(pos.getX()));
        map.put("y", WenyanValues.of(pos.getY()));
        map.put("z", WenyanValues.of(pos.getZ()));
        return map;
    }

    private static WenyanMapValue vec(double x, double y, double z) {
        WenyanMapValue map = new WenyanMapValue();
        map.put("x", WenyanValues.of(x));
        map.put("y", WenyanValues.of(y));
        map.put("z", WenyanValues.of(z));
        return map;
    }

    private static WenyanMapValue properties(BlockState state) {
        WenyanMapValue map = new WenyanMapValue();
        for (Property<?> property : state.getProperties()) {
            map.put(property.getName(), new WenyanString(propertyValueName(state, property)));
        }
        return map;
    }

    private static <T extends Comparable<T>> String propertyValueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static WenyanMapValue entityComponents(Entity entity) {
        WenyanMapValue map = new WenyanMapValue();
        putComponent(map, DataComponents.CUSTOM_NAME, entity.get(DataComponents.CUSTOM_NAME));
        putComponent(map, DataComponents.CUSTOM_DATA, entity.get(DataComponents.CUSTOM_DATA));
        for (TypedDataComponent<?> component : entity.typeHolder().components()) {
            putComponent(map, component.type(), component.value());
        }
        return map;
    }

    private static WenyanMapValue components(Iterable<TypedDataComponent<?>> entries) {
        WenyanMapValue map = new WenyanMapValue();
        for (TypedDataComponent<?> entry : entries) {
            putComponent(map, entry.type(), entry.value());
        }
        return map;
    }

    private static void putComponent(WenyanMapValue map, DataComponentType<?> type, Object value) {
        if (value == null) {
            return;
        }
        Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
        map.put(id == null ? type.toString() : id.toString(), new WenyanString(String.valueOf(value)));
    }
}
