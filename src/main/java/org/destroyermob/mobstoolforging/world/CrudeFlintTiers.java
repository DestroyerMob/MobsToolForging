package org.destroyermob.mobstoolforging.world;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModTags;

public final class CrudeFlintTiers {
    public static final Tier FLINT = new SimpleTier(
            ModTags.Blocks.INCORRECT_FOR_FLINT_TOOL,
            48,
            2.7F,
            0.5F,
            2,
            () -> Ingredient.of(ModItems.FLINT_SHARD.get())
    );

    private CrudeFlintTiers() {
    }
}
