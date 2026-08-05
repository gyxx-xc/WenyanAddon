package org.wenyan.wenyan_addon;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.wenyan.pong.Pong;
import org.wenyan.pong.setup.PongRegistration;
import org.wenyan.wenyan_addon.item.TooltipBlockItem;
import org.wenyan.wenyan_addon.item.TooltipItem;

@Mod(WenyanAddon.MODID)
public class WenyanAddon {
    public static final String MODID = "wenyan_addon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final ResourceKey<CreativeModeTab> WENYAN_PROGRAMMING_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("wenyan_programming", "wenyan_programming")
    );

    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = registerTooltipBlockItem("example_block", EXAMPLE_BLOCK);

    public static final DeferredBlock<Block> PROJECTILE_SPAWNER_BLOCK = BLOCKS.registerSimpleBlock("projectile_spawner_block", p -> p.mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> PROJECTILE_SPAWNER_BLOCK_ITEM = registerTooltipBlockItem("projectile_spawner_block", PROJECTILE_SPAWNER_BLOCK);

    public static final DeferredBlock<Block> FLUID_BLOCK = BLOCKS.registerSimpleBlock("fluid_block", p -> p.mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> FLUID_BLOCK_ITEM = registerTooltipBlockItem("fluid_block", FLUID_BLOCK);

    public static final DeferredBlock<Block> WORLD_INTERACTION_BLOCK = BLOCKS.registerSimpleBlock("world_interaction_block", p -> p.mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> WORLD_INTERACTION_BLOCK_ITEM = registerTooltipBlockItem("world_interaction_block", WORLD_INTERACTION_BLOCK);

    public static final DeferredBlock<Block> ENTITY_MANIPULATION_BLOCK = BLOCKS.registerSimpleBlock("entity_manipulation_block", p -> p.mapColor(MapColor.COLOR_PURPLE).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> ENTITY_MANIPULATION_BLOCK_ITEM = registerTooltipBlockItem("entity_manipulation_block", ENTITY_MANIPULATION_BLOCK);

    public static final DeferredBlock<Block> NOTE_BLOCK_FUNCTION_BLOCK = BLOCKS.registerSimpleBlock("note_block_function_block", p -> p.mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> NOTE_BLOCK_FUNCTION_BLOCK_ITEM = registerTooltipBlockItem("note_block_function_block", NOTE_BLOCK_FUNCTION_BLOCK);

    public static final DeferredBlock<Block> READ_WRITE_BLOCK = BLOCKS.registerSimpleBlock("read_write_block", p -> p.mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> READ_WRITE_BLOCK_ITEM = registerTooltipBlockItem("read_write_block", READ_WRITE_BLOCK);

    public static final DeferredBlock<Block> NAMING_BLOCK = BLOCKS.registerSimpleBlock("naming_block", p -> p.mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.ANVIL));
    public static final DeferredItem<BlockItem> NAMING_BLOCK_ITEM = registerTooltipBlockItem("naming_block", NAMING_BLOCK);

    public static final DeferredBlock<Block> PARTICLE_BLOCK = BLOCKS.registerSimpleBlock("particle_block", p -> p.mapColor(MapColor.COLOR_PURPLE).strength(2.0f).sound(SoundType.GLASS));
    public static final DeferredItem<BlockItem> PARTICLE_BLOCK_ITEM = registerTooltipBlockItem("particle_block", PARTICLE_BLOCK);

    public static final DeferredBlock<Block> DYE_BLOCK = BLOCKS.registerSimpleBlock("dye_block", p -> p.mapColor(MapColor.COLOR_RED).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> DYE_BLOCK_ITEM = registerTooltipBlockItem("dye_block", DYE_BLOCK);

    public static final DeferredBlock<Block> MARKER_BLOCK = BLOCKS.registerSimpleBlock("marker_block", p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> MARKER_BLOCK_ITEM = registerTooltipBlockItem("marker_block", MARKER_BLOCK);

    public static final DeferredBlock<Block> ENTITY_STATUS_BLOCK = BLOCKS.registerSimpleBlock("entity_status_block", p -> p.mapColor(MapColor.COLOR_RED).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> ENTITY_STATUS_BLOCK_ITEM = registerTooltipBlockItem("entity_status_block", ENTITY_STATUS_BLOCK);

    public static final DeferredBlock<Block> ENTITY_SPAWN_BLOCK = BLOCKS.registerSimpleBlock("entity_spawn_block", p -> p.mapColor(MapColor.COLOR_PURPLE).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> ENTITY_SPAWN_BLOCK_ITEM = registerTooltipBlockItem("entity_spawn_block", ENTITY_SPAWN_BLOCK);

    public static final DeferredBlock<Block> POTION_BLOCK = BLOCKS.registerSimpleBlock("potion_block", p -> p.mapColor(MapColor.COLOR_CYAN).strength(2.0f).sound(SoundType.GLASS));
    public static final DeferredItem<BlockItem> POTION_BLOCK_ITEM = registerTooltipBlockItem("potion_block", POTION_BLOCK);

    public static final DeferredBlock<Block> BLOCK_EDIT_BLOCK = BLOCKS.registerSimpleBlock("block_edit_block", p -> p.mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> BLOCK_EDIT_BLOCK_ITEM = registerTooltipBlockItem("block_edit_block", BLOCK_EDIT_BLOCK);

    public static final DeferredBlock<Block> ENCHANT_BLOCK = BLOCKS.registerSimpleBlock("enchant_block", p -> p.mapColor(MapColor.COLOR_YELLOW).strength(2.0f).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> ENCHANT_BLOCK_ITEM = registerTooltipBlockItem("enchant_block", ENCHANT_BLOCK);

    public static final DeferredBlock<StorageRuneBlock> STORAGE_RUNE_BLOCK = BLOCKS.registerBlock("storage_rune_block", StorageRuneBlock::new);
    public static final DeferredItem<BlockItem> STORAGE_RUNE_BLOCK_ITEM = registerTooltipBlockItem("storage_rune_block", STORAGE_RUNE_BLOCK);
    public static final DeferredItem<Item> DATA_DISK_ITEM = ITEMS.registerItem("data_disk", properties -> new TooltipItem(properties, tooltipKey("data_disk")), properties -> properties.stacksTo(1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageRuneBlockEntity>> STORAGE_RUNE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "storage_rune_block",
            () -> new BlockEntityType<>(StorageRuneBlockEntity::new, STORAGE_RUNE_BLOCK.get())
    );

    public static final DeferredBlock<Block> MESSAGE_BLOCK = BLOCKS.registerSimpleBlock("message_block", p -> p.mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> MESSAGE_BLOCK_ITEM = registerTooltipBlockItem("message_block", MESSAGE_BLOCK);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WENYAN_ADDON_TAB = CREATIVE_MODE_TABS.register("wenyan_addon", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.wenyan_addon"))
            .withTabsBefore(CreativeModeTabs.COMBAT, WENYAN_PROGRAMMING_TAB_KEY)
            .icon(() -> PROJECTILE_SPAWNER_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_BLOCK_ITEM.get());
                output.accept(PROJECTILE_SPAWNER_BLOCK_ITEM.get());
                output.accept(FLUID_BLOCK_ITEM.get());
                output.accept(WORLD_INTERACTION_BLOCK_ITEM.get());
                output.accept(ENTITY_MANIPULATION_BLOCK_ITEM.get());
                output.accept(NOTE_BLOCK_FUNCTION_BLOCK_ITEM.get());
                output.accept(READ_WRITE_BLOCK_ITEM.get());
                output.accept(NAMING_BLOCK_ITEM.get());
                output.accept(PARTICLE_BLOCK_ITEM.get());
                output.accept(DYE_BLOCK_ITEM.get());
                output.accept(MARKER_BLOCK_ITEM.get());
                output.accept(ENTITY_STATUS_BLOCK_ITEM.get());
                output.accept(ENTITY_SPAWN_BLOCK_ITEM.get());
                output.accept(POTION_BLOCK_ITEM.get());
                output.accept(BLOCK_EDIT_BLOCK_ITEM.get());
                output.accept(ENCHANT_BLOCK_ITEM.get());
                output.accept(STORAGE_RUNE_BLOCK_ITEM.get());
                output.accept(DATA_DISK_ITEM.get());
                output.accept(PongRegistration.CHAMPAGNE.get());
                output.accept(PongRegistration.CHAMPAGNE_SABRE.get());
                output.accept(PongRegistration.GOBLET.get());
                output.accept(PongRegistration.CHAMPAGNE_RACK_ITEM.get());
                output.accept(PongRegistration.PLUG.get());
                output.accept(MESSAGE_BLOCK_ITEM.get());
            }).build());

    @SuppressWarnings("unused")
    public WenyanAddon(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(Capabilities::registerCapabilities);
        Pong.register(modEventBus);
    }

    private static DeferredItem<BlockItem> registerTooltipBlockItem(String name, DeferredBlock<? extends Block> block) {
        return ITEMS.registerItem(name, properties -> new TooltipBlockItem(block.get(), properties, tooltipKey(name)), Item.Properties::useBlockDescriptionPrefix);
    }

    private static String tooltipKey(String name) {
        return "item." + MODID + "." + name + ".tooltip";
    }
}
