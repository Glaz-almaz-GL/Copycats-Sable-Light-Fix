package com.fiisadev.copycats_sable_light_fix;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;

    static {
        BUILDER.push("copycats_sable_light_fix");
        ENABLED = BUILDER
                .comment("Enable or disable the AO fix for copycat models")
                .define("enabled", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
