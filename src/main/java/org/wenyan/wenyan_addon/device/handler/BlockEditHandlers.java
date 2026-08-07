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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;

import java.util.function.BiFunction;
import java.util.function.Function;


public class BlockEditHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> BLOCK_EDIT_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("放置背包中的方块")
            .handler(ChineseUtils.bracketOf("置"), BlockHandlerHelper.wrap((ctx, request) -> {
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
                if (result && !caster.hasInfiniteMaterials()) {
                    consumeBlockStack(caster, block);
                }
                return WenyanValues.of(result ? 1 : 0);
            }))
            .description("破坏指定位置的方块")
            .handler(ChineseUtils.bracketOf("破"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                boolean result = ctx.level().destroyBlock(pos, true);
                return WenyanValues.of(result ? 1 : 0);
            }))
            .description("将指定位置的方块替换为另一种方块，尽量保留原方块属性")
            .handler(ChineseUtils.bracketOf("替"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                String toBlockId = args.get(3);
                try {
                    Identifier id = Identifier.parse(toBlockId);
                    if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                        return WenyanValues.of(0);
                    }
                    Block targetBlock = BuiltInRegistries.BLOCK.getValue(id);
                    BlockState oldState = ctx.level().getBlockState(pos);
                    BlockState newState;
                    try {
                        newState = targetBlock.withPropertiesOf(oldState);
                    } catch (IllegalArgumentException e) {
                        newState = targetBlock.defaultBlockState();
                    }
                    boolean result = ctx.level().setBlock(pos, newState, 3);
                    return WenyanValues.of(result ? 1 : 0);
                } catch (Exception e) {
                    return WenyanValues.of(0);
                }
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_BLOCK_EDIT_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("放置背包中的方块")
            .handler(ChineseUtils.bracketOf("置"), (ctx, argsRequest) -> {
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
                    if (result && !caster.hasInfiniteMaterials()) {
                        consumeBlockStack(caster, block);
                    }
                    return WenyanValues.of(result ? 1 : 0);
                }
                return WenyanNull.NULL;
            })
            .description("破坏指定位置的方块")
            .handler(ChineseUtils.bracketOf("破"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    boolean result = entity.level().destroyBlock(BlockHandlerHelper.offsetPos(entity.blockPosition(), args), true);
                    return WenyanValues.of(result ? 1 : 0);
                }
                return WenyanNull.NULL;
            })
            .description("将指定位置的方块替换为另一种方块，尽量保留原方块属性")
            .handler(ChineseUtils.bracketOf("替"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    String toBlockId = args.get(3);
                    try {
                        Identifier id = Identifier.parse(toBlockId);
                        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                            return WenyanValues.of(0);
                        }
                        Block targetBlock = BuiltInRegistries.BLOCK.getValue(id);
                        BlockState oldState = entity.level().getBlockState(pos);
                        BlockState newState;
                        try {
                            newState = targetBlock.withPropertiesOf(oldState);
                        } catch (IllegalArgumentException e) {
                            newState = targetBlock.defaultBlockState();
                        }
                        boolean result = entity.level().setBlock(pos, newState, 3);
                        return WenyanValues.of(result ? 1 : 0);
                    } catch (Exception e) {
                        return WenyanValues.of(0);
                    }
                }
                return WenyanNull.NULL;
            })
            .build();

    // ===== 背包方块搜索 =====

    private static final int HOTBAR_SLOTS = 9;

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
