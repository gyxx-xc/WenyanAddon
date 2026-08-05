package org.wenyan.wenyan_addon.dye;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiFunction;

public class Dye {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> DYE_PACKAGE = (pos, state) -> dyePackage();

    private static RawHandlerPackage dyePackage() {
        HandlerPackageBuilder builder = HandlerPackageBuilder.create();

        // 为每种颜色添加染色指令，支持坐标参数
        addDyeShortcuts(builder, DyeColor.WHITE, "白");
        addDyeShortcuts(builder, DyeColor.ORANGE, "橙");
        addDyeShortcuts(builder, DyeColor.MAGENTA, "品红");
        addDyeShortcuts(builder, DyeColor.LIGHT_BLUE, "浅蓝");
        addDyeShortcuts(builder, DyeColor.YELLOW, "黄");
        addDyeShortcuts(builder, DyeColor.LIME, "黄绿");
        addDyeShortcuts(builder, DyeColor.PINK, "粉");
        addDyeShortcuts(builder, DyeColor.GRAY, "灰");
        addDyeShortcuts(builder, DyeColor.LIGHT_GRAY, "浅灰");
        addDyeShortcuts(builder, DyeColor.CYAN, "青");
        addDyeShortcuts(builder, DyeColor.PURPLE, "紫");
        addDyeShortcuts(builder, DyeColor.BLUE, "蓝");
        addDyeShortcuts(builder, DyeColor.BROWN, "棕");
        addDyeShortcuts(builder, DyeColor.GREEN, "绿");
        addDyeShortcuts(builder, DyeColor.RED, "红");
        addDyeShortcuts(builder, DyeColor.BLACK, "黑");

        return builder.build();
    }

    private static void addDyeShortcuts(HandlerPackageBuilder builder, DyeColor color, String... colorNames) {
        for (String colorName : colorNames) {
            addDyeShortcut(builder, colorName, color);
        }
    }

    private static void addDyeShortcut(HandlerPackageBuilder builder, String colorName, DyeColor color) {
        builder.handler(ChineseUtils.bracketOf("染" + colorName), (iHandleContext, request) -> {
            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                Level level = context.level();
                BlockPos basePos = context.pos();
                var args=request.args();
                Vec3 vec=args.get(0).as(WenyanVec3.TYPE).value();
                BlockPos targetPos = new BlockPos(
                        (int) Math.floor(vec.x),
                        (int) Math.floor(vec.y),
                        (int) Math.floor(vec.z)
                );

                if (dyeAt(level, targetPos, color)) {
                    return new WenyanDouble(1);
                }
            }
            return new WenyanDouble(0);
        });
    }

    private static boolean dyeAt(Level level, BlockPos pos, DyeColor color) {
        boolean changed = false;
        AABB area = new AABB(pos).inflate(0.5);
        for (Sheep sheep : level.getEntitiesOfClass(Sheep.class, area)) {
            sheep.setColor(color);
            changed = true;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SignBlockEntity sign) {
            sign.setText(sign.getFrontText().setColor(color), true);
            sign.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            changed = true;
        }

        BlockState state = level.getBlockState(pos);
        Block dyedBlock = dyedVariant(state.getBlock(), color);
        if (dyedBlock != null && dyedBlock != state.getBlock()) {
            level.setBlock(pos, dyedBlock.withPropertiesOf(state), 3);
            changed = true;
        }
        return changed;
    }

    private static Block dyedVariant(Block block, DyeColor color) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (!"minecraft".equals(id.getNamespace())) {
            return null;
        }
        String path = stripColorPrefix(id.getPath());
        String targetPath = targetDyePath(path, color.getName());
        if (targetPath == null) {
            return null;
        }
        Block target = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft", targetPath));
        return target == Blocks.AIR ? null : target;
    }

    private static String stripColorPrefix(String path) {
        for (DyeColor color : DyeColor.values()) {
            String prefix = color.getName() + "_";
            if (path.startsWith(prefix)) {
                return path.substring(prefix.length());
            }
        }
        return path;
    }

    private static String targetDyePath(String path, String colorName) {
        return switch (path) {
            case "wool", "carpet", "terracotta", "concrete", "concrete_powder", "glazed_terracotta",
                 "bed", "candle", "banner", "shulker_box" -> colorName + "_" + path;
            case "stained_glass", "glass" -> colorName + "_stained_glass";
            case "stained_glass_pane", "glass_pane" -> colorName + "_stained_glass_pane";
            default -> null;
        };
    }
}