package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;

public enum WorkstationKind {
    CRUDE_ANVIL(MaterialCategory.METAL, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundEvents.ANVIL_LAND, ParticleTypes.CRIT, "message.mobstoolforging.use_lapidary_table"),
    TOOL_FORGE(MaterialCategory.METAL, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundEvents.ANVIL_LAND, ParticleTypes.CRIT, "message.mobstoolforging.use_lapidary_table"),
    LAPIDARY_TABLE(MaterialCategory.GEM, SoundEvents.AMETHYST_BLOCK_CHIME, SoundEvents.GRINDSTONE_USE, ParticleTypes.CRIT, "message.mobstoolforging.use_tool_forge"),
    TOOLMAKERS_BENCH(MaterialCategory.METAL, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundEvents.UI_STONECUTTER_SELECT_RECIPE, ParticleTypes.CRIT, "message.mobstoolforging.use_toolmakers_bench");

    private final MaterialCategory materialCategory;
    private final SoundEvent placeSound;
    private final SoundEvent workSound;
    private final ParticleOptions workParticle;
    private final String wrongStationMessage;

    WorkstationKind(MaterialCategory materialCategory, SoundEvent placeSound, SoundEvent workSound, ParticleOptions workParticle, String wrongStationMessage) {
        this.materialCategory = materialCategory;
        this.placeSound = placeSound;
        this.workSound = workSound;
        this.workParticle = workParticle;
        this.wrongStationMessage = wrongStationMessage;
    }

    public MaterialCategory materialCategory() {
        return materialCategory;
    }

    public SoundEvent placeSound() {
        return placeSound;
    }

    public SoundEvent workSound() {
        return workSound;
    }

    public ParticleOptions workParticle() {
        return workParticle;
    }

    public String wrongStationMessage() {
        return wrongStationMessage;
    }

    public boolean isSmithingAnvilLike() {
        return this == TOOL_FORGE || this == CRUDE_ANVIL;
    }

    public ForgingQuality maxQuality() {
        return switch (this) {
            case CRUDE_ANVIL -> MobsToolForgingConfig.crudeAnvilMaxQuality();
            case LAPIDARY_TABLE -> MobsToolForgingConfig.lapidaryTableMaxQuality();
            case TOOL_FORGE, TOOLMAKERS_BENCH -> MobsToolForgingConfig.smithingAnvilMaxQuality();
        };
    }

    public int setupQualityBonus() {
        return switch (this) {
            case CRUDE_ANVIL -> -12;
            case TOOL_FORGE -> 6;
            case LAPIDARY_TABLE -> 2;
            case TOOLMAKERS_BENCH -> 0;
        };
    }
}
