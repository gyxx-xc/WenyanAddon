package org.wenyan.wenyan_addon.qi.element;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 元素属性注册表：五行/阴阳/无属性（枚举）在类加载时注册；
 * 衍生属性可运行时注册（支持嵌套继承），注册时做循环依赖检测。
 */
public final class ElementRegistry {
    private static final Logger log = LoggerFactory.getLogger(ElementRegistry.class);
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
        checkCycles(attribute);
        DERIVED_COEFFICIENTS.clear();
        log.info("已注册衍生属性「{}」({})，当前共 {} 种",
                attribute.displayName(), attribute.id(), derived().size());
        return attribute;
    }

    /**
     * 循环依赖检测（DFS）：沿基底链遍历，遇到已访问节点即视为循环。
     * 断开祖先链（ancestorBreak）的基底作为根，不再继续溯源。
     */
    private static void checkCycles(ElementAttribute attribute) {
        checkCycles(attribute, new HashSet<>());
    }

    private static void checkCycles(ElementAttribute current, Set<String> visited) {
        if (!visited.add(current.id())) {
            throw new IllegalStateException("循环依赖检测失败: " + visited + " 中出现 " + current.id());
        }
        for (ElementAttribute base : current.bases()) {
            if (!(base instanceof ElementType) && !base.ancestorBreak()) {
                checkCycles(base, visited);
            }
        }
        visited.remove(current.id());
    }

    @Nullable
    public static ElementAttribute byId(String id) {
        return ELEMENTS.get(id);
    }

    /**
     * 衍生属性默认系数：懒计算缓存（五行/显式配置直接返回）。
     */
    public static ElementCoefficients coefficients(ElementAttribute element) {
        if (element instanceof ElementType type) {
            return type.defaultCoefficients();
        }
        if (element instanceof DerivedElement derived && derived.explicit() != null) {
            return derived.explicit();
        }
        return DERIVED_COEFFICIENTS.computeIfAbsent(element.id(),
                id -> DerivedCoefficientsCalculator.calculate(element, null));
    }

    private static final java.util.Map<String, ElementCoefficients> DERIVED_COEFFICIENTS = new java.util.HashMap<>();

    static void invalidateCoefficients() {
        DERIVED_COEFFICIENTS.clear();
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

    /**
     * 输出已注册衍生属性总数（注册完毕后调用；附属 mod 可自行调用）。
     */
    public static void logSummary() {
        log.info("衍生属性注册完毕，共 {} 种", derived().size());
    }
}
