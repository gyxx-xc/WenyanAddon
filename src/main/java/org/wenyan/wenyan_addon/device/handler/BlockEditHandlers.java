package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.WenyanNull;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;

import java.util.function.BiFunction;
import java.util.function.Function;


public class BlockEditHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> BLOCK_EDIT_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("放置背包中的方块")
            .handler(ChineseUtils.bracketOf("放置"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (request.args().isEmpty()) {
                    return WenyanValues.of(0);
                }
                Vec3 placeVec = request.args().get(0).as(WenyanVec3.TYPE).value();
                BlockPos pos = BlockPos.containing(placeVec);
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null) {
                    return WenyanValues.of(0);
                }
                ItemStack found = findBlockStack(caster);
                if (found.isEmpty()) {
                    return WenyanValues.of(0);
                }
                Block block = ((BlockItem) found.getItem()).getBlock();
                boolean result = ctx.level().setBlock(pos, block.defaultBlockState(), 3);
                if (result) {
                    playPlaceSound(ctx.level(), pos);
                    if (!caster.hasInfiniteMaterials()) {
                        consumeBlockStack(caster, block);
                    }
                }
                return WenyanValues.of(result ? 1 : 0);
            }))
            .description("挖掘指定位置的方块（钻石镐等级，掉落）")
            .handler(ChineseUtils.bracketOf("挖掘"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = request.args();
                if (request.args().isEmpty()) {
                    return WenyanValues.of(0);
                }
                Vec3 placeVec = request.args().get(0).as(WenyanVec3.TYPE).value();
                BlockPos pos = BlockPos.containing(placeVec);
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null) {
                    return WenyanValues.of(0);
                }
                return WenyanValues.of(breakBlock(ctx.level(), pos, caster));
            }))
            .description("交换两处方块，容器换容器则A向B转移物品")
            .handler(ChineseUtils.bracketOf("互换"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (request.args().size() < 2) {
                    return WenyanValues.of(0);
                }
                Vec3 vecA = request.args().get(0).as(WenyanVec3.TYPE).value();
                Vec3 vecB = request.args().get(1).as(WenyanVec3.TYPE).value();
                BlockPos posA = BlockPos.containing(vecA);
                BlockPos posB = BlockPos.containing(vecB);
                return WenyanValues.of(swapBlocks(ctx.level(), posA, posB) ? 1 : 0);
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_BLOCK_EDIT_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("放置背包中的方块")
            .handler(ChineseUtils.bracketOf("放置"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (argsRequest.args().isEmpty()) {
                        return WenyanValues.of(0);
                    }
                    Vec3 placeVec = argsRequest.args().get(0).as(WenyanVec3.TYPE).value();
                    BlockPos pos = BlockPos.containing(placeVec);
                    Player caster = entity.getPlayer();
                    if (caster == null) {
                        return WenyanValues.of(0);
                    }
                    ItemStack found = findBlockStack(caster);
                    if (found.isEmpty()) {
                        return WenyanValues.of(0);
                    }
                    Block block = ((BlockItem) found.getItem()).getBlock();
                    boolean result = entity.level().setBlock(pos, block.defaultBlockState(), 3);
                    if (result) {
                        playPlaceSound(entity.level(), pos);
                        if (!caster.hasInfiniteMaterials()) {
                            consumeBlockStack(caster, block);
                        }
                    }
                    return WenyanValues.of(result ? 1 : 0);
                }
                return WenyanNull.NULL;
            })
            .description("挖掘指定位置的方块（钻石镐等级，掉落）")
            .handler(ChineseUtils.bracketOf("挖掘"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    Player caster = entity.getPlayer();
                    if (caster == null) {
                        return WenyanValues.of(0);
                    }
                    return WenyanValues.of(breakBlock(entity.level(), pos, caster));
                }
                return WenyanNull.NULL;
            })
            .description("交换两处方块，容器换容器则A向B转移物品")
            .handler(ChineseUtils.bracketOf("互换"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (argsRequest.args().size() < 2) {
                        return WenyanValues.of(0);
                    }
                    Vec3 vecA = argsRequest.args().get(0).as(WenyanVec3.TYPE).value();
                    Vec3 vecB = argsRequest.args().get(1).as(WenyanVec3.TYPE).value();
                    BlockPos posA = BlockPos.containing(vecA);
                    BlockPos posB = BlockPos.containing(vecB);
                    return WenyanValues.of(swapBlocks(entity.level(), posA, posB) ? 1 : 0);
                }
                return WenyanNull.NULL;
            })
            .build();

    // ===== 背包方块搜索 =====

    private static final int HOTBAR_SLOTS = 9;

    private static boolean swapBlocks(Level level, BlockPos posA, BlockPos posB) {
        BlockState stateA = level.getBlockState(posA);
        BlockState stateB = level.getBlockState(posB);
        if (stateA.isAir() && stateB.isAir()) {
            return false;
        }
        BlockEntity entityA = level.getBlockEntity(posA);
        BlockEntity entityB = level.getBlockEntity(posB);

        if (entityA instanceof Container containerA && entityB instanceof Container containerB) {
            // 容器换容器：A 向 B 转移物品，B 满即停止，剩余留在 A
            transferContainerContents(containerA, containerB);
            return true;
        }
        if (entityA instanceof Container containerA) {
            // 容器与普通方块交换：物品全掉落在原地
            Containers.dropContents(level, posA, containerA);
        } else if (entityB instanceof Container containerB) {
            Containers.dropContents(level, posB, containerB);
        }
        CompoundTag tagA = entityA != null ? entityA.saveWithFullMetadata(level.registryAccess()) : null;
        CompoundTag tagB = entityB != null ? entityB.saveWithFullMetadata(level.registryAccess()) : null;
        level.setBlock(posA, stateB, 3);
        level.setBlock(posB, stateA, 3);
        loadBlockEntity(level, posA, tagB);
        loadBlockEntity(level, posB, tagA);
        return true;
    }

    private static void loadBlockEntity(Level level, BlockPos pos, @Nullable CompoundTag tag) {
        if (tag == null || !level.getBlockState(pos).hasBlockEntity()) {
            return;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(WenyanAddon.LOGGER)) {
                entity.loadWithComponents(TagValueInput.create(reporter, level.registryAccess(), tag));
            }
            entity.setChanged();
        }
    }

    private static void transferContainerContents(Container from, Container to) {
        for (int slot = 0; slot < from.getContainerSize(); slot++) {
            ItemStack stack = from.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            from.setItem(slot, insertIntoContainer(to, stack));
            if (!from.getItem(slot).isEmpty()) {
                break; // 目标容器已满，停止，剩余留在原容器
            }
        }
        from.setChanged();
        to.setChanged();
    }

    private static ItemStack insertIntoContainer(Container container, ItemStack stack) {
        for (int slot = 0; slot < container.getContainerSize() && !stack.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty()) {
                int max = Math.min(stack.getMaxStackSize(), container.getMaxStackSize(stack));
                container.setItem(slot, stack.split(Math.min(stack.getCount(), max)));
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int space = Math.min(stack.getMaxStackSize(), container.getMaxStackSize(stack)) - existing.getCount();
                if (space > 0) {
                    int count = Math.min(stack.getCount(), space);
                    existing.grow(count);
                    stack.shrink(count);
                }
            }
        }
        return stack;
    }

    private static int breakBlock(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return 0;
        }
        Block block = state.getBlock();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BlockState adjusted = block.playerWillDestroy(level, pos, state, player);
        ItemStack diamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        boolean canHarvest = !adjusted.requiresCorrectToolForDrops() || diamondPickaxe.isCorrectToolForDrops(adjusted);
        boolean removed = adjusted.onDestroyedByPlayer(level, pos, player, diamondPickaxe, canHarvest, level.getFluidState(pos));
        if (removed) {
            block.destroy(level, pos, adjusted);
            playBreakEffect(level, pos, player, adjusted);
            if (canHarvest) {
                block.playerDestroy(level, player, pos, adjusted, blockEntity, diamondPickaxe);
            }
            return 1;
        }
        return 0;
    }

    private static void playBreakEffect(Level level, BlockPos pos, Player player, BlockState state) {
        // playerWillDestroy 的 levelEvent 广播会排除施法者，需单独补发给施法者
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundLevelEventPacket(2001, pos, Block.getId(state), false));
        }
    }

    private static void playPlaceSound(Level level, BlockPos pos) {
        SoundType soundType = level.getBlockState(pos).getSoundType();
        level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
    }

    private static ItemStack findBlockStack(Player player) {
        Inventory inventory = player.getInventory();
        ItemStack stack = firstBlockStack(inventory, 0, HOTBAR_SLOTS);
        if (stack.isEmpty()) {
            stack = firstBlockStack(inventory, HOTBAR_SLOTS, inventory.getContainerSize());
        }
        return stack;
    }

    private static ItemStack firstBlockStack(Inventory inventory, int start, int end) {
        for (int i = start; i < end; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void consumeBlockStack(Player player, Block block) {
        Inventory inventory = player.getInventory();
        int slot = findBlockSlot(inventory, HOTBAR_SLOTS, inventory.getContainerSize(), block);
        if (slot < 0) {
            slot = findBlockSlot(inventory, 0, HOTBAR_SLOTS, block);
        }
        if (slot >= 0) {
            inventory.removeItem(slot, 1);
        }
    }

    private static int findBlockSlot(Inventory inventory, int start, int end, Block block) {
        for (int i = start; i < end; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block) {
                return i;
            }
        }
        return -1;
    }
}
