package org.wenyan.wenyan_addon;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.pongdev.pong.Pong;
import org.pongdev.pong.setup.PongRegistration;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(WenyanAddon.MODID)
public class WenyanAddon {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "wenyan_addon";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold Blocks which will all be registered under the "wenyan_addon" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "wenyan_addon" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "wenyan_addon" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final ResourceKey<CreativeModeTab> WENYAN_NATURE_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("wenyan_nature", "wenyan_nature")
    );

    // Creates a new Block with the id "wenyan_addon:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "wenyan_addon:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates the Wenyan Addon creative tab after Wenyan Nature.
    @SuppressWarnings("unused") public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.wenyan_addon")) //The language key for the title of your CreativeModeTab
            .withTabsAfter(WENYAN_NATURE_TAB)
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> PongRegistration.CHAMPAGNE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(PongRegistration.CHAMPAGNE.get());
                output.accept(PongRegistration.CHAMPAGNE_SABRE.get());
                output.accept(PongRegistration.GOBLET.get());
                output.accept(PongRegistration.CHAMPAGNE_RACK_ITEM.get());
                output.accept(PongRegistration.PLUG.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public WenyanAddon(IEventBus modEventBus, ModContainer modContainer) {

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        Pong.register(modEventBus);
    }
}
