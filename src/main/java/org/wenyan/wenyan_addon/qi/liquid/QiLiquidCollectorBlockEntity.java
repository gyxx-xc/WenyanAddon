package org.wenyan.wenyan_addon.qi.liquid;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiData;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;

/**
 * 灵液收集方块实体：自动从所在区块收集灵气（优先收集储量最大的五行属性），
 * 每 tick 提取 10 点/秒转为灵液储存。
 */
public class QiLiquidCollectorBlockEntity extends BlockEntity {
    public static final double COLLECT_RATE = 10.0;       // 10 点/秒
    public static final double MAX_LIQUID = 1000.0;
    public static final double BOTTLE_AMOUNT = 100.0;     // 每瓶 100 灵液

    private ElementType collectElement = null;
    private double liquid = 0;
    private int tick = 0;

    public QiLiquidCollectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(WenyanAddon.QI_LIQUID_COLLECTOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    /**
     * 当前正在收集的属性（随区块最大含量属性变化）。
     */
    public ElementType collectElement() {
        return collectElement;
    }

    public double liquid() {
        return liquid;
    }

    public boolean hasEnoughLiquid() {
        return liquid >= BOTTLE_AMOUNT;
    }

    public void takeBottle() {
        liquid = Math.max(0, liquid - BOTTLE_AMOUNT);
        setChanged();
    }

    public void tick(ServerLevel level) {
        if (liquid >= MAX_LIQUID) {
            return;
        }
        if (++tick < 20) {
            return;
        }
        tick = 0;
        ChunkQiManager manager = ChunkQiManager.of(level);
        ChunkQiData chunk = manager.getChunkQi(level, net.minecraft.world.level.ChunkPos.containing(getBlockPos()));
        // 优先收集区块储量最大的五行属性
        ElementType dominant = dominantElement(chunk);
        if (dominant == null) {
            return;
        }
        double available = chunk.get(dominant);
        if (available <= 0) {
            return;
        }
        double collect = Math.min(COLLECT_RATE, available);
        chunk.consume(dominant, collect);
        liquid = Math.min(MAX_LIQUID, liquid + collect);
        collectElement = dominant;
        manager.setDirty();
        setChanged();
    }

    private static ElementType dominantElement(ChunkQiData chunk) {
        ElementType dominant = null;
        double max = 0;
        for (ElementType element : ElementRelations.ELEMENTS) {
            double value = chunk.get(element);
            if (value > max) {
                max = value;
                dominant = element;
            }
        }
        return dominant;
    }
}
