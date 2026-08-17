package org.wenyan.wenyan_addon.qi.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.land.YinYangLandType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkQiManager extends SavedData {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "chunk_qi");
    public static final double INITIAL_ACTIVITY = 0.5;
    public static final double DIFFUSE_COEFFICIENT = 0.01;
    public static final double VEIN_DIFFUSE_COEFFICIENT = 0.02;
    public static final double VEIN_GENERATION_CHANCE = 0.02;
    public static final long VEIN_NURTURE_INTERVAL = 36000;
    public static final double VEIN_NURTURE_RATIO = 0.001;
    public static final int VEIN_INITIAL_MIN_COVERAGE = 1;
    public static final int VEIN_INITIAL_MAX_COVERAGE = 3;
    public static final int ACTIVE_RADIUS = 8;
    public static final int CORE_RADIUS = 4;
    public static final int CHUNK_TICK_INTERVAL = 40;
    public static final int CHUNKS_PER_TICK = 200;
    public static final int ACTIVE_CACHE_TTL = 40;
    public static final long VEIN_TICK_INTERVAL = 40;
    public static final double YIN_YANG_LAND_CHANCE = 0.01;
    public static final int YIN_YANG_MIN_COVERAGE = 4;
    public static final int YIN_YANG_MAX_COVERAGE = 16;

    public static final Codec<ChunkQiManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ChunkQiData.CODEC).fieldOf("chunks").forGetter(ChunkQiManager::chunks),
            Codec.unboundedMap(Codec.STRING, QiVein.CODEC).fieldOf("veins").forGetter(ChunkQiManager::veins),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("yinYangLands", Map.of())
                    .forGetter(ChunkQiManager::yinYangLands)
    ).apply(instance, ChunkQiManager::new));

    public static final SavedDataType<ChunkQiManager> TYPE = new SavedDataType<>(ID, ChunkQiManager::new, CODEC);
    private static final Logger log = LoggerFactory.getLogger(ChunkQiManager.class);

    private final Map<String, ChunkQiData> chunks = new ConcurrentHashMap<>();
    private final Map<String, QiVein> veins = new ConcurrentHashMap<>();
    private final Map<String, YinYangLandType> yinYangLands = new ConcurrentHashMap<>();
    private final Map<String, Long> lastProcessedTicks = new HashMap<>();
    private Set<String> activeChunks = Set.of();
    private Set<String> coreChunks = Set.of();
    private long activeChunksRefreshedAt = 0;
    private long lastVeinTickTime = 0;

    public ChunkQiManager() {
    }

    private ChunkQiManager(Map<String, ChunkQiData> chunks, Map<String, QiVein> veins,
                           Map<String, String> yinYangLands) {
        this.chunks.putAll(chunks);
        this.veins.putAll(veins);
        yinYangLands.forEach((key, value) -> this.yinYangLands.put(key, YinYangLandType.valueOf(value)));
    }

    private Map<String, String> yinYangLands() {
        Map<String, String> result = new HashMap<>();
        yinYangLands.forEach((key, value) -> result.put(key, value.name()));
        return result;
    }

    public Map<String, ChunkQiData> chunks() {
        return chunks;
    }

    public Map<String, QiVein> veins() {
        return veins;
    }

    private static final Object DATA_STORAGE_LOCK = new Object();

    public static ChunkQiManager of(ServerLevel level) {
        // SavedDataStorage 内部为普通 HashMap，server tick 与文言线程并发访问需串行化
        synchronized (DATA_STORAGE_LOCK) {
            return level.getDataStorage().computeIfAbsent(TYPE);
        }
    }

    public ChunkQiData getChunkQi(ServerLevel level, ChunkPos pos) {
        String key = keyOf(pos);
        ChunkQiData data = chunks.get(key);
        if (data != null) {
            return data;
        }
        // 双检锁：创建过程可能触发 spawnVein / 阴阳之地生成（写入 chunks/veins/yinYangLands），
        // 必须脱离并发修改期，避免自修改与文言线程并发竞态。
        synchronized (chunks) {
            data = chunks.get(key);
            if (data == null) {
                data = createChunkQi(level, pos);
                chunks.put(key, data);
                setDirty();
                if (!yinYangLands.containsKey(key) && level.getRandom().nextDouble() < YIN_YANG_LAND_CHANCE) {
                    generateYinYangLand(level, pos);
                }
                if (level.getRandom().nextDouble() < VEIN_GENERATION_CHANCE) {
                    spawnVein(level, pos);
                }
                // 阴阳之地：初始化区块阴阳值（富集对应属性）
                applyYinYangValues(level, pos, data);
            }
        }
        return data;
    }

    /**
     * 阴阳之地阴阳值初始化：阴之地 yin = 上限×0.3、阳之地 yang = 上限×0.3；普通区块为 0。
     */
    private void applyYinYangValues(ServerLevel level, ChunkPos pos, ChunkQiData data) {
        YinYangLandType type = yinYangLands.get(keyOf(pos));
        if (type == null) {
            return;
        }
        double amount = data.qiCap() * 0.3;
        if (type == YinYangLandType.YIN) {
            data.setYinYang(amount, amount * 0.2);
        } else {
            data.setYinYang(amount * 0.2, amount);
        }
        setDirty();
    }

    /**
     * 阴阳之地生成：1% 概率成为核心（随机阴/阳），随机游走扩展 3-15 个连续区块。
     */
    private void generateYinYangLand(ServerLevel level, ChunkPos core) {
        YinYangLandType type = level.getRandom().nextBoolean() ? YinYangLandType.YIN : YinYangLandType.YANG;
        int coverage = YIN_YANG_MIN_COVERAGE
                + level.getRandom().nextInt(YIN_YANG_MAX_COVERAGE - YIN_YANG_MIN_COVERAGE + 1);
        Set<ChunkPos> region = new HashSet<>();
        region.add(core);
        ChunkPos current = core;
        while (region.size() < coverage) {
            ChunkPos next = new ChunkPos(
                    current.x() + level.getRandom().nextInt(3) - 1,
                    current.z() + level.getRandom().nextInt(3) - 1);
            if (!region.contains(next) && !yinYangLands.containsKey(keyOf(next))) {
                region.add(next);
            }
            current = next;
        }
        for (ChunkPos pos : region) {
            yinYangLands.put(keyOf(pos), type);
        }
        log.info("生成阴阳之地: {} ({} 区块, {}地)", core, region.size(), type == YinYangLandType.YIN ? "阴" : "阳");
    }

    /**
     * 该区块的阴阳之地类型（非阴阳之地返回 null）。
     */
    public YinYangLandType landTypeAt(ChunkPos pos) {
        return yinYangLands.get(keyOf(pos));
    }

    /**
     * 该区块是否阴阳之地。
     */
    public boolean isYinYangLand(ChunkPos pos) {
        return yinYangLands.containsKey(keyOf(pos));
    }

    /**
     * 新建区块：按群系主属性的结构占比分配初始灵气（主 80% / 相生 15% / 其余 5%）。
     */
    private ChunkQiData createChunkQi(ServerLevel level, ChunkPos pos) {
        ChunkQiData data = new ChunkQiData(baseCap(level, pos));
        data.setProportions(QiDistribution.of(preferredElement(level, pos)).ratios());
        data.distribute(data.qiCap() * INITIAL_ACTIVITY);
        return data;
    }

    /**
     * 旧档兼容：区块缺少结构占比时按群系主属性补齐。
     */
    private void ensureProportions(ServerLevel level, ChunkPos pos, ChunkQiData data) {
        if (data.proportions().isEmpty()) {
            data.setProportions(QiDistribution.of(preferredElement(level, pos)).ratios());
        }
    }

    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        refreshActiveChunks(level, gameTime);
        if (activeChunks.isEmpty()) {
            return;
        }
        // 动态预算：多玩家时按活跃区块数自适应，保证 10 次调用内覆盖全部
        int budget = Math.max(CHUNKS_PER_TICK, activeChunks.size() / 10);
        int processed = 0;
        // 玩家周围核心区块优先处理，其余区块兜底
        processed = processEligible(level, gameTime, coreChunks, budget, processed);
        if (processed < budget) {
            processEligible(level, gameTime, activeChunks, budget, processed);
        }
    }

    private int processEligible(ServerLevel level, long gameTime, Set<String> keys, int budget, int processed) {
        boolean[] changedRef = new boolean[]{false};
        for (String key : keys) {
            if (processed >= budget) {
                break;
            }
            long last = lastProcessedTicks.getOrDefault(key, gameTime - CHUNK_TICK_INTERVAL);
            if (gameTime - last < CHUNK_TICK_INTERVAL) {
                continue;
            }
            ChunkQiData data = chunks.get(key);
            ChunkPos pos = posOf(key);
            if (data == null || !level.isLoaded(pos.getMiddleBlockPosition(0)) || isEnd(level, pos)) {
                lastProcessedTicks.put(key, gameTime);
                continue;
            }
            processChunk(level, pos, data, gameTime - last);
            lastProcessedTicks.put(key, gameTime);
            changedRef[0] = true;
            processed++;
        }
        if (changedRef[0]) {
            setDirty();
        }
        return processed;
    }

    /**
     * 玩家周围 ACTIVE_RADIUS 内的区块集合，缓存 ACTIVE_CACHE_TTL tick。
     * coreChunks 为玩家附近 CORE_RADIUS 的核心区块（优先处理）。
     */
    private void refreshActiveChunks(ServerLevel level, long gameTime) {
        if (gameTime - activeChunksRefreshedAt < ACTIVE_CACHE_TTL) {
            return;
        }
        activeChunksRefreshedAt = gameTime;
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            activeChunks = Set.of();
            coreChunks = Set.of();
            lastProcessedTicks.clear();
            return;
        }
        Set<String> refreshed = new HashSet<>();
        Set<String> core = new HashSet<>();
        for (ServerPlayer player : players) {
            ChunkPos center = ChunkPos.containing(player.blockPosition());
            for (int dx = -ACTIVE_RADIUS; dx <= ACTIVE_RADIUS; dx++) {
                for (int dz = -ACTIVE_RADIUS; dz <= ACTIVE_RADIUS; dz++) {
                    refreshed.add(keyOf(new ChunkPos(center.x() + dx, center.z() + dz)));
                }
            }
            for (int dx = -CORE_RADIUS; dx <= CORE_RADIUS; dx++) {
                for (int dz = -CORE_RADIUS; dz <= CORE_RADIUS; dz++) {
                    core.add(keyOf(new ChunkPos(center.x() + dx, center.z() + dz)));
                }
            }
        }
        activeChunks = refreshed;
        coreChunks = core;
        lastProcessedTicks.keySet().retainAll(refreshed);
    }

    private void processChunk(ServerLevel level, ChunkPos pos, ChunkQiData data, long elapsed) {
        double activity = data.activity();
        if (activity <= 0) {
            return; // 匮乏/全空：锁恢复（仅灵脉养护或外部注入可解除）
        }
        ensureProportions(level, pos, data);
        double rate = restoreRateOf(activity);
        double amount = data.effectiveCap() * rate * elapsed / 20.0
                * Math.pow(ChunkQiData.VEIN_RESTORE_MULTIPLIER, data.veinStage());
        data.distribute(amount);
        double sourceTotal = data.total();
        if (sourceTotal <= 0) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                ChunkPos neighbor = new ChunkPos(pos.x() + dx, pos.z() + dz);
                if (!level.isLoaded(neighbor.getMiddleBlockPosition(0))) {
                    continue;
                }
                ChunkQiData neighborData = chunks.get(keyOf(neighbor));
                // 扩散前置：目标不低于来源或已满则跳过
                if (neighborData == null || neighborData.total() >= sourceTotal
                        || neighborData.total() >= neighborData.effectiveCap()) {
                    continue;
                }
                diffuse(level, pos, data, neighbor, neighborData, elapsed);
            }
        }
    }

    /**
     * 区块恢复分段曲线：按总量占比（活性）确定每秒恢复速率。
     * 1-10% → 0.2%/s；10-50% → 0.35%/s；50-70% → 0.65%/s；70-100% → 0.9%/s。
     */
    public static double restoreRateOf(double activity) {
        if (activity < 0.10) {
            return 0.002;
        }
        if (activity < 0.50) {
            return 0.0035;
        }
        if (activity < 0.70) {
            return 0.0065;
        }
        return 0.009;
    }

    /**
     * 灵气扩散：来源 > 目标 且同群系时，按系数把差值的一部分从来源转到目标。
     */
    private void diffuse(ServerLevel level, ChunkPos sourcePos, ChunkQiData source,
                         ChunkPos targetPos, ChunkQiData target, long elapsed) {
        double sourceTotal = source.total();
        double targetTotal = target.total();
        if (sourceTotal <= 0 || sourceTotal <= targetTotal) {
            return;
        }
        if (isEnd(level, sourcePos) || isEnd(level, targetPos) || !sameBiome(level, sourcePos, targetPos)) {
            return;
        }
        double coefficient = source.veinStage() > 0 ? VEIN_DIFFUSE_COEFFICIENT : DIFFUSE_COEFFICIENT;
        double gap = sourceTotal - targetTotal;
        double diff = Math.min(gap * coefficient * elapsed, gap);
        if (diff <= 0) {
            return;
        }
        for (ElementType element : org.wenyan.wenyan_addon.qi.element.ElementRelations.ELEMENTS) {
            double share = source.get(element) * (diff / sourceTotal);
            if (share > 0) {
                source.consume(element, share);
            }
        }
        if (targetTotal <= 0) {
            ElementType preferred = preferredElement(level, targetPos);
            target.add(preferred, diff * 0.6);
            for (ElementType element : org.wenyan.wenyan_addon.qi.element.ElementRelations.ELEMENTS) {
                if (element != preferred) {
                    target.add(element, diff * 0.1);
                }
            }
        } else {
            for (ElementType element : org.wenyan.wenyan_addon.qi.element.ElementRelations.ELEMENTS) {
                target.add(element, diff * (target.get(element) / targetTotal));
            }
        }
    }

    private static boolean sameBiome(ServerLevel level, ChunkPos a, ChunkPos b) {
        Holder<Biome> biomeA = level.getBiome(a.getMiddleBlockPosition(0).atY(64));
        Holder<Biome> biomeB = level.getBiome(b.getMiddleBlockPosition(0).atY(64));
        return biomeA.is(biomeB);
    }

    private static boolean isEnd(ServerLevel level, ChunkPos pos) {
        return level.getBiome(pos.getMiddleBlockPosition(0).atY(64)).is(BiomeTags.IS_END);
    }

    public ElementType preferredElement(ServerLevel level, ChunkPos pos) {
        Holder<Biome> biome = level.getBiome(pos.getMiddleBlockPosition(0).atY(64));
        if (biome.is(BiomeTags.IS_NETHER)) {
            return ElementType.FIRE;
        }
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER) || biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            return ElementType.WATER;
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_TAIGA)) {
            return ElementType.WOOD;
        }
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(Biomes.DESERT)) {
            return ElementType.FIRE;
        }
        if (biome.is(BiomeTags.IS_MOUNTAIN)) {
            return ElementType.EARTH;
        }
        if (biome.is(Biomes.PLAINS) || biome.is(Biomes.SUNFLOWER_PLAINS) || biome.is(BiomeTags.IS_SAVANNA)) {
            return ElementType.EARTH;
        }
        if (biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.SNOWY_TAIGA) || biome.is(Biomes.ICE_SPIKES) || biome.is(Biomes.SNOWY_BEACH)) {
            return ElementType.WATER;
        }
        return ElementType.EARTH;
    }

    public double baseCap(ServerLevel level, ChunkPos pos) {
        Holder<Biome> biome = level.getBiome(pos.getMiddleBlockPosition(0).atY(64));
        if (biome.is(BiomeTags.IS_END)) {
            return 0;
        }
        if (biome.is(BiomeTags.IS_NETHER)) {
            return 20000;
        }
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER) || biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            return 15000;
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_TAIGA)) {
            return 12000;
        }
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(Biomes.DESERT)) {
            return 10000;
        }
        if (biome.is(BiomeTags.IS_MOUNTAIN)) {
            return 10000;
        }
        if (biome.is(Biomes.PLAINS) || biome.is(Biomes.SUNFLOWER_PLAINS) || biome.is(BiomeTags.IS_SAVANNA)) {
            return 6000;
        }
        if (biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.SNOWY_TAIGA) || biome.is(Biomes.ICE_SPIKES) || biome.is(Biomes.SNOWY_BEACH)) {
            return 4000;
        }
        return 8000;
    }

    /**
     * 灵脉养护：每 30 分钟等级 +1；达到 3 阶段后每个等级尝试向外延伸。
     */
    public void veinTick(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (gameTime - lastVeinTickTime < VEIN_TICK_INTERVAL) {
            return;
        }
        lastVeinTickTime = gameTime;
        for (Map.Entry<String, QiVein> entry : veins.entrySet()) {
            QiVein vein = entry.getValue();
            if (gameTime - vein.lastNurtureTime() < VEIN_NURTURE_INTERVAL) {
                continue;
            }
            QiVein nurtured = vein.nurtureUp(gameTime);
            veins.put(entry.getKey(), nurtured);
            nurture(level, nurtured);
            syncVeinStage(level, nurtured);
            if (nurtured.canExpand()) {
                expand(level, nurtured);
            }
            setDirty();
        }
    }

    private void spawnVein(ServerLevel level, ChunkPos core) {
        int coverage = VEIN_INITIAL_MIN_COVERAGE
                + level.getRandom().nextInt(VEIN_INITIAL_MAX_COVERAGE - VEIN_INITIAL_MIN_COVERAGE + 1);
        QiVein vein = new QiVein(1, new HashSet<>(), level.getGameTime());
        addToVein(level, vein, core);
        for (int i = 1; i < coverage && !vein.covered().isEmpty(); i++) {
            expand(level, vein);
        }
        veins.put(keyOf(core), vein);
        syncVeinStage(level, vein);
        setDirty();
    }

    private void expand(ServerLevel level, QiVein vein) {
        List<ChunkPos> candidates = new ArrayList<>();
        for (String key : vein.covered()) {
            ChunkPos pos = posOf(key);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    ChunkPos neighbor = new ChunkPos(pos.x() + dx, pos.z() + dz);
                    if (!vein.covered().contains(keyOf(neighbor)) && sameBiome(level, pos, neighbor)) {
                        candidates.add(neighbor);
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        ChunkPos chosen = candidates.get(level.getRandom().nextInt(candidates.size()));
        addToVein(level, vein, chosen);
    }

    private void addToVein(ServerLevel level, QiVein vein, ChunkPos pos) {
        vein.covered().add(keyOf(pos));
        ChunkQiData data = chunks.get(keyOf(pos));
        if (data == null) {
            data = createChunkQi(level, pos);
            chunks.put(keyOf(pos), data);
        }
        data.setVeinStage(vein.stage());
        setDirty();
    }

    private void nurture(ServerLevel level, QiVein vein) {
        for (String key : vein.covered()) {
            ChunkQiData data = chunks.get(key);
            if (data == null) {
                continue;
            }
            ensureProportions(level, posOf(key), data);
            double amount = data.effectiveCap() * VEIN_NURTURE_RATIO;
            data.distribute(amount);
        }
        setDirty();
    }

    private void syncVeinStage(ServerLevel level, QiVein vein) {
        for (String key : vein.covered()) {
            ChunkQiData data = chunks.get(key);
            if (data != null) {
                data.setVeinStage(vein.stage());
            }
        }
        setDirty();
    }

    public boolean hasVeinAt(ChunkPos pos) {
        return veins.values().stream().anyMatch(vein -> vein.covered().contains(keyOf(pos)));
    }

    public int veinStageAt(ChunkPos pos) {
        return veins.values().stream()
                .filter(vein -> vein.covered().contains(keyOf(pos)))
                .map(QiVein::stage)
                .findFirst()
                .orElse(0);
    }

    private static String keyOf(ChunkPos pos) {
        return pos.x() + "," + pos.z();
    }

    private static ChunkPos posOf(String key) {
        String[] parts = key.split(",");
        return new ChunkPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
