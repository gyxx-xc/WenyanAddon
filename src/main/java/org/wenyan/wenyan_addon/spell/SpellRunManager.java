package org.wenyan.wenyan_addon.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.wenyan.wenyan_addon.WenyanAddon;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 法术运行管理器：管理所有进行中的法术运行（每玩家至多一个）。
 * 服务端 tick 驱动，负责熔断、剑离手中止与错误派发。
 */
@EventBusSubscriber(modid = WenyanAddon.MODID)
public final class SpellRunManager {
    private static final SpellRunManager INSTANCE = new SpellRunManager();

    private final Map<UUID, SpellRun> runs = new ConcurrentHashMap<>();

    private SpellRunManager() {
    }

    public static SpellRunManager getInstance() {
        return INSTANCE;
    }

    /**
     * 尝试施放法术：读取剑上的咒术代码，异步编译并扫描环境，随后在主线程启动运行。
     */
    public void tryCast(ServerPlayer player, ItemStack stack) {
        String code = SpellCodeHelper.readCode(stack);
        if (code == null || code.isBlank()) {
            player.sendSystemMessage(Component.literal("此剑尚未写入法术"));
            return;
        }
        int step = SpellCodeHelper.stepOf(stack);
        // 主线程读取背包快照（背包访问非线程安全），异步线程仅编译
        SpellEnvironmentScanner.ScanResult scan = SpellEnvironmentScanner.scan(player);
        abort(player.getUUID());
        SpellAsyncExecutor.submit(
                () -> compile(code),
                bytecode -> {
                    if (player.isRemoved() || !player.isAlive()) {
                        return;
                    }
                    SpellRuntimeLauncher.launch(player, new SpellEnvironment(bytecode, scan.scrollPackages(), scan.devicePackages()), code, step);
                },
                () -> compile(code)
        );
    }

    private static indi.wenyan.judou.api.compile.IWenyanBytecode compile(String code) {
        return new indi.wenyan.judou.api.compile.WenyanCompiler().compile(code).bytecode();
    }

    /**
     * 注册运行（替换该玩家已有的运行）。
     */
    public void register(ServerPlayer player, SpellRun run) {
        abort(player.getUUID());
        runs.put(player.getUUID(), run);
    }

    /**
     * 中止某玩家的运行（如重新施法、服务端停机）。
     */
    public void abort(ServerPlayer player) {
        abort(player.getUUID());
    }

    private void abort(UUID uuid) {
        SpellRun old = runs.remove(uuid);
        if (old != null) {
            old.stop();
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (server == null) {
            return;
        }
        INSTANCE.runs.entrySet().removeIf(entry -> {
            SpellRun run = entry.getValue();
            ServerPlayer player = run.player();
            if (player == null || player.isRemoved() || !player.isAlive()) {
                run.stop();
                return true;
            }
            if (!SpellCodeHelper.isSwordPresent(player, run.spellCode())) {
                run.stop();
                player.sendSystemMessage(Component.literal("术剑已离手，法术中止").withStyle(net.minecraft.ChatFormatting.GOLD));
                return true;
            }
            run.tick();
            return run.isFinished();
        });
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        INSTANCE.runs.values().forEach(SpellRun::stop);
        INSTANCE.runs.clear();
        SpellAsyncExecutor.shutdown();
    }
}