package org.wenyan.wenyan_addon.spell;

import indi.wenyan.content.item.FloatNoteItem;
import indi.wenyan.content.item.ItemCodeHolder;
import indi.wenyan.content.item.RunnerItem;
import indi.wenyan.content.item.throw_runner.ThrowRunnerItem;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

/**
 * 符咒包：存放投符/符咒的容器。
 * - shift+右键：打开容器菜单（最高优先级）
 * - shift+滚轮：切换选中符咒（服务端处理 {@link #handleScrollSwitch}）
 * - 选中投符 + 右键：投出投符（与本体 ThrowRunnerItem 一致）
 * - 选中符咒 + 右键方块：将符咒放置在方块面（与本体一致）
 * 运行时扫描不会扫描符咒包（计划规定，仅拓展包可被扫描）。
 * 与符咒拓展包（FuluPouchExtensionItem）行为完全分离：本包可切换/可使用。
 */
@ParametersAreNonnullByDefault
public class FuluPouchItem extends Item {
    public FuluPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            openMenu(level, player, stack);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // 选中投符：投出（与本体 ThrowRunnerItem.use 一致），并应用投符自身的冷却
        ItemStack selected = selectedOf(stack);
        if (selected.getItem() instanceof ThrowRunnerItem throwRunner
                && level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer) {
            // 投符冷却中：不投掷（与本体手持投符时一致）
            if (serverPlayer.getCooldowns().isOnCooldown(selected)) {
                return InteractionResult.PASS;
            }
            Projectile.spawnProjectileFromRotation(
                    (s, p, l) -> new ThrowRunnerEntity(s, p, l, throwRunner.getTier()),
                    serverLevel, selected, serverPlayer, 0.5F, 0.1F, 5.0F);
            // 与本体 ItemStack.use 的 applyAfterUseComponentSideEffects 一致：仅给投符上冷却
            net.minecraft.world.item.component.UseCooldown cooldown = selected.get(DataComponents.USE_COOLDOWN);
            if (cooldown != null) {
                cooldown.apply(selected, serverPlayer);
            }
            consumeSelected(stack, selected);
            return InteractionResult.SUCCESS;
        }
        openMenu(level, player, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            openMenu(context.getLevel(), context.getPlayer(), stack);
            return InteractionResult.SUCCESS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // 选中符咒：将其作为手持物品执行 useOn（放置到方块面 / 贴符）
        ItemStack selected = selectedOf(stack);
        if (selected.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (selected.getItem() instanceof ThrowRunnerItem) {
            Player player = context.getPlayer();
            if (player == null) {
                return InteractionResult.PASS;
            }
            return use(context.getLevel(), player, context.getHand());
        }
        if (selected.getItem() instanceof FloatNoteItem || selected.getItem() instanceof net.minecraft.world.item.BlockItem) {
            net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                    context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
            UseOnContext delegated = new UseOnContext(
                    context.getLevel(), context.getPlayer(), context.getHand(), selected, hit);
            InteractionResult result = selected.getItem().useOn(delegated);
            if (result.consumesAction() && context.getPlayer() instanceof ServerPlayer) {
                if (selected.getItem() instanceof FloatNoteItem) {
                    // 贴符：耐久/组件变更写回包内槽位
                    syncSelectedBack(stack, selected);
                } else {
                    // 放置方块：包内选中槽 -1
                    consumeSelected(stack, selected);
                }
            }
            return result;
        }
        return InteractionResult.PASS;
    }

    /**
     * shift+滚轮切换：服务端处理。delta +1 向后（槽位递增），-1 向前。
     * 仅符咒包响应（拓展包不可切换）。若无可选符咒（全空）则视为未选中，恢复自身材质。
     */
    public static void handleScrollSwitch(ServerPlayer player, int delta) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof FuluPouchItem) || stack.getItem() instanceof FuluPouchExtensionItem) {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof FuluPouchItem) || stack.getItem() instanceof FuluPouchExtensionItem) {
                return;
            }
        }
        FuluPouchComponent component = stack.getOrDefault(SpellDataComponent.POUCH_DATA.get(), FuluPouchComponent.EMPTY);
        int start = component.activeSlot();
        int next = start;
        int step = delta >= 0 ? 1 : -1;
        // 未选中时从首尾开始寻找
        if (start < 0) {
            start = step > 0 ? -1 : FuluPouchMenu.SLOT_COUNT;
        }
        for (int i = 1; i <= FuluPouchMenu.SLOT_COUNT; i++) {
            int candidate = Math.floorMod(start + step * i, FuluPouchMenu.SLOT_COUNT);
            if (!component.createItems().get(candidate).isEmpty()) {
                next = candidate;
                break;
            }
        }
        if (next < 0 || next >= FuluPouchMenu.SLOT_COUNT) {
            // 包内全空：无可选符咒，视为未选中并恢复自身材质
            stack.set(SpellDataComponent.POUCH_DATA.get(), component.withActiveSlot(FuluPouchComponent.NO_SELECTION));
            applySelectedModel(stack, ItemStack.EMPTY);
            player.sendSystemMessage(Component.literal("未选中符咒").withStyle(ChatFormatting.GRAY));
            return;
        }
        ItemStack selected = component.createItems().get(next);
        stack.set(SpellDataComponent.POUCH_DATA.get(), component.withActiveSlot(next));
        applySelectedModel(stack, selected);
        if (selected.isEmpty()) {
            player.sendSystemMessage(Component.literal("未选中符咒").withStyle(ChatFormatting.GRAY));
        } else {
            player.sendSystemMessage(Component.literal("当前符咒：")
                    .append(selected.getHoverName()).withStyle(ChatFormatting.GOLD));
        }
    }

    /**
     * shift+左键清空选中：服务端处理。
     * 清空选中（activeSlot=-1）并恢复自身材质；未选中时不做动作。
     * 仅符咒包响应（拓展包不可切换/选中）。
     */
    public static void clearSelection(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof FuluPouchItem) || stack.getItem() instanceof FuluPouchExtensionItem) {
            stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof FuluPouchItem) || stack.getItem() instanceof FuluPouchExtensionItem) {
                return;
            }
        }
        FuluPouchComponent component = stack.getOrDefault(SpellDataComponent.POUCH_DATA.get(), FuluPouchComponent.EMPTY);
        if (component.activeSlot() < 0) {
            return;
        }
        stack.set(SpellDataComponent.POUCH_DATA.get(), component.withActiveSlot(FuluPouchComponent.NO_SELECTION));
        applySelectedModel(stack, ItemStack.EMPTY);
        player.sendSystemMessage(Component.literal("已取消选中符咒").withStyle(ChatFormatting.GRAY));
    }

    /**
     * 打开容器菜单（服务端）。
     */
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
                buffer.writeBoolean(stack.getItem() instanceof FuluPouchExtensionItem);
                FuluPouchComponent.STREAM_CODEC.encode(buffer, component);
            }
        };
        serverPlayer.openMenu(provider);
    }

    /**
     * 选中槽位的符咒（副本，用于投出/贴符）；未选中返回空栈。
     */
    private static ItemStack selectedOf(ItemStack stack) {
        FuluPouchComponent component = stack.get(SpellDataComponent.POUCH_DATA.get());
        if (component == null) {
            return ItemStack.EMPTY;
        }
        return component.selectedItem();
    }

    /**
     * 投出/贴符消耗后：包内选中槽 -1（或移除），并同步材质。
     */
    private static void consumeSelected(ItemStack stack, ItemStack selected) {
        if (selected.isEmpty()) {
            return;
        }
        FuluPouchComponent component = stack.getOrDefault(SpellDataComponent.POUCH_DATA.get(), FuluPouchComponent.EMPTY);
        int slot = component.activeSlot();
        ItemStack rest = selected.copy();
        rest.shrink(1);
        stack.set(SpellDataComponent.POUCH_DATA.get(), component.withItem(slot, rest));
        applySelectedModel(stack, rest);
    }

    /**
     * 贴符（FloatNote/BlockItem useOn）后：把耐久等变更写回包内槽位。
     */
    private static void syncSelectedBack(ItemStack stack, ItemStack selected) {
        FuluPouchComponent component = stack.getOrDefault(SpellDataComponent.POUCH_DATA.get(), FuluPouchComponent.EMPTY);
        int slot = component.activeSlot();
        stack.set(SpellDataComponent.POUCH_DATA.get(), component.withItem(slot, selected));
        applySelectedModel(stack, selected);
    }

    /**
     * 选中符咒时，把包自身材质切换为符咒材质；未选中（空槽）则恢复自身材质。
     * ITEM_MODEL 的值指向 items/ 目录下的物品模型定义 ID（如 wenyan_programming:vec3_module_block）。
     */
    public static void applySelectedModel(ItemStack stack, ItemStack selected) {
        if (selected == null || selected.isEmpty()) {
            Identifier key = stack.getItem().builtInRegistryHolder().key().identifier();
            stack.set(DataComponents.ITEM_MODEL,
                    Identifier.fromNamespaceAndPath(key.getNamespace(), key.getPath()));
            return;
        }
        Identifier key = selected.getItem().builtInRegistryHolder().key().identifier();
        stack.set(DataComponents.ITEM_MODEL,
                Identifier.fromNamespaceAndPath(key.getNamespace(), key.getPath()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        FuluPouchComponent component = stack.get(SpellDataComponent.POUCH_DATA.get());
        if (component == null) {
            return;
        }
        ItemStack selected = component.selectedItem();
        if (selected.isEmpty()) {
            tooltip.accept(Component.literal("未选中符咒").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.literal("当前符咒：").append(selected.getHoverName()).withStyle(ChatFormatting.GOLD));
        }
    }

    /**
     * 判断物品能否存入符咒包：
     * - 符咒包：投符/符咒/浮签/纸类/文言设备（符咒石）等，且排除拓展包
     */
    public static boolean canStore(ItemStack stack) {
        if (stack.getItem() instanceof FuluPouchExtensionItem) {
            return false;
        }
        return isStorable(stack);
    }

    private static boolean isStorable(ItemStack stack) {
        if (SpellCodeHelper.isCodeCarrier(stack)) {
            return true;
        }
        // 文言设备（符咒石等）：通过 WENYAN_ITEM_DEVICE_CAPABILITY 注册，如向量符等本体模块物品
        if (stack.getCapability(WyRegistration.WENYAN_ITEM_DEVICE_CAPABILITY) instanceof indi.wenyan.content.block.IWenyanDevice) {
            return true;
        }
        Item item = stack.getItem();
        if (item instanceof ThrowRunnerItem || item instanceof FloatNoteItem) {
            return true;
        }
        if (item instanceof RunnerItem){
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