package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public record ToolMaterialDefinition(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier, HeatLevel minimumForgeHeat, @Nullable String translationKey) {
    public ToolMaterialDefinition {
        if (minimumForgeHeat == null) {
            minimumForgeHeat = defaultMinimumForgeHeat(category);
        }
    }

    public ToolMaterialDefinition(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier) {
        this(id, category, displayItem, tier, defaultMinimumForgeHeat(category), null);
    }

    public ToolMaterialDefinition(ResourceLocation id, MaterialCategory category, Item displayItem, Tier tier, @Nullable String translationKey) {
        this(id, category, displayItem, tier, defaultMinimumForgeHeat(category), translationKey);
    }

    public Component displayName() {
        return MaterialCatalog.displayName(id);
    }

    public static HeatLevel defaultMinimumForgeHeat(MaterialCategory category) {
        return category == MaterialCategory.METAL ? HeatLevel.HOT : HeatLevel.NONE;
    }
}
