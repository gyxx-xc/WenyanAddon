package org.wenyan.wenyan_addon.qi.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PlayerQiData {
    public static final double MAX_QI = 100.0;
    public static final double ELEMENTS_TOTAL_CAP_RATIO = 0.5;
    public static final double YIN_YANG_CAP = 100.0;
    public static final double YIN_YANG_CONVERT_RATE = 0.002;

    public static final Codec<EnumMap<ElementType, ElementCoefficients>> COEFFICIENTS_CODEC =
            Codec.unboundedMap(Codec.STRING, ElementCoefficients.CODEC.codec())
                    .xmap(PlayerQiData::toCoefficientMap, PlayerQiData::fromCoefficientMap);

    public static final MapCodec<PlayerQiData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("metal", 0.0).forGetter(data -> data.get(ElementType.METAL)),
            Codec.DOUBLE.optionalFieldOf("wood", 0.0).forGetter(data -> data.get(ElementType.WOOD)),
            Codec.DOUBLE.optionalFieldOf("water", 0.0).forGetter(data -> data.get(ElementType.WATER)),
            Codec.DOUBLE.optionalFieldOf("fire", 0.0).forGetter(data -> data.get(ElementType.FIRE)),
            Codec.DOUBLE.optionalFieldOf("earth", 0.0).forGetter(data -> data.get(ElementType.EARTH)),
            Codec.DOUBLE.optionalFieldOf("yin", 0.0).forGetter(data -> data.get(ElementType.YIN)),
            Codec.DOUBLE.optionalFieldOf("yang", 0.0).forGetter(data -> data.get(ElementType.YANG)),
            Codec.DOUBLE.optionalFieldOf("neutral", 0.0).forGetter(data -> data.get(ElementType.NEUTRAL)),
            COEFFICIENTS_CODEC.optionalFieldOf("elementCoefficients", new EnumMap<>(ElementType.class)).forGetter(PlayerQiData::elementCoefficients)
    ).apply(instance, PlayerQiData::fromCodecFields));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerQiData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.METAL),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.WOOD),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.WATER),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.FIRE),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.EARTH),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.YIN),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.YANG),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.NEUTRAL),
            PlayerQiData::fromStreamFields
    );

    private final EnumMap<ElementType, Double> reserves = new EnumMap<>(ElementType.class);
    private final EnumMap<ElementType, ElementCoefficients> elementCoefficients = new EnumMap<>(ElementType.class);

    public PlayerQiData() {
        for (ElementType element : ElementType.values()) {
            reserves.put(element, 0.0);
        }
    }

    private static PlayerQiData fromCodecFields(double metal, double wood, double water, double fire, double earth,
                                                double yin, double yang, double neutral,
                                                EnumMap<ElementType, ElementCoefficients> coefficients) {
        PlayerQiData data = fromStreamFields(metal, wood, water, fire, earth, yin, yang, neutral);
        data.elementCoefficients.putAll(coefficients);
        return data;
    }

    private static PlayerQiData fromStreamFields(double metal, double wood, double water, double fire, double earth,
                                                 double yin, double yang, double neutral) {
        PlayerQiData data = new PlayerQiData();
        data.reserves.put(ElementType.METAL, metal);
        data.reserves.put(ElementType.WOOD, wood);
        data.reserves.put(ElementType.WATER, water);
        data.reserves.put(ElementType.FIRE, fire);
        data.reserves.put(ElementType.EARTH, earth);
        data.reserves.put(ElementType.YIN, yin);
        data.reserves.put(ElementType.YANG, yang);
        data.reserves.put(ElementType.NEUTRAL, neutral);
        return data;
    }

    private static EnumMap<ElementType, ElementCoefficients> toCoefficientMap(Map<String, ElementCoefficients> map) {
        EnumMap<ElementType, ElementCoefficients> result = new EnumMap<>(ElementType.class);
        map.forEach((name, coefficients) -> {
            try {
                result.put(ElementType.valueOf(name), coefficients);
            } catch (IllegalArgumentException ignored) {
            }
        });
        return result;
    }

    private static Map<String, ElementCoefficients> fromCoefficientMap(EnumMap<ElementType, ElementCoefficients> map) {
        Map<String, ElementCoefficients> result = new HashMap<>();
        map.forEach((element, coefficients) -> result.put(element.name(), coefficients));
        return result;
    }

    public double get(ElementType element) {
        return reserves.getOrDefault(element, 0.0);
    }

    public Map<ElementType, Double> reserves() {
        return reserves;
    }

    public EnumMap<ElementType, ElementCoefficients> elementCoefficients() {
        return elementCoefficients;
    }

    public ElementCoefficients coefficients(ElementType element) {
        return elementCoefficients.getOrDefault(element, ElementCoefficients.DEFAULT);
    }

    public void setCoefficients(ElementType element, ElementCoefficients coefficients) {
        elementCoefficients.put(element, coefficients);
    }

    public double getTotal() {
        double sum = 0;
        for (ElementType element : ElementType.values()) {
            if (element != ElementType.NEUTRAL && element != ElementType.YIN && element != ElementType.YANG) {
                sum += get(element);
            }
        }
        return sum + get(ElementType.NEUTRAL);
    }

    public boolean has(ElementType element, double amount) {
        return get(element) >= amount;
    }

    public void add(ElementType element, double amount) {
        if (element == ElementType.YIN || element == ElementType.YANG) {
            reserves.put(element, Math.min(YIN_YANG_CAP, get(element) + amount));
            return;
        }
        double totalAvailable = MAX_QI - getTotal();
        if (totalAvailable <= 0) {
            return;
        }
        amount = Math.min(amount, totalAvailable);
        if (element == ElementType.NEUTRAL) {
            reserves.put(element, get(element) + amount);
            return;
        }
        double elementsTotal = totalElements();
        double cap = MAX_QI * ELEMENTS_TOTAL_CAP_RATIO;
        double added = Math.min(amount, cap - elementsTotal);
        if (added > 0) {
            reserves.put(element, get(element) + added);
        }
    }

    private double totalElements() {
        double sum = 0;
        for (ElementType element : ElementType.values()) {
            if (element != ElementType.NEUTRAL && element != ElementType.YIN && element != ElementType.YANG) {
                sum += get(element);
            }
        }
        return sum;
    }

    public boolean consume(ElementType element, double amount) {
        if (!has(element, amount)) {
            return false;
        }
        reserves.put(element, get(element) - amount);
        return true;
    }

    public void restoreNatural(double perTick) {
        add(ElementType.NEUTRAL, perTick * coefficients(ElementType.NEUTRAL).restoreRate());
    }

    public void restoreEnvironment(ElementType element, double perTick) {
        ElementCoefficients coefficients = coefficients(element);
        double amount = perTick * coefficients.restoreRate();
        double main = amount * coefficients.environmentMainRatio();
        double sub = amount * coefficients.environmentSubRatio();
        double elementsBefore = totalElements();
        add(element, main);
        ElementType generated = ElementRelations.generates(element);
        if (generated != null) {
            add(generated, sub);
        }
        double elementsAfter = totalElements();
        double wasted = (main + sub) - (elementsAfter - elementsBefore);
        if (wasted > 0) {
            add(ElementType.NEUTRAL, wasted);
        }
    }

    /**
     * 阴阳自我恢复：阳/阴各自按时间比例的速度向上限恢复；
     * 超过总上限的部分等比压缩，超出的部分逐步转化为另一种灵气（相互转换）。
     */
    public void updateYinYang(double yangRatio, double perTick) {
        double yangCap = YIN_YANG_CAP * yangRatio;
        double yang = get(ElementType.YANG);
        double yin = get(ElementType.YIN);
        double yangRestore = perTick * yangRatio * coefficients(ElementType.YANG).yinYangRestoreRate();
        double yinRestore = perTick * (1.0 - yangRatio) * coefficients(ElementType.YIN).yinYangRestoreRate();
        double newYang = yang + yangRestore;
        double newYin = yin + yinRestore;
        double total = newYang + newYin;
        if (total > YIN_YANG_CAP) {
            double scale = YIN_YANG_CAP / total;
            newYang *= scale;
            newYin *= scale;
            total = YIN_YANG_CAP;
        }
        double convert = (total * yangRatio - newYang) * YIN_YANG_CONVERT_RATE;
        double convertedYang = Math.max(0, Math.min(yangCap, newYang + convert));
        reserves.put(ElementType.YANG, convertedYang);
        reserves.put(ElementType.YIN, Math.max(0, total - convertedYang));
    }

    /**
     * 阳占比曲线（基于白天时长 0~24000，0=6点）：
     * 6→12点 阳逐步满 0→1；12→18点 阳为主 1；
     * 18→0点 阴逐步满 1→0；0→6点 阴为主 0。
     */
    public static double yangRatio(double dayTime) {
        if (dayTime < 6000) {
            return dayTime / 6000.0;
        }
        if (dayTime < 12000) {
            return 1.0;
        }
        if (dayTime < 18000) {
            return 1.0 - (dayTime - 12000) / 6000.0;
        }
        return 0.0;
    }
}
