package org.wenyan.wenyan_addon.mixin_util;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class CasterContext {
    private static final ThreadLocal<ServerPlayer> CURRENT_CASTER = new ThreadLocal<>();

    private CasterContext() {
    }

    public static void set(ServerPlayer caster) {
        CURRENT_CASTER.set(caster);
    }

    public static ServerPlayer get() {
        return CURRENT_CASTER.get();
    }

    public static void clear() {
        CURRENT_CASTER.remove();
    }
}
