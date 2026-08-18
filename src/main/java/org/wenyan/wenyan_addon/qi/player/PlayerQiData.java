package org.wenyan.wenyan_addon.qi.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家灵气数据：每个已注册属性独立储量与独立上限。
 * 初始仅有无属性上限 100，其余属性上限为 0（须通过特殊方法提升，如淬体仪式）。
 * 恢复量 = 恢复数额（restoreAmount）× 恢复系数（restoreRate），两个独立乘区。
 */
public class PlayerQiData {
    /**
     * 无属性灵气条初始上限。
     */
    public static final double INITIAL_NEUTRAL_CAP = 100.0;

    public static final Codec<Map<String, ElementCoefficients>> COEFFICIENTS_CODEC =
            Codec.unboundedMap(Codec.STRING, ElementCoefficients.CODEC.codec());

    /**
     * 旧版固定字段（metal/wood/.../neutral）+ 衍生属性 map + 各属性上限，兼容旧存档。
     */
    public static final MapCodec<PlayerQiData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("metal", 0.0).forGetter(data -> data.reserves.getOrDefault("metal", 0.0)),
            Codec.DOUBLE.optionalFieldOf("wood", 0.0).forGetter(data -> data.reserves.getOrDefault("wood", 0.0)),
            Codec.DOUBLE.optionalFieldOf("water", 0.0).forGetter(data -> data.reserves.getOrDefault("water", 0.0)),
            Codec.DOUBLE.optionalFieldOf("fire", 0.0).forGetter(data -> data.reserves.getOrDefault("fire", 0.0)),
            Codec.DOUBLE.optionalFieldOf("earth", 0.0).forGetter(data -> data.reserves.getOrDefault("earth", 0.0)),
            Codec.DOUBLE.optionalFieldOf("yin", 0.0).forGetter(data -> data.reserves.getOrDefault("yin", 0.0)),
            Codec.DOUBLE.optionalFieldOf("yang", 0.0).forGetter(data -> data.reserves.getOrDefault("yang", 0.0)),
            Codec.DOUBLE.optionalFieldOf("neutral", 0.0).forGetter(data -> data.reserves.getOrDefault("neutral", 0.0)),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("derived", Map.of())
                    .forGetter(PlayerQiData::derivedReserves),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("caps", Map.of())
                    .forGetter(PlayerQiData::capsMap),
            COEFFICIENTS_CODEC.optionalFieldOf("elementCoefficients", Map.of()).forGetter(PlayerQiData::coefficientsMap),
            Codec.STRING.optionalFieldOf("mainElement", "").forGetter(PlayerQiData::mainElement)
    ).apply(instance, PlayerQiData::fromCodecFields));

    /**
     * 客户端同步：全部储量 + 各属性上限。
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerQiData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                Map<String, Double> all = data.allReserves();
                buf.writeVarInt(all.size());
                for (Map.Entry<String, Double> entry : all.entrySet()) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
                    ByteBufCodecs.DOUBLE.encode(buf, entry.getValue());
                }
                Map<String, Double> caps = data.caps;
                buf.writeVarInt(caps.size());
                for (Map.Entry<String, Double> entry : caps.entrySet()) {
                    ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
                    ByteBufCodecs.DOUBLE.encode(buf, entry.getValue());
                }
            },
            buf -> {
                Map<String, Double> reserves = new HashMap<>();
                int size = buf.readVarInt();
                for (int i = 0; i < size; i++) {
                    reserves.put(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.DOUBLE.decode(buf));
                }
                Map<String, Double> caps = new HashMap<>();
                int capSize = buf.readVarInt();
                for (int i = 0; i < capSize; i++) {
                    caps.put(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.DOUBLE.decode(buf));
                }
                return PlayerQiData.fromStreamFields(reserves, caps);
            });

    private final HashMap<String, Double> reserves = new HashMap<>();
    private final HashMap<String, Double> caps = new HashMap<>();
    private final Map<String, ElementCoefficients> elementCoefficients = new HashMap<>();
    private String mainElement = "";
    /**
     * 数据版本号：任何修改操作递增，异步计算结果应用前比对（防旧数据覆盖新数据）。
     */
    private long version = 0;

    public long version() {
        return version;
    }

    private void bumpVersion() {
        version++;
    }

    public PlayerQiData() {
        caps.put(ElementType.NEUTRAL.id(), INITIAL_NEUTRAL_CAP);
        // 玩家初始生成：从每个属性类扫描默认系数
        for (ElementAttribute attribute : ElementRegistry.all()) {
            elementCoefficients.put(attribute.id(), attribute.defaultCoefficients());
        }
    }

    private static PlayerQiData fromCodecFields(double metal, double wood, double water, double fire, double earth,
                                                double yin, double yang, double neutral,
                                                Map<String, Double> derived,
                                                Map<String, Double> caps,
                                                Map<String, ElementCoefficients> coefficients,
                                                String mainElement) {
        PlayerQiData data = new PlayerQiData();
        data.reserves.put("metal", metal);
        data.reserves.put("wood", wood);
        data.reserves.put("water", water);
        data.reserves.put("fire", fire);
        data.reserves.put("earth", earth);
        data.reserves.put("yin", yin);
        data.reserves.put("yang", yang);
        data.reserves.put("neutral", neutral);
        data.reserves.putAll(derived);
        data.caps.clear();
        data.caps.putAll(caps);
        data.ensureDefaultCap();
        data.elementCoefficients.clear();
        data.elementCoefficients.putAll(coefficients);
        data.mainElement = mainElement;
        return data;
    }

    private static PlayerQiData fromStreamFields(Map<String, Double> allReserves, Map<String, Double> caps) {
        PlayerQiData data = new PlayerQiData();
        data.reserves.putAll(allReserves);
        data.caps.clear();
        data.caps.putAll(caps);
        data.ensureDefaultCap();
        return data;
    }

    /**
     * 旧档兼容：无属性初始上限缺失时补齐（旧存档无 caps 字段）。
     */
    private void ensureDefaultCap() {
        caps.putIfAbsent(ElementType.NEUTRAL.id(), INITIAL_NEUTRAL_CAP);
    }

    private Map<String, Double> derivedReserves() {
        Map<String, Double> derived = new HashMap<>();
        for (ElementAttribute attribute : ElementRegistry.derived()) {
            double amount = get(attribute);
            if (amount != 0) {
                derived.put(attribute.id(), amount);
            }
        }
        return derived;
    }

    private Map<String, Double> allReserves() {
        return reserves;
    }

    private Map<String, Double> capsMap() {
        return caps;
    }

    /**
     * 上限表只读访问（快照等外部只读场景）。
     */
    public Map<String, Double> capMap() {
        return caps;
    }

    // ===== 主属性 =====

    /**
     * 玩家主属性 id（首次开拓的属性，参与攻击与被攻击的五行计算；其余为辅属性）。
     */
    public String mainElement() {
        return mainElement;
    }

    /**
     * 设置主属性（首次淬体仪式开拓时记录，不可更改）。
     */
    public void setMainElement(String elementId) {
        if (mainElement.isEmpty() && elementId != null && !elementId.isEmpty()) {
            this.mainElement = elementId;
            bumpVersion();
        }
    }

    /**
     * 是否已选择主属性。
     */
    public boolean hasMainElement() {
        return !mainElement.isEmpty();
    }

    private Map<String, ElementCoefficients> coefficientsMap() {
        return elementCoefficients;
    }

    /**
     * 系数表只读访问（快照等外部只读场景）。
     */
    public Map<String, ElementCoefficients> coefficientMap() {
        return elementCoefficients;
    }

    // ===== 储量 =====

    public double get(ElementAttribute element) {
        return reserves.getOrDefault(element.id(), 0.0);
    }

    public Map<String, Double> reserves() {
        return reserves;
    }

    public boolean has(ElementAttribute element, double amount) {
        return get(element) >= amount;
    }

    public boolean consume(ElementAttribute element, double amount) {
        if (!has(element, amount)) {
            return false;
        }
        reserves.put(element.id(), get(element) - amount);
        bumpVersion();
        return true;
    }

    public void add(ElementAttribute element, double amount) {
        if (amount <= 0) {
            return;
        }
        double cap = cap(element);
        reserves.put(element.id(), Math.min(cap, get(element) + amount));
        bumpVersion();
    }

    /**
     * 清空所有属性灵气（淬体仪式结束等场景）。
     */
    public void clearAll() {
        reserves.clear();
        bumpVersion();
    }

    // ===== 上限 =====

    /**
     * 该属性灵气条上限（初始仅无属性 100，其余 0）。
     */
    public double cap(ElementAttribute element) {
        return caps.getOrDefault(element.id(), 0.0);
    }

    /**
     * 提升该属性灵气条上限（淬体仪式等特殊方法）。
     */
    public void increaseCap(ElementAttribute element, double amount) {
        if (amount > 0) {
            caps.merge(element.id(), amount, Double::sum);
            bumpVersion();
        }
    }

    public void setCap(ElementAttribute element, double amount) {
        caps.put(element.id(), amount);
        bumpVersion();
    }

    public double totalCap() {
        double sum = 0;
        for (double cap : caps.values()) {
            sum += cap;
        }
        return sum;
    }

    // ===== 总量 =====

    public double getTotal() {
        double sum = 0;
        for (ElementAttribute attribute : ElementRegistry.all()) {
            sum += get(attribute);
        }
        return sum;
    }

    // ===== 系数 =====

    public ElementCoefficients coefficients(ElementAttribute element) {
        return elementCoefficients.getOrDefault(element.id(), element.defaultCoefficients());
    }

    public void setCoefficients(ElementAttribute element, ElementCoefficients coefficients) {
        elementCoefficients.put(element.id(), coefficients);
        bumpVersion();
    }

    // ===== 恢复 =====

    /**
     * 属性灵力恢复统一入口（分步计算，所有参数来自玩家属性系数）：
     *
     * 基础值 = 恢复数额 × 恢复系数
     * 环境值 = 基础值 × 环境增益
     * 总恢复 = (基础值 + 环境值) × 灵脉加成
     * 计算主属性恢复值权重
     * 计算副属性恢复值权重
     */
    public void restoreAttribute(ElementAttribute element, double perTick,
                                 double environmentGain, double veinBoost) {
        ElementCoefficients c = coefficients(element);
        double base = c.restoreAmount() * c.restoreRate();
        double env = base * environmentGain;
        double total = (base + env) * veinBoost;
        ElementType generated = ElementRelations.generates(element);
        if (generated != null) {
            double sub = total * c.environmentSubRatio();
            add(element, (total - sub) * perTick);
            add(generated, sub * perTick);
        } else {
            add(element, total * perTick);
        }
    }

    /**
     * 匮乏漏气：全部五行系灵气按比例消散（0 表示无，1 表示全消散）。
     */
    public void leakQi(double ratePerSecond) {
        double total = getTotal();
        if (total <= 0) {
            return;
        }
        double scale = 1.0 - ratePerSecond / 20.0;
        for (ElementAttribute attribute : ElementRegistry.all()) {
            if (attribute != ElementType.YIN && attribute != ElementType.YANG) {
                reserves.put(attribute.id(), get(attribute) * scale);
            }
        }
        bumpVersion();
    }

    /**
     * 按比例缩放全部五行系储量（异步漏气结果应用）。
     */
    public void scaleQi(double scale) {
        if (scale <= 0) {
            return;
        }
        for (ElementAttribute attribute : ElementRegistry.all()) {
            if (attribute != ElementType.YIN && attribute != ElementType.YANG) {
                reserves.put(attribute.id(), get(attribute) * scale);
            }
        }
        bumpVersion();
    }

    /**
     * 阴阳自我恢复：阳/阴各自按时间比例的速度向各自上限恢复（上限由灵气条 cap 控制）。
     * 注：此昼夜恢复方式将在阴阳之地（M6）重构时移除。
     */
    public void updateYinYang(double yangRatio, double perTick) {
        double yangCap = cap(ElementType.YANG);
        double yinCap = cap(ElementType.YIN);
        double yang = get(ElementType.YANG);
        double yin = get(ElementType.YIN);
        double yangRestore = perTick * yangRatio * coefficients(ElementType.YANG).yinYangRestoreRate();
        double yinRestore = perTick * (1.0 - yangRatio) * coefficients(ElementType.YIN).yinYangRestoreRate();
        reserves.put(ElementType.YANG.id(), Math.min(yangCap, yang + yangRestore));
        reserves.put(ElementType.YIN.id(), Math.min(yinCap, yin + yinRestore));
        bumpVersion();
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
