package org.wenyan.wenyan_addon.qi.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.async.ChunkQiSnapshot;
import org.wenyan.wenyan_addon.qi.async.QiAsyncExecutor;
import org.wenyan.wenyan_addon.qi.async.QiRestoreCalculator;
import org.wenyan.wenyan_addon.qi.async.QiSnapshot;
import org.wenyan.wenyan_addon.qi.async.RestoreResult;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiData;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = WenyanAddon.MODID)
public final class QiRestoreHandler {
    private static final int SLOW_INTERVAL = 20;
    private static final int VEIN_INTERVAL = 40;
    private static long tickCounter = 0;

    private QiRestoreHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        tickCounter++;
        boolean slowTick = tickCounter % SLOW_INTERVAL == 0;
        boolean veinTick = tickCounter % VEIN_INTERVAL == 0;

        // ===== 世界级操作（每个维度一次） =====
        for (ServerLevel level : server.getAllLevels()) {
            ChunkQiManager manager = ChunkQiManager.of(level);
            if (slowTick) {
                manager.tick(level);
            }
            if (veinTick) {
                manager.veinTick(level);
            }
        }

        // ===== 玩家级操作 =====
        if (!slowTick) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            submitAsyncRestore(server, player);
        }
    }

    /**
     * 提交异步恢复：主线程创建快照 → 异步纯计算 → 回调主线程应用（版本检查 + 降级）。
     */
    private static void submitAsyncRestore(MinecraftServer server, ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        ChunkQiManager manager = ChunkQiManager.of(level);
        ChunkQiData chunkQi = manager.getChunkQi(level, ChunkPos.containing(player.blockPosition()));
        PlayerQiData qi = PlayerQi.of(player);
        QiSnapshot playerSnapshot = QiSnapshot.of(qi, tickCounter);
        ChunkQiSnapshot chunkSnapshot = ChunkQiSnapshot.of(chunkQi);
        Set<ElementAttribute> sources = collectRestoreElements(player);
        sources.add(ElementType.NEUTRAL);
        for (ElementAttribute element : ElementRegistry.all()) {
            if (element == ElementType.YIN || element == ElementType.YANG) {
                continue;
            }
            if (qi.cap(element) > 0) {
                sources.add(element);
            }
        }
        double perTick = 1.0 / 20.0;
        // 异步恢复每 SLOW_INTERVAL(20) tick 结算一次，单次应用完整每秒量，保持原速率
        double applyAmount = perTick * SLOW_INTERVAL;

        QiAsyncExecutor.submit(
                () -> QiRestoreCalculator.calculate(playerSnapshot, chunkSnapshot, sources, applyAmount),
                result -> applyResult(server, player, qi, result),
                () -> QiRestoreCalculator.calculate(playerSnapshot, chunkSnapshot, sources, applyAmount));
    }

    /**
     * 回调（主线程）：版本检查通过则应用结果。
     */
    private static void applyResult(MinecraftServer server, ServerPlayer player,
                                    PlayerQiData qi, RestoreResult result) {
        server.execute(() -> {
            if (player.isRemoved() || !player.isAlive()) {
                return;
            }
            if (qi.version() != result.sourceVersion()) {
                return; // 版本不匹配：丢弃旧结果
            }
            if (result.leakScale() < 1.0) {
                qi.scaleQi(result.leakScale()); // 匮乏：按比例漏气
            } else {
                for (var entry : result.gains().entrySet()) {
                    qi.add(entry.getKey(), entry.getValue());
                }
            }
            PlayerQi.markDirty(player);
        });
    }

    private static Set<ElementAttribute> collectRestoreElements(ServerPlayer player) {
        Set<ElementAttribute> result = new HashSet<>();
        PlayerEquipment.forEachItem(player, stack -> addRestoreElements(stack, result));
        return result;
    }

    private static void addRestoreElements(ItemStack stack, Set<ElementAttribute> result) {
        if (stack.getItem() instanceof QiRestoreSource source) {
            result.addAll(source.restoreElements());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        QiAsyncExecutor.shutdown();
    }
}
