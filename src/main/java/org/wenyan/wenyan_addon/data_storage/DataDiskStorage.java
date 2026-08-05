package org.wenyan.wenyan_addon.data_storage;

import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.LevelResource;
import org.wenyan.wenyan_addon.WenyanAddon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

public final class DataDiskStorage {
    private static final String DISK_ID_KEY = "WenyanAddonDataDisk";

    private DataDiskStorage() {
    }

    public static boolean isDataDisk(ItemStack stack) {
        return !stack.isEmpty() && stack.is(WenyanAddon.DATA_DISK_ITEM.get());
    }

    public static UUID getOrCreateDiskId(ItemStack stack) {
        Optional<UUID> existing = getDiskId(stack);
        if (existing.isPresent()) {
            return existing.get();
        }
        UUID id = UUID.randomUUID();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(DISK_ID_KEY, id.toString()));
        return id;
    }

    public static Optional<UUID> getDiskId(ItemStack stack) {
        if (!isDataDisk(stack)) {
            return Optional.empty();
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return Optional.empty();
        }
        CompoundTag tag = data.copyTag();
        return tag.getString(DISK_ID_KEY).flatMap(raw -> {
            try {
                return Optional.of(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                return Optional.empty();
            }
        });
    }

    public static IWenyanValue read(ServerLevel level, UUID diskId) {
        Path path = path(level, diskId);
        if (!Files.exists(path)) {
            return WenyanBoolean.FALSE;
        }
        try {
            CompoundTag tag = NbtIo.read(path);
            if (tag == null) {
                return WenyanBoolean.FALSE;
            }
            return WenyanNbtCodec.fromNbt(tag);
        } catch (IOException | NbtException e) {
            WenyanAddon.LOGGER.warn("Failed to read Wenyan data disk {}", diskId, e);
            return WenyanBoolean.FALSE;
        }
    }

    public static boolean write(ServerLevel level, UUID diskId, IWenyanValue value) {
        Path path = path(level, diskId);
        try {
            Files.createDirectories(path.getParent());
            Path temp = path.resolveSibling(path.getFileName() + ".tmp");
            NbtIo.write(WenyanNbtCodec.toNbt(value), temp);
            try {
                Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            WenyanAddon.LOGGER.warn("Failed to write Wenyan data disk {}", diskId, e);
            return false;
        }
    }

    private static Path path(ServerLevel level, UUID diskId) {
        return level.getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("wenyan_addon")
                .resolve("data_disks")
                .resolve(diskId + ".nbt");
    }
}
