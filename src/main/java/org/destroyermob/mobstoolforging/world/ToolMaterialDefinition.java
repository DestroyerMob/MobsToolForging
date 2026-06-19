package org.destroyermob.mobstoolforging.world;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public record ToolMaterialDefinition(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier) {
    public Component displayName() {
        return MaterialCatalog.displayName(id);
    }
}
