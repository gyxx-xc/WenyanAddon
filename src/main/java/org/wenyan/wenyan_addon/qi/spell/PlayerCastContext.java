package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.judou.api.exec.structure.IHandleContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * 玩家施法上下文：以玩家为施法主体（法术剑运行时）。
 * 玩家版函数以此类型作为第一个参数，通过 {@link #player()} 获取施法者，
 * {@link #level()} / {@link #pos()} 提供施法世界与锚点（玩家脚底）。
 */
public record PlayerCastContext(ServerPlayer player) implements IHandleContext {

    public Level level() {
        return player.level();
    }

    public BlockPos pos() {
        return player.blockPosition();
    }
}