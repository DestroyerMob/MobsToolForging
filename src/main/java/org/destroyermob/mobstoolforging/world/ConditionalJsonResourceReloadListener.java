package org.destroyermob.mobstoolforging.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Base class for custom JSON registries which support NeoForge's standard
 * {@code neoforge:conditions} member.
 */
abstract class ConditionalJsonResourceReloadListener extends SimpleJsonResourceReloadListener {
    private final ConditionalOps<JsonElement> conditionalOps;

    protected ConditionalJsonResourceReloadListener(
            Gson gson,
            String directory,
            ICondition.IContext conditionContext,
            HolderLookup.Provider registryLookup
    ) {
        super(gson, directory);
        conditionalOps = new ConditionalOps<>(registryLookup.createSerializationContext(JsonOps.INSTANCE), conditionContext);
    }

    protected final boolean conditionsMatch(JsonElement element) {
        return ICondition.conditionsMatched(conditionalOps, element);
    }
}
