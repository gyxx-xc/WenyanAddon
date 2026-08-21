package org.wenyan.wenyan_addon.spell;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.level.Level;

/**
 * 法术剑：可写入（合成）并施放符咒法术的剑类物品。
 * 手持右键读取剑上法术代码(- {@link SpellDataComponent#SPELL_CODE})，
 * 异步编译并运行法器环境（扫描玩家背包符咒拓展包 + 周围设备方块）。
 */
public class SpellSwordItem extends MaceItem {
    public SpellSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND) {
            ItemStack stack = player.getItemInHand(hand);
            SpellRunManager.getInstance().tryCast(serverPlayer, stack);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }
}