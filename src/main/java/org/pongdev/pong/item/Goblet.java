package org.pongdev.pong.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.pongdev.pong.setup.PongRegistration;

public class Goblet extends Item {
    public static final String ID = "goblet";
    public static final String CONTAIN_TAG = "contain";

    public Goblet(Properties properties) {
        super(properties.food(
                new FoodProperties.Builder()
                        .alwaysEdible()
                        .build()
        ));
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand thisHand) {
        ItemStack thisItem = pPlayer.getItemInHand(thisHand);
        if (!PongStackData.getString(thisItem, CONTAIN_TAG).equals("")) { // not empty
            pPlayer.startUsingItem(thisHand);
            return InteractionResult.CONSUME;
        }
        InteractionHand otherHand = thisHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherItem = pPlayer.getItemInHand(otherHand);
        if(otherItem.getItem() instanceof ChampagneBottle){
            int remainChampagne = PongStackData.getInt(otherItem, ChampagneBottle.CAPABILITY_TAG);
            if (remainChampagne >= 100) {
                PongStackData.putBoolean(thisItem, "hand", thisHand == InteractionHand.MAIN_HAND);
                pPlayer.startUsingItem(thisHand);
                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.FAIL;
            }
        } // TODO: add the bucket can fill the goblet too
        return InteractionResult.FAIL;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity user) {
        return 15;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        if (!PongStackData.getString(pStack, CONTAIN_TAG).equals("")) // not empty
            return ItemUseAnimation.DRINK;
        else
            return ItemUseAnimation.NONE; //TODO: get this right
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!PongStackData.getString(pStack, CONTAIN_TAG).equals("")) {
            PongStackData.putString(pStack, CONTAIN_TAG, "");
            // we may change this in the future
            // but for now, the containing can only be the champagne
            if (!pLevel.isClientSide()) {
                int level = 0;
                MobEffectInstance drunk = pLivingEntity.getEffect(PongRegistration.DRUNK);
                if (drunk != null)
                    level = drunk.getAmplifier();
                pLivingEntity.addEffect(new MobEffectInstance(PongRegistration.DRUNK, 3000, level+1));
                if (level <= 3) {
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, 500, level));
                } else if (level <= 5) {
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, 500, 4));
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.HASTE, 500, 1));
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 500, (level - 3)));
                } else if (level <= 10) {
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, 500, 4));
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 500, level - 5));
                } else if (level <= 20) {
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, (level-10)*100+100, 100));
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 500, level - 10));
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 500, 10));
                } else if (pLevel instanceof ServerLevel serverLevel) {
                    pLivingEntity.kill(serverLevel);
                }
            }
        } else {
            ItemStack newItemStack = new ItemStack(pStack.getItem(), pStack.getCount()-1);
            PongStackData.copyTo(pStack, newItemStack);
            PongStackData.putString(pStack, CONTAIN_TAG, "champagne");

            if (pStack.getCount() >= 1) {
                Player player = (Player) pLivingEntity;
                if (!player.getInventory().add(newItemStack))
                    player.drop(newItemStack, false);
            }
            pStack.setCount(1);

            ItemStack otherItem = pLivingEntity.getItemInHand(
                    PongStackData.getBoolean(pStack, "hand") ?
                            InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            int remainChampagne = PongStackData.getInt(otherItem, ChampagneBottle.CAPABILITY_TAG);
            PongStackData.putInt(otherItem, ChampagneBottle.CAPABILITY_TAG, remainChampagne-100);
        }
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }
}
