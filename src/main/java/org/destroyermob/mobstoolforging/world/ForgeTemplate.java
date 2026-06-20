package org.destroyermob.mobstoolforging.world;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;

public enum ForgeTemplate {
    SWORD_BLADE(ToolKind.SWORD, ToolPartData.SWORD_BLADE, ToolKind.SWORD.requiredMaterials(), 5),
    SWORD_GUARD(ToolKind.SWORD, ToolPartData.SWORD_GUARD, 1, 5),
    SHOVEL_HEAD(ToolKind.SHOVEL, ToolPartData.SHOVEL_HEAD, ToolKind.SHOVEL.requiredMaterials(), 5),
    PICKAXE_HEAD(ToolKind.PICKAXE, ToolPartData.PICKAXE_HEAD, ToolKind.PICKAXE.requiredMaterials(), 5),
    AXE_HEAD(ToolKind.AXE, ToolPartData.AXE_HEAD, ToolKind.AXE.requiredMaterials(), 5),
    HOE_HEAD(ToolKind.HOE, ToolPartData.HOE_HEAD, ToolKind.HOE.requiredMaterials(), 5);

    private final ToolKind toolKind;
    private final String partType;
    private final int requiredMaterials;
    private final int requiredHits;

    ForgeTemplate(ToolKind toolKind, String partType, int requiredMaterials, int requiredHits) {
        this.toolKind = toolKind;
        this.partType = partType;
        this.requiredMaterials = requiredMaterials;
        this.requiredHits = requiredHits;
    }

    public String id() {
        return partType;
    }

    public ResourceLocation registryId() {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, partType);
    }

    public ToolKind toolKind() {
        return toolKind;
    }

    public Component displayName() {
        return definition().displayName();
    }

    public int requiredMaterials() {
        return requiredMaterials;
    }

    public int requiredHits() {
        return requiredHits;
    }

    public ItemStack outputStack(ResourceLocation materialId) {
        return definition().outputStack(materialId);
    }

    public ForgeTemplateDefinition definition() {
        return new ForgeTemplateDefinition(
                registryId(),
                ToolConstructionData.toolType(toolKind),
                partType,
                requiredMaterials,
                requiredHits,
                "forge_template.mobstoolforging." + partType
        );
    }

    public static Optional<ForgeTemplate> byId(String id) {
        return Arrays.stream(values()).filter(template -> template.id().equals(id)).findFirst();
    }

    public static Optional<ForgeTemplateDefinition> definitionById(String id) {
        return ToolTypeRegistry.template(id)
                .or(() -> byId(id).map(ForgeTemplate::definition));
    }
}
