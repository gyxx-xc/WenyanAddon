package org.wenyan.wenyan_addon.qi.element;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 元素属性注册表：五行/阴阳/无属性（枚举）在类加载时注册；
 * 衍生属性可运行时注册，克制关系继承其五行基底。
 */
public final class ElementRegistry {
    private static final Map<String, ElementAttribute> ELEMENTS = new LinkedHashMap<>();

    static {
        for (ElementType type : ElementType.values()) {
            ELEMENTS.put(type.id(), type);
        }
    }

    private ElementRegistry() {
    }

    public static ElementAttribute register(ElementAttribute attribute) {
        ElementAttribute previous = ELEMENTS.putIfAbsent(attribute.id(), attribute);
        if (previous != null) {
            throw new IllegalArgumentException("重复注册元素属性: " + attribute.id());
        }
        return attribute;
    }

    @Nullable
    public static ElementAttribute byId(String id) {
        return ELEMENTS.get(id);
    }

    public static Collection<ElementAttribute> all() {
        return ELEMENTS.values();
    }

    /**
     * 已注册的衍生属性（不含五行/阴阳/无属性）。
     */
    public static List<ElementAttribute> derived() {
        return ELEMENTS.values().stream()
                .filter(attribute -> !(attribute instanceof ElementType))
                .toList();
    }

    /**
     * 清空衍生属性（预留：数据包重载时调用，模组注册的衍生属性需重新注册）。
     */
    public static void reset() {
        ELEMENTS.entrySet().removeIf(entry -> !(entry.getValue() instanceof ElementType));
    }
}
