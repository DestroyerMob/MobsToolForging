package org.destroyermob.mobstoolforging;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MobsToolForgingConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.BooleanValue DEBUG_TEMPLATE_SELECTOR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        DEBUG_TEMPLATE_SELECTOR = builder
                .comment("When true, sneak-right-clicking a workstation can open the old template selector GUI for debugging.")
                .define("debugTemplateSelector", false);
        COMMON_SPEC = builder.build();
    }

    private MobsToolForgingConfig() {
    }
}
