package org.wenyan.wenyan_addon.data_storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanInteger;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import org.wenyan.wenyan_addon.value.WenyanMapValue;

import java.util.Map;

public final class WenyanJsonCodec {
    private WenyanJsonCodec() {
    }

    public static JsonElement toJson(IWenyanValue value) {
        try {
            if (value == null || value == WenyanNull.NULL) {
                return JsonNull.INSTANCE;
            }
            if (value.is(WenyanBoolean.TYPE)) {
                return new JsonPrimitive(value.as(WenyanBoolean.TYPE).value());
            }
            if (value.is(WenyanInteger.TYPE)) {
                return new JsonPrimitive(value.as(WenyanInteger.TYPE).value());
            }
            if (value.is(WenyanDouble.TYPE)) {
                return new JsonPrimitive(value.as(WenyanDouble.TYPE).value());
            }
            if (value.is(WenyanString.TYPE)) {
                return new JsonPrimitive(value.as(WenyanString.TYPE).value());
            }
            if (value.is(WenyanList.TYPE)) {
                JsonArray array = new JsonArray();
                for (IWenyanValue item : value.as(WenyanList.TYPE).value()) {
                    array.add(toJson(item));
                }
                return array;
            }
            if (value.is(WenyanMapValue.TYPE)) {
                return mapToJson(value.as(WenyanMapValue.TYPE));
            }
        } catch (WenyanException e) {
            return opaque(value, e.getMessage());
        }
        return opaque(value, "不支持的文言类型");
    }

    public static IWenyanValue fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return WenyanNull.NULL;
        }
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean() ? WenyanBoolean.TRUE : WenyanBoolean.FALSE;
            }
            if (primitive.isNumber()) {
                return new WenyanDouble(primitive.getAsDouble());
            }
            return new WenyanString(primitive.getAsString());
        }
        if (json.isJsonArray()) {
            WenyanList list = new WenyanList();
            for (JsonElement item : json.getAsJsonArray()) {
                list.add(fromJson(item));
            }
            return list;
        }
        if (json.isJsonObject()) {
            WenyanMapValue map = new WenyanMapValue();
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                map.put(entry.getKey(), fromJson(entry.getValue()));
            }
            return map;
        }
        return WenyanNull.NULL;
    }

    public static WenyanMapValue objectFromJson(JsonElement json) {
        IWenyanValue value = fromJson(json);
        return value.tryAs(WenyanMapValue.TYPE).orElseGet(() -> {
            WenyanMapValue fallback = new WenyanMapValue();
            fallback.put("乱", value);
            return fallback;
        });
    }

    private static JsonObject mapToJson(WenyanMapValue value) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, IWenyanValue> entry : value.values().entrySet()) {
            object.add(entry.getKey(), toJson(entry.getValue()));
        }
        return object;
    }

    private static JsonObject opaque(IWenyanValue value, String reason) {
        JsonObject object = new JsonObject();
        object.addProperty("乱", value == null ? "" : value.toString());
        object.addProperty("因", reason == null ? "" : reason);
        object.addProperty("类", value == null ? "null" : value.type().toString());
        return object;
    }
}
