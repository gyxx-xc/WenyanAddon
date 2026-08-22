package org.wenyan.wenyan_addon.spell;

import indi.wenyan.content.block.IWenyanDevice;
import indi.wenyan.content.item.FloatNoteItem;
import indi.wenyan.content.item.ItemCodeHolder;
import indi.wenyan.content.item.throw_runner.ThrowRunnerItem;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 符咒拓展包：存放符咒与符咒石（文言设备）的容器。
 * 与符咒包（FuluPouchItem）行为完全分离：
 * - 仅右键打开容器菜单，不可切换选中、不可使用符咒、不切换材质
 * - 运行时可被扫描（SpellEnvironmentScanner），其内部符咒/符咒石提供环境函数
 * - 符咒包中的符咒不可被扫描，仅本拓展包可被扫描
 */
@ParametersAreNonnullByDefault
public class FuluPouchExtensionItem extends Item {
    public FuluPouchExtensionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        openMenu(level, player, player.getItemInHand(hand));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null) {
            openMenu(context.getLevel(), context.getPlayer(), context.getItemInHand());
        }
        return InteractionResult.SUCCESS;
    }

    private void openMenu(Level level, Player player, ItemStack stack) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
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
                buffer.writeBoolean(true);
                FuluPouchComponent.STREAM_CODEC.encode(buffer, component);
            }
        };
        serverPlayer.openMenu(provider);
    }

    /**
     * 判断物品能否存入拓展包：符咒/符咒石（文言设备）/投符/浮签/纸类。
     */
    public static boolean canStore(ItemStack stack) {
        if (SpellCodeHelper.isCodeCarrier(stack)) {
            return true;
        }
        if (stack.getCapability(WyRegistration.WENYAN_ITEM_DEVICE_CAPABILITY) instanceof IWenyanDevice) {
            return true;
        }
        Item item = stack.getItem();
        if (item instanceof ThrowRunnerItem || item instanceof FloatNoteItem) {
            return true;
        }
        String id = item.builtInRegistryHolder().key().identifier().toString();
        return id.startsWith("wenyan_programming:") && id.endsWith("_paper");
    }

    /**
     * 符咒/符咒石在运行环境中的包名：平台名（自定义名称或方括号物品名）。
     */
    public static String packageNameOf(ItemStack stack) {
        return ItemCodeHolder.getCodeCapability(stack).getPlatformName();
    }
}