package org.wenyan.wenyan_addon.spell;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.wenyan.wenyan_addon.WenyanAddon;

import java.util.ArrayList;
import java.util.List;

/**
 * 符咒包/拓展包容器菜单：操作物品上的 POUCH_DATA 组件。
 * 54 格（9×6，大箱子大小）。槽位变更实时写回组件（服务端）。
 */
public class FuluPouchMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = 54;
    public static final int ROWS = 6;

    private final ItemStack pouchStack;
    private final SimpleContainer container;
    private final boolean extension;

    private int activeSlot;

    /**
     * 服务端构造：从物品栈组件读取内容，持有引用以便写回。
     */
    public FuluPouchMenu(int containerId, Inventory playerInv, ItemStack pouchStack) {
        super(WenyanAddon.FULU_POUCH_MENU.get(), containerId);
        this.pouchStack = pouchStack;
        this.extension = pouchStack.getItem() instanceof FuluPouchExtensionItem;
        this.container = new SavingContainer();
        FuluPouchComponent component = pouchStack.get(SpellDataComponent.POUCH_DATA.get());
        if (component != null) {
            this.activeSlot = component.activeSlot();
            loadComponent(component);
        }
        addPouchSlots(8, 18);
        addStandardInventorySlots(playerInv, 8, 18 + ROWS * 18 + 13);
    }

    /**
     * 客户端构造：从网络缓冲读取组件数据。
     */
    public FuluPouchMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInv, buffer.readBoolean(), FuluPouchComponent.STREAM_CODEC.decode(buffer));
    }

    private FuluPouchMenu(int containerId, Inventory playerInv, boolean extension, FuluPouchComponent component) {
        super(WenyanAddon.FULU_POUCH_MENU.get(), containerId);
        this.pouchStack = ItemStack.EMPTY;
        this.extension = extension;
        this.container = new SimpleContainer(SLOT_COUNT);
        this.activeSlot = component.activeSlot();
        loadComponent(component);
        addPouchSlots(8, 18);
        addStandardInventorySlots(playerInv, 8, 18 + ROWS * 18 + 13);
    }

    private void loadComponent(FuluPouchComponent component) {
        List<ItemStack> items = component.createItems();
        for (int i = 0; i < items.size() && i < SLOT_COUNT; i++) {
            container.setItem(i, items.get(i));
        }
    }

    /**
     * 服务端容器：内容变更时立即写回背包物品上的组件，并同步选中材质（仅符咒包）。
     */
    private final class SavingContainer extends SimpleContainer {
        private SavingContainer() {
            super(SLOT_COUNT);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            save();
            // 取出/更换选中槽物品后，材质随之切换（拓展包不切换材质）
            if (!pouchStack.isEmpty() && !extension) {
                FuluPouchItem.applySelectedModel(pouchStack, container.getItem(activeSlot));
            }
        }
    }

    private void addPouchSlots(int left, int top) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                this.addSlot(new Slot(container, index, left + col * 18, top + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        if (extension) {
                            return FuluPouchExtensionItem.canStore(stack);
                        }
                        return FuluPouchItem.canStore(stack);
                    }
                });
            }
        }
    }

    private void addStandardInventorySlots(Inventory playerInventory, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, left + col * 18, top + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, left + col * 18, top + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < SLOT_COUNT) {
                if (!moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stack, 0, SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return !pouchStack.isEmpty() || !player.isRemoved();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        save();
    }

    private void save() {
        if (pouchStack.isEmpty()) {
            return;
        }
        List<ItemStack> items = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.add(container.getItem(i));
        }
        FuluPouchComponent component = FuluPouchComponent.EMPTY.withItems(items).withActiveSlot(activeSlot);
        pouchStack.set(SpellDataComponent.POUCH_DATA.get(), component);
    }

    public boolean isExtension() {
        return extension;
    }

    public int activeSlot() {
        return activeSlot;
    }

    public void setActiveSlot(int activeSlot) {
        this.activeSlot = activeSlot;
        save();
        // 材质切换仅符咒包（拓展包不切换材质）
        if (!pouchStack.isEmpty() && !extension) {
            FuluPouchItem.applySelectedModel(pouchStack, container.getItem(activeSlot));
        }
    }

    public ItemStack getPouchStack() {
        return pouchStack;
    }
}