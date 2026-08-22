package org.wenyan.wenyan_addon.spell;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.spell.network.FuluPouchClearPayload;
import org.wenyan.wenyan_addon.spell.network.FuluPouchSwitchPayload;

/**
 * 客户端：符咒包交互。
 * - shift+滚轮：切换选中符咒
 * - shift+左键：清空选中（恢复自身材质）
 * 仅符咒包（FuluPouchItem）响应，拓展包不可切换。
 */
@EventBusSubscriber(modid = WenyanAddon.MODID, value = Dist.CLIENT)
public final class FuluPouchClientHandler {
    private FuluPouchClientHandler() {
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        // 使用游戏内 shift 键映射（与玩家改键一致）
        if (!mc.options.keyShift.isDown()) {
            return;
        }
        ItemStack main = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        // 仅符咒包可切换（拓展包不可切换）
        if (!(main.getItem() instanceof FuluPouchItem) || main.getItem() instanceof FuluPouchExtensionItem) {
            if (!(off.getItem() instanceof FuluPouchItem) || off.getItem() instanceof FuluPouchExtensionItem) {
                return;
            }
        }
        double deltaY = event.getScrollDeltaY();
        if (deltaY == 0) {
            return;
        }
        event.setCanceled(true);
        ClientPacketDistributor.sendToServer(new FuluPouchSwitchPayload(deltaY > 0 ? 1 : -1));
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        if (event.getAction() != InputConstants.PRESS || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }
        if ((event.getModifiers() & InputConstants.MOD_SHIFT) == 0) {
            return;
        }
        ItemStack main = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        // 仅符咒包可清空选中（拓展包不可选中）
        if (!(main.getItem() instanceof FuluPouchItem) || main.getItem() instanceof FuluPouchExtensionItem) {
            if (!(off.getItem() instanceof FuluPouchItem) || off.getItem() instanceof FuluPouchExtensionItem) {
                return;
            }
        }
        event.setCanceled(true);
        ClientPacketDistributor.sendToServer(new FuluPouchClearPayload());
    }
}