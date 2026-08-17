package org.wenyan.wenyan_addon.qi.liquid;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.potion.QiRestorePotionItem;

/**
 * 灵液纯化方块实体：放入灵液水瓶后计时处理，产出对应属性的灵气恢复药水。
 * 档位按灵液量：≥500 大瓶、≥200 中瓶、否则小瓶。
 */
public class QiLiquidPurifierBlockEntity extends BlockEntity {
    private static final int PROCESS_TICKS = 40; // 2 秒

    private ElementAttribute pendingAttribute = null;
    private double pendingAmount = 0;
    private int progress = 0;

    public QiLiquidPurifierBlockEntity(BlockPos pos, BlockState blockState) {
        super(WenyanAddon.QI_LIQUID_PURIFIER_BLOCK_ENTITY.get(), pos, blockState);
    }

    public boolean isProcessing() {
        return pendingAttribute != null;
    }

    public ElementAttribute pendingAttribute() {
        return pendingAttribute;
    }

    /**
     * 放入灵液水瓶开始纯化。
     */
    public boolean start(net.minecraft.world.item.ItemStack liquidBottle) {
        if (isProcessing()) {
            return false;
        }
        ElementAttribute attribute = QiLiquidNbt.liquidAttribute(liquidBottle);
        double amount = QiLiquidNbt.liquidAmount(liquidBottle);
        if (attribute == null || amount <= 0) {
            return false;
        }
        pendingAttribute = attribute;
        pendingAmount = amount;
        progress = 0;
        setChanged();
        return true;
    }

    public void tick(ServerLevel level) {
        if (!isProcessing()) {
            return;
        }
        if (++progress < PROCESS_TICKS) {
            return;
        }
        // 完成：产出恢复药水（档位按灵液量）
        var stack = org.wenyan.wenyan_addon.WenyanAddon.QI_RESTORE_POTION_MEDIUM.get().getDefaultInstance();
        if (pendingAmount >= 500) {
            stack = org.wenyan.wenyan_addon.WenyanAddon.QI_RESTORE_POTION_LARGE.get().getDefaultInstance();
        } else if (pendingAmount < 200) {
            stack = org.wenyan.wenyan_addon.WenyanAddon.QI_RESTORE_POTION_SMALL.get().getDefaultInstance();
        }
        double restoreAmount = pendingAmount >= 500 ? 100 : pendingAmount >= 200 ? 50 : 20;
        QiRestorePotionItem.configure(stack, pendingAttribute, restoreAmount, false);
        Block.popResource(level, getBlockPos().above(), stack);
        pendingAttribute = null;
        pendingAmount = 0;
        progress = 0;
        setChanged();
    }
}
