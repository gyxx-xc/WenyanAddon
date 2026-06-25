package org.wenyan.wenyan_addon.value;

import indi.wenyan.judou.api.WenyanType;
import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.IWenyanObject;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WenyanMapValue implements IWenyanObject {
    public static final WenyanType<WenyanMapValue> TYPE = new WenyanType<>("圖", WenyanMapValue.class);

    private final LinkedHashMap<String, IWenyanValue> values;

    public WenyanMapValue() {
        this.values = new LinkedHashMap<>();
    }

    public WenyanMapValue(Map<String, IWenyanValue> values) {
        this.values = new LinkedHashMap<>(values);
    }

    public Map<String, IWenyanValue> values() {
        return values;
    }

    public IWenyanValue get(String key) {
        return values.getOrDefault(key, WenyanNull.NULL);
    }

    public void put(String key, IWenyanValue value) {
        values.put(key, value == null ? WenyanNull.NULL : value);
    }

    public IWenyanValue remove(String key) {
        IWenyanValue removed = values.remove(key);
        return removed == null ? WenyanNull.NULL : removed;
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }

    @Override
    public IWenyanValue getAttribute(String attribute) throws WenyanException {
        return switch (attribute) {
            case "「取」" -> WenyanValues.of((_, args) -> get(keyArg(args, 0)));
            case "「置」" -> WenyanValues.of((_, args) -> {
                put(keyArg(args, 0), args.size() > 1 ? args.get(1) : WenyanNull.NULL);
                return this;
            });
            case "「有」" -> WenyanValues.of((_, args) -> WenyanValues.of(contains(keyArg(args, 0))));
            case "「删」", "「刪」" -> WenyanValues.of((_, args) -> remove(keyArg(args, 0)));
            case "「鍵」", "「键」" -> keys();
            case "「長」", "「长」" -> WenyanValues.of(values.size());
            default -> throw new WenyanException("無屬性「" + attribute + "」");
        };
    }

    @Override
    public WenyanType<?> type() {
        return TYPE;
    }

    @Override
    public String toString() {
        return values.toString();
    }

    private WenyanList keys() {
        WenyanList result = new WenyanList();
        for (String key : values.keySet()) {
            result.add(new WenyanString(key));
        }
        return result;
    }

    private static String keyArg(List<IWenyanValue> args, int index) throws WenyanException.WenyanTypeException {
        if (args.size() <= index) {
            return "";
        }
        return args.get(index).as(WenyanString.TYPE).value();
    }
}
