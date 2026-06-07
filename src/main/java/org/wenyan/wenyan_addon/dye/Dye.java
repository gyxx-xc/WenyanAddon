package org.wenyan.wenyan_addon.dye;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
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

import java.util.function.BiFunction;

public class Dye {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> DYE_PACKAGE = (pos, state) -> dyePackage();

    private static RawHandlerPackage dyePackage() {
        HandlerPackageBuilder builder = HandlerPackageBuilder.create();
        addDyeShortcuts(builder, DyeColor.WHITE, "白", "白色");
        addDyeShortcuts(builder, DyeColor.ORANGE, "橙", "橙色");
        addDyeShortcuts(builder, DyeColor.MAGENTA, "品红", "品红色", "洋红", "洋红色");
        addDyeShortcuts(builder, DyeColor.LIGHT_BLUE, "浅蓝", "浅蓝色", "淺藍", "淺藍色", "淡蓝", "淡蓝色", "淡藍", "淡藍色");
        addDyeShortcuts(builder, DyeColor.YELLOW, "黄", "黄色", "黃", "黃色");
        addDyeShortcuts(builder, DyeColor.LIME, "黄绿", "黄绿色", "黃綠", "黃綠色", "青柠", "青柠色", "青檸", "青檸色");
        addDyeShortcuts(builder, DyeColor.PINK, "粉", "粉色");
        addDyeShortcuts(builder, DyeColor.GRAY, "灰", "灰色");
        addDyeShortcuts(builder, DyeColor.LIGHT_GRAY, "浅灰", "浅灰色", "淺灰", "淺灰色", "淡灰", "淡灰色");
        addDyeShortcuts(builder, DyeColor.CYAN, "青", "青色");
        addDyeShortcuts(builder, DyeColor.PURPLE, "紫", "紫色");
        addDyeShortcuts(builder, DyeColor.BLUE, "蓝", "蓝色", "藍", "藍色");
        addDyeShortcuts(builder, DyeColor.BROWN, "棕", "棕色", "褐", "褐色");
        addDyeShortcuts(builder, DyeColor.GREEN, "绿", "绿色", "綠", "綠色");
        addDyeShortcuts(builder, DyeColor.RED, "红", "红色", "紅", "紅色");
        addDyeShortcuts(builder, DyeColor.BLACK, "黑", "黑色");
        return builder.build();
    }

    private static void addDyeShortcuts(HandlerPackageBuilder builder, DyeColor color, String... colorNames) {
        for (String colorName : colorNames) {
            addDyeShortcut(builder, "右", 1, 0, 0, colorName, color);
            addDyeShortcut(builder, "左", -1, 0, 0, colorName, color);
            addDyeShortcut(builder, "上", 0, 1, 0, colorName, color);
            addDyeShortcut(builder, "下", 0, -1, 0, colorName, color);
            addDyeShortcut(builder, "前", 0, 0, 1, colorName, color);
            addDyeShortcut(builder, "後", 0, 0, -1, colorName, color);
        }
    }

    private static void addDyeShortcut(HandlerPackageBuilder builder, String directionName, int dx, int dy, int dz, String colorName, DyeColor color) {
        builder.handler(ChineseUtils.bracketOf("染" + directionName + colorName), (iHandleContext, _) -> dyeFixedDirection(iHandleContext, dx, dy, dz, color));
    }

    private static WenyanDouble dyeFixedDirection(Object iHandleContext, int dx, int dy, int dz, DyeColor color) {
        if (iHandleContext instanceof BlockRequest.BlockContext context
                && dyeAt(context.level(), context.pos().offset(dx, dy, dz), color)) {
            return new WenyanDouble(1);
        }
        return new WenyanDouble(0);
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
