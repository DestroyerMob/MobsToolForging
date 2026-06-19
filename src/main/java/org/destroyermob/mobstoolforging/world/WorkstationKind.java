package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum WorkstationKind {
    TOOL_FORGE(MaterialCategory.METAL, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundEvents.ANVIL_LAND, ParticleTypes.CRIT, "message.mobstoolforging.use_lapidary_table"),
    LAPIDARY_TABLE(MaterialCategory.GEM, SoundEvents.AMETHYST_BLOCK_CHIME, SoundEvents.GRINDSTONE_USE, ParticleTypes.CRIT, "message.mobstoolforging.use_tool_forge");

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
}
