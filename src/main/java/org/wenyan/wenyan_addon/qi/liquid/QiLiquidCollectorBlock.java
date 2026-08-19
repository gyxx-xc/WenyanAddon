package org.wenyan.wenyan_addon.qi.liquid;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementType;

/**
 * 灵液收集方块：自动从区块收集灵气；空瓶右键接取灵液。
 */
public class QiLiquidCollectorBlock extends Block implements EntityBlock {
    public QiLiquidCollectorBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_CYAN)
                .strength(2.0f)
                .sound(SoundType.GLASS));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new QiLiquidCollectorBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof QiLiquidCollectorBlockEntity collector)) {
            return InteractionResult.PASS;
        }
        // 空瓶且足量：接取灵液（按当前收集属性）
        if (stack.is(Items.GLASS_BOTTLE) && collector.collectElement() != null && collector.hasEnoughLiquid()) {
            collector.takeBottle();
            stack.shrink(1);
            ItemStack bottle = QiLiquidNbt.liquidBottle(collector.collectElement(), QiLiquidCollectorBlockEntity.BOTTLE_AMOUNT);
            if (!player.addItem(bottle)) {
                Block.popResource(level, pos, bottle);
            }
            WenyanAddon.LOGGER.info(String.valueOf(Component.literal(
                    "已接取" + collector.collectElement().displayName() + "灵液一瓶，剩余液量 "
                            + String.format(java.util.Locale.ROOT, "%.0f", collector.liquid()) + "/"
                            + (long) QiLiquidCollectorBlockEntity.MAX_LIQUID)));
            return InteractionResult.SUCCESS;
        }
        // 手持其他物品/空瓶不足：显示采集状态
        showStatus(player, collector);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof QiLiquidCollectorBlockEntity collector) {
            showStatus(player, collector);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * 向玩家发送收集器当前采集状态（属性、液量、每瓶需求）。
     */
    private static void showStatus(Player player, QiLiquidCollectorBlockEntity collector) {
        ElementType element = collector.collectElement();
        if (element == null) {
            WenyanAddon.LOGGER.info(String.valueOf(Component.literal("灵液收集器尚未采集到灵气")));
            return;
        }
        WenyanAddon.LOGGER.info(String.valueOf(Component.literal(
                "正在收集" + element.displayName() + "灵气，液量 "
                        + String.format(java.util.Locale.ROOT, "%.0f", collector.liquid()) + "/"
                        + (long) QiLiquidCollectorBlockEntity.MAX_LIQUID
                        + "（每瓶需 " + (long) QiLiquidCollectorBlockEntity.BOTTLE_AMOUNT + "）")));
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state,
                                                                             @NonNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (entityLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof QiLiquidCollectorBlockEntity collector) {
                collector.tick((ServerLevel) entityLevel);
            }
        };
    }
}
