package org.destroyermob.mobstoolforging.world;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.registry.ModItems;

public enum ForgeTemplate {
    SWORD_BLADE(ToolKind.SWORD, 5),
    SHOVEL_HEAD(ToolKind.SHOVEL, 5),
    PICKAXE_HEAD(ToolKind.PICKAXE, 5),
    AXE_HEAD(ToolKind.AXE, 5),
    HOE_HEAD(ToolKind.HOE, 5);

    private final ToolKind toolKind;
    private final int requiredHits;

    ForgeTemplate(ToolKind toolKind, int requiredHits) {
        this.toolKind = toolKind;
        this.requiredHits = requiredHits;
    }

    public String id() {
        return toolKind.partType();
    }

    public ToolKind toolKind() {
        return toolKind;
    }

    public Component displayName() {
        return toolKind.templateName();
    }

    public int requiredMaterials() {
        return toolKind.requiredMaterials();
    }

    public int requiredHits() {
        return requiredHits;
    }

    public ItemStack outputStack(ResourceLocation materialId) {
        return toolKind.createPart(materialId);
    }

    public static Optional<ForgeTemplate> byId(String id) {
        return Arrays.stream(values()).filter(template -> template.id().equals(id)).findFirst();
    }
}
