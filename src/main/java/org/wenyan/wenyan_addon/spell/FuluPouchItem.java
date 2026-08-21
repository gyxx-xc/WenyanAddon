package org.wenyan.wenyan_addon.spell;

import indi.wenyan.content.item.ItemCodeHolder;
import indi.wenyan.content.block.IWenyanDevice;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.wenyan.wenyan_addon.WenyanAddon;

/**
 * 符咒包：存放符咒（承载代码的物品）的容器。
 * 右键打开容器菜单；shift+右键切换当前符咒（activeSlot）。
 * 运行时扫描不会扫描符咒包（计划规定，仅拓展包可被扫描）。
 */
public class FuluPouchItem extends Item {
    public FuluPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.isShiftKeyDown()) {
                cycleActiveSlot(serverPlayer, stack);
                return InteractionResult.SUCCESS;
            }
            FuluPouchComponent component = stack.getOrDefault(SpellDataComponent.POUCH_DATA.get(), FuluPouchComponent.EMPTY);
            MenuProvider provider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                    return new FuluPouchMenu(containerId, inventory, stack);
                }

                @Override
                public void writeClientSideData(AbstractContainerMenu menu, net.minecraft.network.RegistryFriendlyByteBuf buffer) {
                    buffer.writeBoolean(stack.getItem() instanceof FuluPouchExtensionItem);
                    FuluPouchComponent.STREAM_CODEC.encode(buffer, component);
                }
            };
            serverPlayer.openMenu(provider);
        }
        return InteractionResult.SUCCESS;
    }

    private void cycleActiveSlot(ServerPlayer player, ItemStack stack) {
        FuluPouchComponent component = stack.getOrDefault(SpellDataComponent.POUCH_DATA.get(), FuluPouchComponent.EMPTY);
        int start = component.activeSlot();
        int next = start;
        for (int i = 1; i <= FuluPouchMenu.SLOT_COUNT; i++) {
            int candidate = (start + i) % FuluPouchMenu.SLOT_COUNT;
            if (!component.createItems().get(candidate).isEmpty()) {
                next = candidate;
                break;
            }
        }
        stack.set(SpellDataComponent.POUCH_DATA.get(), component.withActiveSlot(next));
    }

    /**
     * 判断物品能否存入包内：
     * - 符咒包：仅代码载体（符咒），且排除拓展包
     * - 符咒拓展包：代码载体或文言设备（符咒石）
     */
    public static boolean canStore(boolean extension, ItemStack stack) {
        if (extension) {
            return SpellCodeHelper.isCodeCarrier(stack) || isWenyanDevice(stack);
        }
        return SpellCodeHelper.isCodeCarrier(stack) && !(stack.getItem() instanceof FuluPouchExtensionItem);
    }

    private static boolean isWenyanDevice(ItemStack stack) {
        return stack.getCapability(WyRegistration.WENYAN_ITEM_DEVICE_CAPABILITY) instanceof IWenyanDevice;
    }

    /**
     * 符咒/符咒石在运行环境中的包名：平台名（自定义名称或方括号物品名）。
     */
    public static String packageNameOf(ItemStack stack) {
        return ItemCodeHolder.getCodeCapability(stack).getPlatformName();
    }
}