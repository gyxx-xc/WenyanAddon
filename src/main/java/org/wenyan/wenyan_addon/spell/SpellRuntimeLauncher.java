package org.wenyan.wenyan_addon.spell;

import net.minecraft.server.level.ServerPlayer;

/**
 * 法术运行启动器：在主线程创建并注册运行实例。
 */
public final class SpellRuntimeLauncher {
    private SpellRuntimeLauncher() {
    }

    /**
     * 启动运行：创建 SpellRun 并注册到 SpellRunManager（替换玩家已有的运行）。
     * 必须在主线程调用。
     */
    public static void launch(ServerPlayer player, SpellEnvironment env, String spellCode, int step) {
        SpellRun run = new SpellRun(player, env, spellCode, step);
        SpellRunManager.getInstance().register(player, run);
        run.launch();
    }
}