package com.fiisadev.copycats_sable_light_fix;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("copycats_sable_light_fix")
public class CopycatsSableLightFix {

    public static final Logger LOGGER = LogManager.getLogger();

    public CopycatsSableLightFix(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
