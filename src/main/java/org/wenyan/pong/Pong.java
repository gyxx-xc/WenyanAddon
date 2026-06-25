package org.wenyan.pong;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import org.wenyan.pong.setup.PongRegistration;
import org.slf4j.Logger;

public final class Pong {
    public static final String MODID = "pong";
    public static final Logger LOGGER = LogUtils.getLogger();

    private Pong() {
    }

    public static void register(IEventBus modEventBus) {
        PongRegistration.register(modEventBus);
    }
}
