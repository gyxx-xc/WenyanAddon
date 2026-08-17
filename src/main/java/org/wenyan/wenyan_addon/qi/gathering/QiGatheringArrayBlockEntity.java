package org.wenyan.wenyan_addon.qi.gathering;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiData;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

/**
 * 聚灵阵核心方块实体：抽取当前区块主属性灵气，填充范围内玩家的对应属性灵气条。
 * 抽取量 = 玩家灵气条上限的 5%；恢复效率：对应属性 100%、无属性 80%、其它属性 50%。
 */
public class QiGatheringArrayBlockEntity extends BlockEntity {
    private static final int INTERVAL = 20;
    private static final int RANGE = 3;
    private static final double DRAIN_RATIO = 0.05;

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
                gather(level, player);
            }
        }
    }

    private void gather(ServerLevel level, ServerPlayer player) {
        ChunkQiManager manager = ChunkQiManager.of(level);
        net.minecraft.world.level.ChunkPos chunkPos = net.minecraft.world.level.ChunkPos.containing(getBlockPos());
        ChunkQiData chunk = manager.getChunkQi(level, chunkPos);
        ElementType dominant = manager.preferredElement(level, chunkPos);

        PlayerQiData qi = PlayerQi.of(player);
        double drain = qi.totalCap() * DRAIN_RATIO;
        double available = chunk.get(dominant);
        if (available <= 0 || drain <= 0) {
            return;
        }
        double extracted = Math.min(drain, available);
        chunk.consume(dominant, extracted);
        manager.setDirty();

        // 恢复目标：对应属性 100% → 无属性 80% → 其它已解锁属性 50%
        if (qi.cap(dominant) > 0) {
            qi.add(dominant, extracted);
        } else if (qi.cap(ElementType.NEUTRAL) > 0) {
            qi.add(ElementType.NEUTRAL, extracted * 0.8);
        } else {
            for (ElementAttribute attribute : ElementRegistry.all()) {
                if (attribute == ElementType.YIN || attribute == ElementType.YANG
                        || attribute == ElementType.NEUTRAL) {
                    continue;
                }
                if (qi.cap(attribute) > 0) {
                    qi.add(attribute, extracted * 0.5);
                    break;
                }
            }
        }
        PlayerQi.markDirty(player);
    }
}
