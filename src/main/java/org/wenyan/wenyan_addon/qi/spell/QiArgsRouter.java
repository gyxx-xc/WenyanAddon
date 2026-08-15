package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.judou.api.values.IWenyanValue;

import java.util.List;

/**
 * 参数类型匹配：按 {@link IWenyanValue#type()} 的 tClass 与声明类型列表逐项比对。
 */
public final class QiArgsRouter {
    private QiArgsRouter() {
    }

    /**
     * args 是否匹配声明类型组合（数量与每项类型一致）。
     *
     * @param expected 声明的参数类型（IWenyanValue 实现类，对应 type().tClass）
     */
    public static boolean matches(Class<? extends IWenyanValue>[] expected, List<IWenyanValue> args) {
        if (expected.length != args.size()) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            Class<?> actual = args.get(i).type().tClass;
            if (!expected[i].isAssignableFrom(actual)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 组合描述文本（异常提示用）：如 [str, int]。
     */
    public static String describe(Class<? extends IWenyanValue>[] expected) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < expected.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(simpleName(expected[i]));
        }
        return sb.append("]").toString();
    }

    private static String simpleName(Class<?> type) {
        String name = type.getSimpleName();
        if (name.startsWith("Wenyan")) {
            return name.substring("Wenyan".length()).toLowerCase();
        }
        return name.toLowerCase();
    }
}
