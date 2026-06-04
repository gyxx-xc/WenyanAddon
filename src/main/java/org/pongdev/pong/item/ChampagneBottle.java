package org.pongdev.pong.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.pongdev.pong.block.ChampagneRack;
import org.pongdev.pong.block.RackEntity;
import org.pongdev.pong.setup.PongRegistration;

import java.util.List;

public class ChampagneBottle extends BlockItem {
    public static final String ID = "champagne_bottle";
    public static final String POWER_TAG = "power";
    public static final String OPEN_TAG = "open";
    private static final String X0_TAG = "X0";
    private static final String Y0_TAG = "Y0";
    private static final String Z0_TAG = "Z0";
    public static final String CAPABILITY_TAG = "champagne_capability";

    public ChampagneBottle(Properties properties) {
        super(PongRegistration.CHAMPAGNE_BOTTLE_BLOCK.get(), properties);
    }

    public static void syncModelData(ItemStack stack) {
        int stage;
        if (PongStackData.getBoolean(stack, OPEN_TAG)) {
            stage = 6;
        } else {
            stage = Math.min(5, Math.max(0, (int) (PongStackData.getDouble(stack, POWER_TAG) / 10)));
        }
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float) stage), List.of(), List.of(), List.of()));
    }

    @Override
    public void inventoryTick(ItemStack pStack, ServerLevel pLevel, Entity pEntity, EquipmentSlot pSlot) {
        if(PongStackData.getBoolean(pStack, OPEN_TAG)) return;
        if (!(pEntity instanceof Player player)) return;

        boolean pIsSelected = pSlot == EquipmentSlot.MAINHAND || pSlot == EquipmentSlot.OFFHAND;
        if (pIsSelected) {
            CompoundTag compoundTag1 = pEntity.getPersistentData();
            Vec3 view = pEntity.getViewVector(1.0f);
            Vec3 view0 = new Vec3(compoundTag1.getDouble(X0_TAG).orElse(0.0),
                    compoundTag1.getDouble(Y0_TAG).orElse(0.0),
                    compoundTag1.getDouble(Z0_TAG).orElse(0.0));
            double dot = Math.max(-1.0, Math.min(1.0, view.dot(view0)));
            double d = (Math.acos(dot) * 5);
            d = 0.1 * Math.pow(d, 2) - 0.1;
            compoundTag1.putDouble(X0_TAG, view.x);
            compoundTag1.putDouble(Y0_TAG, view.y);
            compoundTag1.putDouble(Z0_TAG, view.z);
            double power = PongStackData.getDouble(pStack, POWER_TAG);
            if (power > 0 || d > 0) {
                PongStackData.putDouble(pStack, POWER_TAG, Math.max(0, power + d));
                syncModelData(pStack);
            }
            if (PongStackData.getDouble(pStack, POWER_TAG) >= 50 && !PongStackData.getBoolean(pStack, OPEN_TAG)){
                if(pStack.getCount() > 1) {
                    for (int i = 0; i < pStack.getCount() - 1; i ++){
                        ItemStack itemStack = new ItemStack(pStack.getItem(), 1);
                        PongStackData.copyTo(pStack, itemStack);
                        OpenChampagne.open(itemStack, pEntity, pLevel);
                        if (!player.getInventory().add(itemStack))
                            player.drop(itemStack, false);
                    }
                }
                pStack.setCount(1);
                OpenChampagne.open(pStack, pEntity, pLevel);
            }
        } else {
            double power = PongStackData.getDouble(pStack, POWER_TAG);
            if(power > 0) {
                PongStackData.putDouble(pStack, POWER_TAG, Math.max(0, power - 0.4));
                syncModelData(pStack);
            }
        }
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        if (!PongStackData.getBoolean(pContext.getItemInHand(), OPEN_TAG)) {
            Level level = pContext.getLevel();
            BlockPos pos = pContext.getClickedPos();
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RackEntity rack) {
                int temp = rack.getPersistentData().getInt(ChampagneRack.CONTAIN).orElse(0);
                if (temp < 4) {
                    rack.getPersistentData().putInt(ChampagneRack.CONTAIN, temp + 1);
                    pContext.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.FAIL;
                }
            }
        }
        return super.useOn(pContext);
    }
}
