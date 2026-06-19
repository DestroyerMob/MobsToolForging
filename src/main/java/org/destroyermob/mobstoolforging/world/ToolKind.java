package org.destroyermob.mobstoolforging.world;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import org.destroyermob.mobstoolforging.item.ModularToolItem;
import org.destroyermob.mobstoolforging.item.ModularToolPartItem;
import org.destroyermob.mobstoolforging.registry.ModItems;

public enum ToolKind {
    SWORD("sword", "sword_blade", 2),
    SHOVEL("shovel", "shovel_head", 1),
    PICKAXE("pickaxe", "pickaxe_head", 3),
    AXE("axe", "axe_head", 3),
    HOE("hoe", "hoe_head", 2);

    private final String id;
    private final String partType;
    private final int requiredMaterials;

    ToolKind(String id, String partType, int requiredMaterials) {
        this.id = id;
        this.partType = partType;
        this.requiredMaterials = requiredMaterials;
    }

    public String id() {
        return id;
    }

    public String partType() {
        return partType;
    }

    public int requiredMaterials() {
        return requiredMaterials;
    }

    public Component templateName() {
        return Component.translatable("forge_template.mobstoolforging." + partType);
    }

    public Component partName(ResourceLocation materialId) {
        return Component.translatable("item.mobstoolforging.material_" + partType, MaterialCatalog.displayName(materialId));
    }

    public Component toolName(ResourceLocation materialId) {
        return Component.translatable("item.mobstoolforging.material_" + id, MaterialCatalog.displayName(materialId));
    }

    public ItemStack createPart(ResourceLocation materialId) {
        return partItem().get().createPart(materialId);
    }

    public ItemStack createTool(ResourceLocation materialId, ItemStack handle) {
        Item item = toolItem().get();
        if (item instanceof ModularToolItem modularTool) {
            return modularTool.create(materialId, handle);
        }
        return ItemStack.EMPTY;
    }

    public DeferredItem<ModularToolPartItem> partItem() {
        return switch (this) {
            case SWORD -> ModItems.SWORD_BLADE;
            case SHOVEL -> ModItems.SHOVEL_HEAD;
            case PICKAXE -> ModItems.PICKAXE_HEAD;
            case AXE -> ModItems.AXE_HEAD;
            case HOE -> ModItems.HOE_HEAD;
        };
    }

    public DeferredItem<? extends Item> toolItem() {
        return switch (this) {
            case SWORD -> ModItems.SWORD;
            case SHOVEL -> ModItems.SHOVEL;
            case PICKAXE -> ModItems.PICKAXE;
            case AXE -> ModItems.AXE;
            case HOE -> ModItems.HOE;
        };
    }
}
