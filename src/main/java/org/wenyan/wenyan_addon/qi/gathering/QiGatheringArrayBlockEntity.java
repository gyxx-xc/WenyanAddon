package org.wenyan.wenyan_addon.qi.gathering;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.async.QiAsyncExecutor;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiData;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.gathering.QiGatheringCalculator.GatherResult;
import org.wenyan.wenyan_addon.qi.gathering.QiGatheringCalculator.GatherSnapshot;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

import java.util.Map;

/**
 * 聚灵阵核心方块实体：抽取当前区块主属性灵气，填充范围内玩家的对应属性灵气条。
 * tick 只负责：主线程读快照 → 提交异步计算 → 回调回主线程应用结果（consume + add）。
 */
public class QiGatheringArrayBlockEntity extends BlockEntity {
    private static final int INTERVAL = 20;
    private static final int RANGE = 5;

    private int tick = 0;

    public QiGatheringArrayBlockEntity(BlockPos pos, BlockState blockState) {
        super(WenyanAddon.QI_GATHERING_ARRAY_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tick(ServerLevel level) {
        if (++tick < INTERVAL) {
            return;
        }
        tick = 0;
        BlockPos center = getBlockPos();
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distToCenterSqr(center.getX(), center.getY(), center.getZ()) <= RANGE * RANGE) {
                submitGather(level, player);
            }
        }
    }

    /**
     * 提交异步抽取：主线程只读快照（区块储量、玩家上限）→ 异步线程纯计算 → 主线程应用结果。
     */
    private void submitGather(ServerLevel level, ServerPlayer player) {
        ChunkQiManager manager = ChunkQiManager.of(level);
        ChunkPos chunkPos = ChunkPos.containing(getBlockPos());
        ChunkQiData chunk = manager.getChunkQi(level, chunkPos);
        ElementType dominant = manager.preferredElement(level, chunkPos);

        PlayerQiData qi = PlayerQi.of(player);
        GatherSnapshot snapshot = new GatherSnapshot(dominant, chunk.get(dominant),
                qi.totalCap(), Map.copyOf(qi.capMap()), Map.copyOf(qi.reserves()));

        QiAsyncExecutor.submit(
                () -> QiGatheringCalculator.calculate(snapshot),
                result -> level.getServer().execute(() -> apply(manager, chunk, player, qi, result)),
                () -> QiGatheringCalculator.calculate(snapshot));
    }

    /**
     * 主线程应用结果：区块扣除 → 玩家增加（不递增版本号，避免干扰异步恢复）→ 标记保存。
     */
    private void apply(ChunkQiManager manager, ChunkQiData chunk,
                       ServerPlayer player, PlayerQiData qi, GatherResult result) {
        if (player.isRemoved() || !player.isAlive() || result.extracted() <= 0) {
            return;
        }
        if (chunk.consume(result.dominant(), result.extracted())) {
            manager.setDirty();
            qi.addExternal(result.target(), result.amount());
            PlayerQi.markDirty(player);
        }
    }
}
