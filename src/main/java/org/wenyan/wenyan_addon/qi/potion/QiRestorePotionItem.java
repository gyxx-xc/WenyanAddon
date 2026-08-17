package org.wenyan.wenyan_addon.qi.potion;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

/**
 * 灵气恢复药水：饮用恢复指定属性的灵气。
 * NBT：attribute（属性 id）+ amount（恢复量）+ 缓释型（sustained：通过药水效果缓慢恢复）。
 */
public class QiRestorePotionItem extends Item {
    public static final String NBT_KEY = "WenyanRestorePotion";

    public QiRestorePotionItem(Properties properties) {
        super(properties);
    }

    public static void configure(ItemStack stack, ElementAttribute attribute, double amount, boolean sustained) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, outer -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("attribute", attribute.id());
            tag.putDouble("amount", amount);
            tag.putBoolean("sustained", sustained);
            outer.put(NBT_KEY, tag);
        });
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel && entity instanceof ServerPlayer player) {
            CompoundTag tag = data(stack);
            ElementAttribute attribute = ElementRegistry.byId(tag.getString("attribute").orElse(""));
            double amount = tag.getDoubleOr("amount", 0.0);
            boolean sustained = tag.getBooleanOr("sustained", false);
            if (attribute != null && amount > 0) {
                PlayerQiData qi = PlayerQi.of(player);
                if (sustained) {
                    // 缓释型：添加药水效果，按 tick 缓慢恢复（每 tick 恢复上限 5%，持续 30 秒）
                    Holder<MobEffect> effect = QiRestorePotionEffects.holderOf(attribute);
                    if (effect != null) {
                        player.addEffect(new MobEffectInstance(effect, 600, 0));
                    }
                } else {
                    qi.add(attribute, amount);
                }
                PlayerQi.markDirty(player);
            }
        }
        stack.shrink(1);
        return stack;
    }

    private static CompoundTag data(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag outer = data.copyTag();
            if (outer.contains(NBT_KEY)) {
                return outer.getCompoundOrEmpty(NBT_KEY);
            }
        }
        return new CompoundTag();
    }
}
