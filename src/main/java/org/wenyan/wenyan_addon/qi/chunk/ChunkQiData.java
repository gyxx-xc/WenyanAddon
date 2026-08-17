package org.wenyan.wenyan_addon.qi.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 区块灵气数据：五行储量 + 阴阳值 + 结构占比。
 * 结构占比（proportions）：每个属性在该区块灵气结构中的固定百分比
 * （主 80% / 相生各 7.5% / 其余各 2.5%，由群系主属性决定并持久化），
 * 决定该属性灵气可恢复到区块总量的比例。
 */
public class ChunkQiData {
    public static final double MAX_CAP = 20000.0;
    public static final double VEIN_CAP_BONUS = 2000.0;
    public static final double VEIN_RESTORE_MULTIPLIER = 1.2;

    public static final Codec<ChunkQiData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("metal").forGetter(data -> data.get(ElementType.METAL)),
            Codec.DOUBLE.fieldOf("wood").forGetter(data -> data.get(ElementType.WOOD)),
            Codec.DOUBLE.fieldOf("water").forGetter(data -> data.get(ElementType.WATER)),
            Codec.DOUBLE.fieldOf("fire").forGetter(data -> data.get(ElementType.FIRE)),
            Codec.DOUBLE.fieldOf("earth").forGetter(data -> data.get(ElementType.EARTH)),
            Codec.DOUBLE.fieldOf("yin").forGetter(data -> data.yin),
            Codec.DOUBLE.fieldOf("yang").forGetter(data -> data.yang),
            Codec.DOUBLE.fieldOf("qiCap").forGetter(data -> data.qiCap),
            Codec.BOOL.fieldOf("depleted").forGetter(data -> data.depleted),
            Codec.INT.fieldOf("veinStage").forGetter(data -> data.veinStage),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("proportions", Map.of())
                    .forGetter(data -> data.proportions)
    ).apply(instance, ChunkQiData::fromFields));

    private final EnumMap<ElementType, Double> elements = new EnumMap<>(ElementType.class);
    private double yin;
    private double yang;
    private double qiCap;
    private boolean depleted;
    private int veinStage;
    private final Map<String, Double> proportions = new HashMap<>();

    public ChunkQiData(double qiCap) {
        for (ElementType element : ElementRelations.ELEMENTS) {
            elements.put(element, 0.0);
        }
        this.qiCap = Math.min(MAX_CAP, qiCap);
    }

    private static ChunkQiData fromFields(double metal, double wood, double water, double fire, double earth,
                                          double yin, double yang, double qiCap, boolean depleted, int veinStage,
                                          Map<String, Double> proportions) {
        ChunkQiData data = new ChunkQiData(qiCap);
        data.elements.put(ElementType.METAL, metal);
        data.elements.put(ElementType.WOOD, wood);
        data.elements.put(ElementType.WATER, water);
        data.elements.put(ElementType.FIRE, fire);
        data.elements.put(ElementType.EARTH, earth);
        data.yin = yin;
        data.yang = yang;
        data.depleted = depleted;
        data.veinStage = veinStage;
        data.proportions.putAll(proportions);
        return data;
    }

    public double get(ElementType element) {
        return elements.getOrDefault(element, 0.0);
    }

    public double yin() {
        return yin;
    }

    public double yang() {
        return yang;
    }

    /**
     * 设置阴阳值（阴阳之地生成时初始化）。
     */
    public void setYinYang(double yin, double yang) {
        this.yin = yin;
        this.yang = yang;
    }

    public double qiCap() {
        return qiCap;
    }

    public double effectiveCap() {
        return Math.min(MAX_CAP, qiCap + veinStage * VEIN_CAP_BONUS);
    }

    public int veinStage() {
        return veinStage;
    }

    public void setVeinStage(int veinStage) {
        this.veinStage = veinStage;
    }

    public boolean isDepleted() {
        return depleted;
    }

    public double total() {
        return elements.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double activity() {
        return Math.min(1.0, total() / effectiveCap());
    }

    /**
     * 区块灵气余量百分比（0~1）：1 - 活性。
     */
    public double remainingRatio() {
        return 1.0 - activity();
    }

    // ===== 结构占比 =====

    public Map<String, Double> proportions() {
        return proportions;
    }

    public void setProportions(Map<String, Double> proportions) {
        this.proportions.clear();
        this.proportions.putAll(proportions);
    }

    /**
     * 该属性在区块灵气结构中的固定占比（0~1），决定其可恢复到区块总量的比例。
     * 未设置结构时返回 0。
     */
    public double ratio(ElementAttribute element) {
        return proportions.getOrDefault(element.id(), 0.0);
    }

    /**
     * 按结构占比把 amount 分配到各属性。
     */
    public void distribute(double amount) {
        for (Map.Entry<String, Double> entry : proportions.entrySet()) {
            ElementType element = elementById(entry.getKey());
            if (element != null) {
                add(element, amount * entry.getValue());
            }
        }
    }

    private static ElementType elementById(String id) {
        for (ElementType element : ElementRelations.ELEMENTS) {
            if (element.id().equals(id)) {
                return element;
            }
        }
        return null;
    }

    /**
     * 该元素在区块中的浓度（储量 / 区块上限，0~1）。
     */
    public double concentration(ElementType element) {
        double cap = effectiveCap();
        return cap <= 0 ? 0 : get(element) / cap;
    }

    public ElementType dominantElement() {
        ElementType dominant = null;
        double max = 0;
        for (ElementType element : ElementRelations.ELEMENTS) {
            double value = get(element);
            if (value > max) {
                max = value;
                dominant = element;
            }
        }
        return dominant;
    }

    public void add(ElementType element, double amount) {
        double cap = effectiveCap();
        double total = total();
        double added = Math.min(amount, cap - total);
        if (added > 0) {
            elements.put(element, get(element) + added);
        }
        depleted = total() <= 0;
    }

    public boolean consume(ElementType element, double amount) {
        if (get(element) < amount) {
            return false;
        }
        elements.put(element, get(element) - amount);
        depleted = total() <= 0;
        return true;
    }
}
