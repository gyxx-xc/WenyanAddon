package org.wenyan.wenyan_addon.data_storage;

import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanInteger;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.wenyan.wenyan_addon.value.WenyanMapValue;

import java.util.Map;

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

    public static IWenyanValue fromNbt(CompoundTag tag) {
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
                        list.add(fromNbt(compound));
                    }
                }
                yield list;
            }
            case "map" -> {
                WenyanMapValue map = new WenyanMapValue();
                CompoundTag entries = tag.getCompoundOrEmpty(VALUE_KEY);
                for (String key : entries.keySet()) {
                    map.put(key, fromNbt(entries.getCompoundOrEmpty(key)));
                }
                yield map;
            }
            case "opaque" -> new WenyanString(tag.getStringOr(VALUE_KEY, ""));
            default -> WenyanNull.NULL;
        };
    }

    private static CompoundTag encode(IWenyanValue value) throws WenyanException {
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
}
