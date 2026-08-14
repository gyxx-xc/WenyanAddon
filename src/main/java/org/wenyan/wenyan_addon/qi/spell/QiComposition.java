package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 输入灵气组成：运行时从玩家持有的灵气中提取（全部已注册属性，含衍生）。
 * 五行标签匹配只看五行系 + 无属性；阴阳倾向单独参与匹配。
 */
public record QiComposition(Map<ElementAttribute, Double> amounts) {

    public static final QiComposition EMPTY = new QiComposition(Map.of());

    public static QiComposition of(PlayerQiData qi) {
        Map<ElementAttribute, Double> amounts = new HashMap<>();
        for (ElementAttribute element : ElementRegistry.all()) {
            amounts.put(element, qi.get(element));
        }
        return new QiComposition(amounts);
    }

    public double total() {
        double sum = 0;
        for (double amount : amounts.values()) {
            sum += amount;
        }
        return sum;
    }

    public boolean isEmpty() {
        return total() <= 0;
    }

    public boolean contains(ElementAttribute element) {
        return amounts.getOrDefault(element, 0.0) > 0;
    }

    /**
     * 储量大于 0 的五行系 + 无属性集合（阴阳不参与五行标签匹配）。
     */
    public Set<ElementAttribute> present() {
        return amounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(element -> element != ElementType.YIN && element != ElementType.YANG)
                .collect(Collectors.toSet());
    }

    /**
     * 储量最大的五行系/无属性；无五行灵气时返回 null。
     */
    public ElementAttribute dominant() {
        ElementAttribute dominant = null;
        double max = 0;
        for (ElementAttribute element : present()) {
            double value = amounts.get(element);
            if (value > max) {
                max = value;
                dominant = element;
            }
        }
        return dominant;
    }
}
