package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public record ToolMaterialDefinition(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier, @Nullable String translationKey) {
    public ToolMaterialDefinition(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier) {
        this(id, category, displayItem, tier, null);
    }

    public Component displayName() {
        return MaterialCatalog.displayName(id);
    }
}
