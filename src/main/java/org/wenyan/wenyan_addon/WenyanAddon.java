package org.wenyan.wenyan_addon;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.wenyan.pong.Pong;
import org.wenyan.pong.setup.PongRegistration;
import org.wenyan.wenyan_addon.device.handler.data_disk.StorageRuneBlock;
import org.wenyan.wenyan_addon.device.handler.data_disk.StorageRuneBlockEntity;
import org.wenyan.wenyan_addon.device.handler.data_disk.StorageRuneMenu;
import org.wenyan.wenyan_addon.item.DataDiskItem;
import org.wenyan.wenyan_addon.item.TooltipBlockItem;
import org.wenyan.wenyan_addon.qi.damage.QiDamageTypes;
import org.wenyan.wenyan_addon.qi.element.DerivedElement;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.element.RelationType;
import org.wenyan.wenyan_addon.qi.mark.QiMarkEffects;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.potion.QiRestorePotionEffects;
import org.wenyan.wenyan_addon.qi.ritual.QiRitualBlock;
import org.wenyan.wenyan_addon.qi.ritual.QiRitualBlockEntity;
import org.wenyan.wenyan_addon.qi.ritual.QiRitualRecipes;
import org.wenyan.wenyan_addon.qi.storage.QiStorageBlock;
import org.wenyan.wenyan_addon.qi.storage.QiStorageBlockEntity;
import org.wenyan.wenyan_addon.qi.storage.QiVesselItem;

import java.util.List;

@Mod(WenyanAddon.MODID)
public class WenyanAddon {
    public static final String MODID = "wenyan_addon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MODID);
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

