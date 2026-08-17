package org.wenyan.wenyan_addon.qi.liquid;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiData;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;
import org.wenyan.wenyan_addon.qi.element.ElementType;

/**
 * 灵液收集方块实体：按放入的五行矿石确定收集属性，
 * 每 tick 从所在区块提取该属性灵气（10 点/秒）转为灵液储存。
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

    public ElementType collectElement() {
        return collectElement;
    }

    public void setCollectElement(ElementType element) {
        this.collectElement = element;
        setChanged();
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
        if (collectElement == null) {
            return;
        }
        if (liquid >= MAX_LIQUID) {
            return;
        }
        if (++tick < 20) {
            return;
        }
        tick = 0;
        ChunkQiManager manager = ChunkQiManager.of(level);
        ChunkQiData chunk = manager.getChunkQi(level, net.minecraft.world.level.ChunkPos.containing(getBlockPos()));
        double available = chunk.get(collectElement);
        if (available <= 0) {
            return;
        }
        double collect = Math.min(COLLECT_RATE, available);
        chunk.consume(collectElement, collect);
        liquid = Math.min(MAX_LIQUID, liquid + collect);
        manager.setDirty();
        setChanged();
    }
}
