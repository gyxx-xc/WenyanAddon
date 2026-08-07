package org.wenyan.wenyan_addon.mixin_util;

import net.minecraft.server.level.ServerPlayer;

public interface RunnerBlockEntityCasterAccessor {
    ServerPlayer getCaster();

    void setCaster(ServerPlayer caster);
}