    public static final DeferredBlock<Block> MUSIC_BLOCK = BLOCKS.registerSimpleBlock("music_block", p -> p.mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> MUSIC_BLOCK_ITEM = registerTooltipBlockItem("music_block", MUSIC_BLOCK);

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
    public static final DeferredItem<Item> DATA_DISK_ITEM = ITEMS.registerItem("data_disk", properties -> new DataDiskItem(properties, tooltipKey("data_disk")), properties -> properties.stacksTo(1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageRuneBlockEntity>> STORAGE_RUNE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "storage_rune_block",
            () -> new BlockEntityType<>(StorageRuneBlockEntity::new, STORAGE_RUNE_BLOCK.get())
    );
    public static final DeferredHolder<MenuType<?>, MenuType<StorageRuneMenu>> STORAGE_RUNE_MENU = MENUS.register(
            "storage_rune_menu",
            () -> new MenuType<>(StorageRuneMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final DeferredBlock<Block> MESSAGE_BLOCK = BLOCKS.registerSimpleBlock("message_block", p -> p.mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> MESSAGE_BLOCK_ITEM = registerTooltipBlockItem("message_block", MESSAGE_BLOCK);

    public static final DeferredBlock<Block> TIME_BLOCK = BLOCKS.registerSimpleBlock("time_block", p -> p.mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> TIME_BLOCK_ITEM = registerTooltipBlockItem("time_block", TIME_BLOCK);

    public static final DeferredBlock<Block> QI_BLOCK = BLOCKS.registerSimpleBlock("qi_block", p -> p.mapColor(MapColor.COLOR_CYAN).strength(2.0f).sound(SoundType.AMETHYST));
    public static final DeferredItem<BlockItem> QI_BLOCK_ITEM = registerTooltipBlockItem("qi_block", QI_BLOCK);

    public static final DeferredBlock<QiStorageBlock> QI_STORAGE_BLOCK = BLOCKS.registerBlock("qi_storage_block", QiStorageBlock::new);
    public static final DeferredItem<BlockItem> QI_STORAGE_BLOCK_ITEM = registerTooltipBlockItem("qi_storage_block", QI_STORAGE_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QiStorageBlockEntity>> QI_STORAGE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "qi_storage_block",
            () -> new BlockEntityType<>(QiStorageBlockEntity::new, QI_STORAGE_BLOCK.get())
    );
    public static final DeferredItem<Item> QI_VESSEL_ITEM = ITEMS.registerItem("qi_vessel", properties -> new QiVesselItem(properties, tooltipKey("qi_vessel")), properties -> properties.stacksTo(1));

    public static final DeferredItem<Item> YIN_CRYSTAL_ITEM = ITEMS.registerItem("yin_crystal", properties -> new org.wenyan.wenyan_addon.qi.land.YinCrystalItem(properties, tooltipKey("yin_crystal")));
    public static final DeferredItem<Item> YANG_CRYSTAL_ITEM = ITEMS.registerItem("yang_crystal", properties -> new org.wenyan.wenyan_addon.qi.land.YangCrystalItem(properties, tooltipKey("yang_crystal")));
    public static final DeferredItem<Item> SPIRIT_STONE_ITEM = ITEMS.registerItem("spirit_stone", properties -> new org.wenyan.wenyan_addon.qi.storage.SpiritStoneItem(properties, tooltipKey("spirit_stone"), org.wenyan.wenyan_addon.qi.storage.SpiritStoneItem.Grade.PURE));
    public static final DeferredItem<Item> SPIRIT_STONE_IMPURE_ITEM = ITEMS.registerItem("spirit_stone_impure", properties -> new org.wenyan.wenyan_addon.qi.storage.SpiritStoneItem(properties, tooltipKey("spirit_stone_impure"), org.wenyan.wenyan_addon.qi.storage.SpiritStoneItem.Grade.IMPURE));
    public static final DeferredItem<Item> SPIRIT_STONE_REFINED_ITEM = ITEMS.registerItem("spirit_stone_refined", properties -> new org.wenyan.wenyan_addon.qi.storage.SpiritStoneItem(properties, tooltipKey("spirit_stone_refined"), org.wenyan.wenyan_addon.qi.storage.SpiritStoneItem.Grade.REFINED));
    public static final DeferredItem<Item> QI_RESTORE_POTION_SMALL = ITEMS.registerItem("qi_restore_potion_small", properties -> new org.wenyan.wenyan_addon.qi.potion.QiRestorePotionItem(properties.stacksTo(16)));
    public static final DeferredItem<Item> QI_RESTORE_POTION_MEDIUM = ITEMS.registerItem("qi_restore_potion_medium", properties -> new org.wenyan.wenyan_addon.qi.potion.QiRestorePotionItem(properties.stacksTo(16)));
    public static final DeferredItem<Item> QI_RESTORE_POTION_LARGE = ITEMS.registerItem("qi_restore_potion_large", properties -> new org.wenyan.wenyan_addon.qi.potion.QiRestorePotionItem(properties.stacksTo(16)));
    public static final DeferredItem<Item> QI_RESTORE_POTION_SUSTAINED = ITEMS.registerItem("qi_restore_potion_sustained", properties -> new org.wenyan.wenyan_addon.qi.potion.QiRestorePotionItem(properties.stacksTo(16)));
    public static final DeferredItem<Item> QI_LIQUID_BOTTLE_ITEM = ITEMS.registerItem("qi_liquid_bottle", properties -> new org.wenyan.wenyan_addon.qi.liquid.QiLiquidBottleItem(properties, tooltipKey("qi_liquid_bottle")));

    public static final DeferredBlock<org.wenyan.wenyan_addon.qi.liquid.QiLiquidCollectorBlock> QI_LIQUID_COLLECTOR_BLOCK = BLOCKS.registerBlock(
            "qi_liquid_collector_block", org.wenyan.wenyan_addon.qi.liquid.QiLiquidCollectorBlock::new);
    public static final DeferredItem<BlockItem> QI_LIQUID_COLLECTOR_BLOCK_ITEM = registerTooltipBlockItem("qi_liquid_collector_block", QI_LIQUID_COLLECTOR_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.wenyan.wenyan_addon.qi.liquid.QiLiquidCollectorBlockEntity>> QI_LIQUID_COLLECTOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "qi_liquid_collector_block",
            () -> new BlockEntityType<>(org.wenyan.wenyan_addon.qi.liquid.QiLiquidCollectorBlockEntity::new, QI_LIQUID_COLLECTOR_BLOCK.get())
    );
    public static final DeferredBlock<org.wenyan.wenyan_addon.qi.liquid.QiLiquidPurifierBlock> QI_LIQUID_PURIFIER_BLOCK = BLOCKS.registerBlock(
            "qi_liquid_purifier_block", org.wenyan.wenyan_addon.qi.liquid.QiLiquidPurifierBlock::new);
    public static final DeferredItem<BlockItem> QI_LIQUID_PURIFIER_BLOCK_ITEM = registerTooltipBlockItem("qi_liquid_purifier_block", QI_LIQUID_PURIFIER_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.wenyan.wenyan_addon.qi.liquid.QiLiquidPurifierBlockEntity>> QI_LIQUID_PURIFIER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "qi_liquid_purifier_block",
            () -> new BlockEntityType<>(org.wenyan.wenyan_addon.qi.liquid.QiLiquidPurifierBlockEntity::new, QI_LIQUID_PURIFIER_BLOCK.get())
    );

    public static final DeferredBlock<Block> QI_GATHERING_ARRAY_BLOCK = BLOCKS.registerBlock(
            "qi_gathering_array_block", org.wenyan.wenyan_addon.qi.gathering.QiGatheringArrayBlock::new);
    public static final DeferredItem<BlockItem> QI_GATHERING_ARRAY_BLOCK_ITEM = registerTooltipBlockItem("qi_gathering_array_block", QI_GATHERING_ARRAY_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.wenyan.wenyan_addon.qi.gathering.QiGatheringArrayBlockEntity>> QI_GATHERING_ARRAY_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "qi_gathering_array_block",
            () -> new BlockEntityType<>(org.wenyan.wenyan_addon.qi.gathering.QiGatheringArrayBlockEntity::new, QI_GATHERING_ARRAY_BLOCK.get())
    );

    public static final DeferredBlock<QiRitualBlock> QI_RITUAL_BLOCK = BLOCKS.registerBlock("qi_ritual_block", QiRitualBlock::new);
    public static final DeferredItem<BlockItem> QI_RITUAL_BLOCK_ITEM = registerTooltipBlockItem("qi_ritual_block", QI_RITUAL_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QiRitualBlockEntity>> QI_RITUAL_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "qi_ritual_block",
            () -> new BlockEntityType<>(QiRitualBlockEntity::new, QI_RITUAL_BLOCK.get())
    );

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
                output.accept(MUSIC_BLOCK_ITEM.get());
                output.accept(READ_WRITE_BLOCK_ITEM.get());
                output.accept(NAMING_BLOCK_ITEM.get());
                output.accept(PARTICLE_BLOCK_ITEM.get());
                output.accept(DYE_BLOCK_ITEM.get());
                output.accept(MARKER_BLOCK_ITEM.get());
//                output.accept(ENTITY_STATUS_BLOCK_ITEM.get());
//                output.accept(ENTITY_SPAWN_BLOCK_ITEM.get());
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
                output.accept(TIME_BLOCK_ITEM.get());
                output.accept(QI_BLOCK_ITEM.get());
                output.accept(QI_STORAGE_BLOCK_ITEM.get());
                output.accept(QI_VESSEL_ITEM.get());
                output.accept(YIN_CRYSTAL_ITEM.get());
                output.accept(YANG_CRYSTAL_ITEM.get());
                output.accept(SPIRIT_STONE_ITEM.get());
                output.accept(SPIRIT_STONE_IMPURE_ITEM.get());
                output.accept(SPIRIT_STONE_REFINED_ITEM.get());
                output.accept(QI_RITUAL_BLOCK_ITEM.get());
                output.accept(QI_LIQUID_COLLECTOR_BLOCK_ITEM.get());
                output.accept(QI_LIQUID_PURIFIER_BLOCK_ITEM.get());
                output.accept(QI_LIQUID_BOTTLE_ITEM.get());
                output.accept(QI_RESTORE_POTION_SMALL.get());
                output.accept(QI_RESTORE_POTION_MEDIUM.get());
                output.accept(QI_RESTORE_POTION_LARGE.get());
                output.accept(QI_RESTORE_POTION_SUSTAINED.get());
                output.accept(QI_GATHERING_ARRAY_BLOCK_ITEM.get());
            }).build());

    @SuppressWarnings("unused")
    public WenyanAddon(IEventBus modEventBus, ModContainer modContainer) {
        registerDerivedElements();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        PlayerQi.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(Capabilities::registerCapabilities);
        modEventBus.addListener(QiMarkEffects::register);
        modEventBus.addListener(QiRestorePotionEffects::register);
        modEventBus.addListener(org.wenyan.wenyan_addon.qi.damage.QiDamageTypes::register);
        net.neoforged.bus.api.IEventBus gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener((AddServerReloadListenersEvent event) ->
                event.addListener(Identifier.fromNamespaceAndPath(MODID, "qi_ritual_recipes"), new QiRitualRecipes()));
        Pong.register(modEventBus);
    }

    /**
     * 注册示例衍生属性
     */
    private static void registerDerivedElements() {
        ElementRegistry.register(new DerivedElement("ice", "冰", ElementType.WATER)
                .withColor(0xFF9AD5FF));
        ElementRegistry.register(new DerivedElement("lightning", "雷", ElementType.WOOD)
                .withRelation("ice", RelationType.COUNTER)
                .withColor(0xFFFFD700));
    }

    private static DeferredItem<BlockItem> registerTooltipBlockItem(String name, DeferredBlock<? extends Block> block) {
        return ITEMS.registerItem(name, properties -> new TooltipBlockItem(block.get(), properties, tooltipKey(name)), Item.Properties::useBlockDescriptionPrefix);
    }

    private static String tooltipKey(String name) {
        return "item." + MODID + "." + name + ".tooltip";
    }
}
