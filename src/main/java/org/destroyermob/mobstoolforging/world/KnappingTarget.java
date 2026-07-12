package org.destroyermob.mobstoolforging.world;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public enum KnappingTarget {
    SWORD_BLADE("sword_blade", ToolConstructionData.toolType(ToolKind.SWORD), ToolPartData.SWORD_BLADE, true),
    SWORD_GUARD("sword_guard", ToolConstructionData.toolType(ToolKind.SWORD), ToolPartData.SWORD_GUARD, false),
    SHOVEL_HEAD("shovel_head", ToolConstructionData.toolType(ToolKind.SHOVEL), ToolPartData.SHOVEL_HEAD, true),
    PICKAXE_HEAD("pickaxe_head", ToolConstructionData.toolType(ToolKind.PICKAXE), ToolPartData.PICKAXE_HEAD, true),
    AXE_HEAD("axe_head", ToolConstructionData.toolType(ToolKind.AXE), ToolPartData.AXE_HEAD, true),
    HOE_HEAD("hoe_head", ToolConstructionData.toolType(ToolKind.HOE), ToolPartData.HOE_HEAD, true),
    COOKING_KNIFE_HEAD(
            "cooking_knife_head",
            ResourceLocation.fromNamespaceAndPath("farmersdelight", "cooking_knife"),
            ToolPartData.COOKING_KNIFE_HEAD,
            true
    );

    private static final List<KnappingTarget> VALUES = List.of(values());

    private final String id;
    private final ResourceLocation toolType;
    private final String partType;
    private final boolean primaryPart;

    KnappingTarget(String id, ResourceLocation toolType, String partType, boolean primaryPart) {
        this.id = id;
        this.toolType = toolType;
        this.partType = partType;
        this.primaryPart = primaryPart;
    }

    public String id() {
        return id;
    }

    public String partType() {
        return partType;
    }

    public boolean primaryPart() {
        return primaryPart;
    }

    public Component displayName() {
        return Component.translatable("knapping_target.mobstoolforging." + id);
    }

    public ItemStack createOutput() {
        return ToolTypeRegistry.toolType(toolType)
                .map(definition -> definition.createPart(partType, MaterialCatalog.FLINT))
                .orElse(ItemStack.EMPTY);
    }

    public boolean available() {
        return ToolTypeRegistry.toolType(toolType).isPresent();
    }

    public KnappingTarget cycle(int delta) {
        List<KnappingTarget> available = VALUES.stream().filter(KnappingTarget::available).toList();
        if (available.isEmpty()) {
            return SWORD_BLADE;
        }
        int index = available.indexOf(this);
        int next = Math.floorMod((index < 0 ? 0 : index) + delta, available.size());
        return available.get(next);
    }

    public static KnappingTarget byId(String id) {
        return Arrays.stream(values())
                .filter(target -> target.id.equals(id))
                .filter(KnappingTarget::available)
                .findFirst()
                .orElse(SWORD_BLADE);
    }

    public static Optional<KnappingTarget> byPartData(ToolPartData data) {
        if (data == null || !MaterialCatalog.FLINT.equals(data.materialId())) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(target -> target.partType.equals(data.partType()))
                .filter(KnappingTarget::available)
                .findFirst();
    }
}
