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
import java.util.Map;

public class PlayerQiData {
    public static final double MAX_QI = 100.0;
    public static final double ELEMENTS_TOTAL_CAP_RATIO = 0.5;
    public static final double NATURAL_RESTORE_PER_SECOND = 1.0;

    public static final MapCodec<PlayerQiData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("metal", 0.0).forGetter(data -> data.get(ElementType.METAL)),
            Codec.DOUBLE.optionalFieldOf("wood", 0.0).forGetter(data -> data.get(ElementType.WOOD)),
            Codec.DOUBLE.optionalFieldOf("water", 0.0).forGetter(data -> data.get(ElementType.WATER)),
            Codec.DOUBLE.optionalFieldOf("fire", 0.0).forGetter(data -> data.get(ElementType.FIRE)),
            Codec.DOUBLE.optionalFieldOf("earth", 0.0).forGetter(data -> data.get(ElementType.EARTH)),
            Codec.DOUBLE.optionalFieldOf("neutral", 0.0).forGetter(data -> data.get(ElementType.NEUTRAL))
    ).apply(instance, PlayerQiData::fromFields));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerQiData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.METAL),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.WOOD),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.WATER),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.FIRE),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.EARTH),
            ByteBufCodecs.DOUBLE, data -> data.get(ElementType.NEUTRAL),
            PlayerQiData::fromFields
    );

    private final EnumMap<ElementType, Double> reserves = new EnumMap<>(ElementType.class);

    public PlayerQiData() {
        for (ElementType element : ElementType.values()) {
            reserves.put(element, 0.0);
        }
    }

    private static PlayerQiData fromFields(double metal, double wood, double water, double fire, double earth, double neutral) {
        PlayerQiData data = new PlayerQiData();
        data.reserves.put(ElementType.METAL, metal);
        data.reserves.put(ElementType.WOOD, wood);
        data.reserves.put(ElementType.WATER, water);
        data.reserves.put(ElementType.FIRE, fire);
        data.reserves.put(ElementType.EARTH, earth);
        data.reserves.put(ElementType.NEUTRAL, neutral);
        return data;
    }

    public double get(ElementType element) {
        return reserves.getOrDefault(element, 0.0);
    }

    public Map<ElementType, Double> reserves() {
        return reserves;
    }

    public double getTotal() {
        return reserves.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public boolean has(ElementType element, double amount) {
        return get(element) >= amount;
    }

    public void add(ElementType element, double amount) {
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
            if (element != ElementType.NEUTRAL) {
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

    public void restoreNatural(double amount) {
        add(ElementType.NEUTRAL, amount);
    }

    public void restoreEnvironment(ElementType element, double amount) {
        double main = amount * 0.8;
        double sub = amount * 0.2;
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
}
