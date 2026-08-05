package org.wenyan.wenyan_addon.data_storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.LevelResource;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.value.WenyanMapValue;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

public final class DataDiskStorage {
    private static final String DISK_ID_KEY = "WenyanAddonDataDisk";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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

    public static WenyanMapValue read(ServerLevel level, UUID diskId) {
        Path path = path(level, diskId);
        if (!Files.exists(path)) {
            return new WenyanMapValue();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement json = JsonParser.parseReader(reader);
            return WenyanJsonCodec.objectFromJson(json);
        } catch (Exception e) {
            WenyanMapValue broken = new WenyanMapValue();
            broken.put("亂", readRaw(path));
            broken.put("因", new WenyanString(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
            return broken;
        }
    }

    public static IWenyanValue readKey(ServerLevel level, UUID diskId, String key) {
        return read(level, diskId).get(key);
    }

    public static boolean writeKey(ServerLevel level, UUID diskId, String key, IWenyanValue value) {
        WenyanMapValue data = read(level, diskId);
        data.put(key, value);
        return write(level, diskId, data);
    }

    public static IWenyanValue deleteKey(ServerLevel level, UUID diskId, String key) {
        WenyanMapValue data = read(level, diskId);
        IWenyanValue removed = data.remove(key);
        if (write(level, diskId, data)) {
            return removed;
        }
        return WenyanNull.NULL;
    }

    public static boolean write(ServerLevel level, UUID diskId, WenyanMapValue data) {
        Path path = path(level, diskId);
        try {
            Files.createDirectories(path.getParent());
            Path temp = path.resolveSibling(path.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp)) {
                GSON.toJson(WenyanJsonCodec.toJson(data), writer);
            }
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
                .resolve(diskId + ".json");
    }

    private static WenyanString readRaw(Path path) {
        try {
            return new WenyanString(Files.readString(path));
        } catch (IOException e) {
            return new WenyanString("");
        }
    }
}
