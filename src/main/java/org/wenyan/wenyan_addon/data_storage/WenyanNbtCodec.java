package org.wenyan.wenyan_addon.data_storage;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import indi.wenyan.content.block.runner.RunnerBlockEntity;
import indi.wenyan.interpreter_impl.value.WenyanBlock;
import indi.wenyan.interpreter_impl.value.WenyanBlockRunnerValue;
import indi.wenyan.interpreter_impl.value.WenyanCapabilitySlot;
import indi.wenyan.interpreter_impl.value.WenyanEntity;
import indi.wenyan.interpreter_impl.value.WenyanPlayer;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanLeftValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanInteger;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.value.WenyanElement;
import org.wenyan.wenyan_addon.value.WenyanMapValue;
import org.wenyan.wenyan_addon.value.WenyanPotionType;

import java.util.Map;
import java.util.UUID;

public final class WenyanNbtCodec {
    private static final String TYPE_KEY = "t";
    private static final String VALUE_KEY = "v";
    private static final String CLASS_KEY = "c";

    private WenyanNbtCodec() {
    }

    public static CompoundTag toNbt(IWenyanValue value) {
        try {
            return encode(value);
        } catch (WenyanException e) {
            return opaque(value, e.getMessage());
        }
    }

    public static IWenyanValue fromNbt(CompoundTag tag, ServerLevel level) {
        if (tag == null) {
            return WenyanNull.NULL;
        }
        String type = tag.getStringOr(TYPE_KEY, "null");
        return switch (type) {
            case "bool" -> tag.getBooleanOr(VALUE_KEY, false) ? WenyanBoolean.TRUE : WenyanBoolean.FALSE;
            case "int" -> WenyanInteger.valueOf(tag.getLongOr(VALUE_KEY, 0));
            case "num" -> new WenyanDouble(tag.getDoubleOr(VALUE_KEY, 0));
            case "str" -> new WenyanString(tag.getStringOr(VALUE_KEY, ""));
            case "list" -> {
                WenyanList list = new WenyanList();
                for (Tag item : tag.getListOrEmpty(VALUE_KEY)) {
                    if (item instanceof CompoundTag compound) {
                        list.add(fromNbt(compound, level));
                    }
                }
                yield list;
            }
            case "map" -> {
                WenyanMapValue map = new WenyanMapValue();
                CompoundTag entries = tag.getCompoundOrEmpty(VALUE_KEY);
                for (String key : entries.keySet()) {
                    map.put(key, fromNbt(entries.getCompoundOrEmpty(key), level));
                }
                yield map;
            }
            case "vec3" -> {
                CompoundTag vec = tag.getCompoundOrEmpty(VALUE_KEY);
                yield new WenyanVec3(new Vec3(
                        vec.getDoubleOr("x", 0),
                        vec.getDoubleOr("y", 0),
                        vec.getDoubleOr("z", 0)));
            }
            case "potion" -> {
                CompoundTag potionTag = tag.getCompoundOrEmpty(VALUE_KEY);
                String raw = potionTag.getStringOr("e", "");
                if (raw.isEmpty()) {
                    yield WenyanNull.NULL;
                }
                try {
                    var holder = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(raw));
                    yield holder.<IWenyanValue>map(effect -> new WenyanPotionType(new MobEffectInstance(effect, 0, 0)))
                            .orElse(WenyanNull.NULL);
                } catch (IllegalArgumentException e) {
                    yield WenyanNull.NULL;
                }
            }
            case "element" -> {
                String raw = tag.getStringOr(VALUE_KEY, "");
                try {
                    yield new WenyanElement(ElementType.valueOf(raw));
                } catch (IllegalArgumentException e) {
                    yield WenyanNull.NULL;
                }
            }
            case "block" -> {
                try {
                    BlockState state = BlockStateParser
                            .parseForBlock(level.registryAccess().lookupOrThrow(Registries.BLOCK), tag.getStringOr(VALUE_KEY, ""), false)
                            .blockState();
                    yield new WenyanBlock(state);
                } catch (CommandSyntaxException e) {
                    yield WenyanNull.NULL;
                }
            }
            case "entity" -> {
                UUID uuid = parseUuid(tag.getStringOr(VALUE_KEY, ""));
                Entity entity = uuid != null ? level.getEntity(uuid) : null;
                yield entity != null ? new WenyanEntity(entity) : WenyanNull.NULL;
            }
            case "player" -> {
                UUID uuid = parseUuid(tag.getStringOr(VALUE_KEY, ""));
                Player player = uuid != null ? level.getServer().getPlayerList().getPlayer(uuid) : null;
                yield player != null ? new WenyanPlayer(player) : WenyanNull.NULL;
            }
            case "itemslot" -> {
                CompoundTag slotTag = tag.getCompoundOrEmpty(VALUE_KEY);
                Vec3 pose = new Vec3(
                        slotTag.getDoubleOr("x", 0),
                        slotTag.getDoubleOr("y", 0),
                        slotTag.getDoubleOr("z", 0));
                ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, BlockPos.containing(pose), null);
                yield handler != null ? new WenyanCapabilitySlot(pose, handler, slotTag.getIntOr("s", 0)) : WenyanNull.NULL;
            }
            case "runner" -> {
                CompoundTag posTag = tag.getCompoundOrEmpty(VALUE_KEY);
                BlockPos pos = new BlockPos(
                        posTag.getIntOr("x", 0),
                        posTag.getIntOr("y", 0),
                        posTag.getIntOr("z", 0));
                yield level.getBlockEntity(pos) instanceof RunnerBlockEntity runner
                        ? new WenyanBlockRunnerValue(runner)
                        : WenyanNull.NULL;
            }
            case "opaque" -> new WenyanString(tag.getStringOr(VALUE_KEY, ""));
            default -> WenyanNull.NULL;
        };
    }

    private static CompoundTag encode(IWenyanValue value) throws WenyanException {
        if (value instanceof WenyanLeftValue leftValue) {
            return encode(leftValue.getValue());
        }
        CompoundTag tag = new CompoundTag();
        if (value == null || value == WenyanNull.NULL) {
            tag.putString(TYPE_KEY, "null");
        } else if (value instanceof WenyanBoolean bool) {
            tag.putString(TYPE_KEY, "bool");
            tag.putBoolean(VALUE_KEY, bool.value());
        } else if (value instanceof WenyanInteger integer) {
            tag.putString(TYPE_KEY, "int");
            tag.putLong(VALUE_KEY, integer.value());
        } else if (value instanceof WenyanDouble number) {
            tag.putString(TYPE_KEY, "num");
            tag.putDouble(VALUE_KEY, number.value());
        } else if (value instanceof WenyanString string) {
            tag.putString(TYPE_KEY, "str");
            tag.putString(VALUE_KEY, string.value());
        } else if (value instanceof WenyanList list) {
            tag.putString(TYPE_KEY, "list");
            ListTag listTag = new ListTag();
            for (IWenyanValue item : list.value()) {
                listTag.add(toNbt(item));
            }
            tag.put(VALUE_KEY, listTag);
        } else if (value instanceof WenyanMapValue map) {
            tag.putString(TYPE_KEY, "map");
            CompoundTag entries = new CompoundTag();
            for (Map.Entry<String, IWenyanValue> entry : map.values().entrySet()) {
                entries.put(entry.getKey(), toNbt(entry.getValue()));
            }
            tag.put(VALUE_KEY, entries);
        } else if (value instanceof WenyanVec3 vec3) {
            tag.putString(TYPE_KEY, "vec3");
            CompoundTag vec = new CompoundTag();
            vec.putDouble("x", vec3.value().x);
            vec.putDouble("y", vec3.value().y);
            vec.putDouble("z", vec3.value().z);
            tag.put(VALUE_KEY, vec);
        } else if (value instanceof WenyanPotionType potion) {
            tag.putString(TYPE_KEY, "potion");
            CompoundTag potionTag = new CompoundTag();
            potionTag.putString("e", potion.value().getEffect().unwrapKey().map(key -> key.identifier().toString()).orElse(""));
            potionTag.putString("n", potion.value().getEffect().value().getDisplayName().getString());
            tag.put(VALUE_KEY, potionTag);
        } else if (value instanceof WenyanElement element) {
            tag.putString(TYPE_KEY, "element");
            tag.putString(VALUE_KEY, element.value().name());
        } else if (value instanceof WenyanBlock block) {
            tag.putString(TYPE_KEY, "block");
            tag.putString(VALUE_KEY, BlockStateParser.serialize(block.value()));
        } else if (value instanceof WenyanEntity entity) {
            tag.putString(TYPE_KEY, "entity");
            tag.putString(VALUE_KEY, entity.value().getUUID().toString());
        } else if (value instanceof WenyanPlayer player) {
            tag.putString(TYPE_KEY, "player");
            tag.putString(VALUE_KEY, player.value().getUUID().toString());
        } else if (value instanceof WenyanCapabilitySlot slot) {
            tag.putString(TYPE_KEY, "itemslot");
            CompoundTag slotTag = new CompoundTag();
            slotTag.putDouble("x", slot.pose().x);
            slotTag.putDouble("y", slot.pose().y);
            slotTag.putDouble("z", slot.pose().z);
            slotTag.putInt("s", slot.slot());
            tag.put(VALUE_KEY, slotTag);
        } else if (value instanceof WenyanBlockRunnerValue runner) {
            tag.putString(TYPE_KEY, "runner");
            BlockPos pos = runner.entity().getBlockPos();
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            tag.put(VALUE_KEY, posTag);
        } else {
            return opaque(value, null);
        }
        return tag;
    }

    private static CompoundTag opaque(IWenyanValue value, String reason) {
        CompoundTag tag = new CompoundTag();
        tag.putString(TYPE_KEY, "opaque");
        tag.putString(VALUE_KEY, value == null ? "" : value.toString());
        tag.putString(CLASS_KEY, reason == null ? "" : reason);
        return tag;
    }

    private static @Nullable UUID parseUuid(String raw) {
        if (raw.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
