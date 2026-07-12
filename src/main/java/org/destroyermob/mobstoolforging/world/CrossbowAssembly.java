package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;

/** Validation and construction for a body, limbs, and a raw string component. */
public final class CrossbowAssembly {
    public static final ResourceLocation TOOL_TYPE = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "crossbow");

    private CrossbowAssembly() {
    }

    public static boolean isCrossbow(ToolConstructionData construction) {
        return construction != null && TOOL_TYPE.equals(construction.toolType());
    }

    public static boolean isCrossbowPart(ItemStack stack) {
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        return data != null && ((stack.is(ModItems.CROSSBOW_BODY.get()) && ToolPartData.CROSSBOW_BODY.equals(data.partType()))
                || (stack.is(ModItems.CROSSBOW_LIMBS.get()) && ToolPartData.CROSSBOW_LIMBS.equals(data.partType())));
    }

    public static boolean isCrossbowAssemblyComponent(ItemStack stack) {
        return isCrossbowPart(stack) || stringMaterial(stack).isPresent();
    }

    public static ItemStack assemble(List<ItemStack> stacks) {
        if (stacks.size() != 3) {
            return ItemStack.EMPTY;
        }
        ItemStack body = ItemStack.EMPTY;
        ItemStack limbs = ItemStack.EMPTY;
        ItemStack string = ItemStack.EMPTY;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty() || WorkpieceHeat.hasHeat(stack)) {
                return ItemStack.EMPTY;
            }
            if (stringMaterial(stack).isPresent()) {
                if (!string.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                string = stack;
                continue;
            }
            if (!isCrossbowPart(stack)) {
                return ItemStack.EMPTY;
            }
            ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
            if (data != null && ToolPartData.CROSSBOW_BODY.equals(data.partType())) {
                if (!body.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                body = stack;
            } else if (data != null && ToolPartData.CROSSBOW_LIMBS.equals(data.partType())) {
                if (!limbs.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                limbs = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        ToolPartData bodyData = body.get(ModDataComponents.TOOL_PART.get());
        ToolPartData limbData = limbs.get(ModDataComponents.TOOL_PART.get());
        ResourceLocation stringMaterial = stringMaterial(string).orElse(null);
        if (bodyData == null || limbData == null || stringMaterial == null
                || !isBodyMaterial(bodyData.materialId())
                || !isLimbMaterial(limbData.materialId())
                || !isStringMaterial(stringMaterial)) {
            return ItemStack.EMPTY;
        }

        int quality = Math.round((bodyData.effectiveQuality() + limbData.effectiveQuality()) / 2.0F);
        ToolConstructionData construction = new ToolConstructionData(
                TOOL_TYPE,
                limbData.materialId(),
                Optional.empty(),
                MaterialCatalog.WOOD,
                Optional.of(stringMaterial),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                quality
        );
        ItemStack crossbow = new ItemStack(ModItems.CROSSBOW.get());
        crossbow.set(ModDataComponents.TOOL_CONSTRUCTION.get(), construction);
        crossbow.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(List.of(body, limbs, string)));
        CompositeAffixCompatibility.syncCompatibilityMirror(crossbow);
        return crossbow;
    }

    public static Optional<List<ItemStack>> disassemble(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (!isCrossbow(construction)) {
            return Optional.empty();
        }
        ToolAssemblyParts storedParts = stack.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (storedParts != null && !storedParts.stacks().isEmpty()) {
            return Optional.of(storedParts.copyStacks());
        }
        ResourceLocation stringMaterial = construction.guardMaterial().orElse(MaterialCatalog.SPIDER_SILK);
        List<ItemStack> parts = new ArrayList<>();
        parts.add(ModItems.CROSSBOW_BODY.get().createPart(MaterialCatalog.WOOD, construction.quality()));
        parts.add(ModItems.CROSSBOW_LIMBS.get().createPart(construction.headMaterial(), construction.quality()));
        parts.add(stringStack(stringMaterial));
        return Optional.of(List.copyOf(parts));
    }

    private static boolean isLimbMaterial(ResourceLocation material) {
        if (MaterialCatalog.OAK.equals(material)) {
            return true;
        }
        return MaterialCatalog.definition(material)
                .map(definition -> definition.category() == MaterialCategory.METAL || definition.category() == MaterialCategory.GEM)
                .orElse(false);
    }

    private static boolean isBodyMaterial(ResourceLocation material) {
        // Oak is accepted for bodies saved before the generic wood material was introduced.
        return MaterialCatalog.WOOD.equals(material) || MaterialCatalog.OAK.equals(material);
    }

    private static boolean isStringMaterial(ResourceLocation material) {
        return MaterialCatalog.SPIDER_SILK.equals(material) || MaterialCatalog.PLANT_FIBER.equals(material);
    }

    private static Optional<ResourceLocation> stringMaterial(ItemStack stack) {
        if (stack.is(Items.STRING)) {
            return Optional.of(MaterialCatalog.SPIDER_SILK);
        }
        if (stack.is(ModItems.PLANT_FIBER.get())) {
            return Optional.of(MaterialCatalog.PLANT_FIBER);
        }
        return Optional.empty();
    }

    private static ItemStack stringStack(ResourceLocation material) {
        return MaterialCatalog.PLANT_FIBER.equals(material)
                ? new ItemStack(ModItems.PLANT_FIBER.get())
                : new ItemStack(Items.STRING);
    }
}
