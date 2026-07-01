package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;

public final class ArmorForgeAttachment {
    private ArmorForgeAttachment() {
    }

    public static boolean isAttachmentTemplate(@Nullable ForgeTemplateDefinition template) {
        return template != null && isAttachmentTemplate(template.id());
    }

    public static boolean isAttachmentTemplate(@Nullable ResourceLocation templateId) {
        return ToolTypeRegistry.HELMET_COMB_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.HELMET_VISOR_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.LEGGINGS_KNEES_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.LEGGINGS_TASSETS_TEMPLATE.equals(templateId);
    }

    public static boolean isBaseArmorTemplate(@Nullable ResourceLocation templateId) {
        return ToolTypeRegistry.HELMET_SKULL_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.CHESTPLATE_CHAINMAIL_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.LEGGINGS_LEGS_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.BOOTS_FEET_TEMPLATE.equals(templateId);
    }

    public static boolean isAttachmentStation(WorkstationKind workstationKind) {
        return workstationKind == WorkstationKind.TOOL_FORGE || workstationKind == WorkstationKind.LAPIDARY_TABLE;
    }

    public static boolean isArmorStack(ItemStack stack) {
        return stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) != null;
    }

    public static boolean isCompatibleTarget(ForgeTemplateDefinition template, ItemStack stack) {
        return isCompatibleTarget(template.id(), stack);
    }

    public static boolean isCompatibleTarget(ResourceLocation templateId, ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null) {
            return false;
        }
        if (ToolTypeRegistry.HELMET_COMB_TEMPLATE.equals(templateId) || ToolTypeRegistry.HELMET_VISOR_TEMPLATE.equals(templateId)) {
            return stack.is(ModItems.MODULAR_HELMET.get()) && ArmorConstructionData.HELMET_TYPE.equals(construction.armorType());
        }
        if (ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE.equals(templateId)) {
            return stack.is(ModItems.MODULAR_CHESTPLATE.get()) && construction.isChestplate();
        }
        if (ToolTypeRegistry.LEGGINGS_KNEES_TEMPLATE.equals(templateId) || ToolTypeRegistry.LEGGINGS_TASSETS_TEMPLATE.equals(templateId)) {
            return stack.is(ModItems.MODULAR_LEGGINGS.get()) && ArmorConstructionData.LEGGINGS_TYPE.equals(construction.armorType());
        }
        return false;
    }

    public static ItemStack apply(ItemStack target, ResourceLocation templateId, ResourceLocation attachmentMaterial) {
        ArmorConstructionData construction = target.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !isCompatibleTarget(templateId, target)) {
            return ItemStack.EMPTY;
        }
        ItemStack output = target.copyWithCount(1);
        output.set(ModDataComponents.ARMOR_CONSTRUCTION.get(), updatedConstruction(construction, templateId, attachmentMaterial));
        return output;
    }

    public static ItemStack baseOutputStack(ResourceLocation templateId, ResourceLocation materialId) {
        if (ToolTypeRegistry.HELMET_SKULL_TEMPLATE.equals(templateId)) {
            return ModItems.MODULAR_HELMET.get().create(materialId, Optional.empty(), Optional.empty());
        }
        if (ToolTypeRegistry.CHESTPLATE_CHAINMAIL_TEMPLATE.equals(templateId)) {
            return ModItems.MODULAR_CHESTPLATE.get().createChainmail();
        }
        if (ToolTypeRegistry.LEGGINGS_LEGS_TEMPLATE.equals(templateId)) {
            return ModItems.MODULAR_LEGGINGS.get().create(materialId);
        }
        if (ToolTypeRegistry.BOOTS_FEET_TEMPLATE.equals(templateId)) {
            return ModItems.MODULAR_BOOTS.get().create(materialId);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack previewTargetStack(ResourceLocation templateId) {
        if (ToolTypeRegistry.HELMET_COMB_TEMPLATE.equals(templateId) || ToolTypeRegistry.HELMET_VISOR_TEMPLATE.equals(templateId)) {
            return ModItems.MODULAR_HELMET.get().create(MaterialCatalog.IRON, Optional.empty(), Optional.empty());
        }
        if (ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE.equals(templateId)) {
            return ModItems.MODULAR_CHESTPLATE.get().createChainmail();
        }
        if (ToolTypeRegistry.LEGGINGS_KNEES_TEMPLATE.equals(templateId) || ToolTypeRegistry.LEGGINGS_TASSETS_TEMPLATE.equals(templateId)) {
            return ModItems.MODULAR_LEGGINGS.get().create(MaterialCatalog.IRON);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack previewOutputStack(ResourceLocation templateId, ResourceLocation attachmentMaterial) {
        ItemStack target = previewTargetStack(templateId);
        return target.isEmpty() ? ItemStack.EMPTY : apply(target, templateId, attachmentMaterial);
    }

    public static Component statusMessage(ToolForgeBlockEntity forge) {
        ForgeTemplateDefinition template = forge.template();
        if (template == null || !isAttachmentTemplate(template) || !forge.hasArmorAttachmentTarget()) {
            return Component.translatable("message.mobstoolforging.armor_attachment_needs_target");
        }
        ItemStack target = forge.armorAttachmentTarget();
        ResourceLocation materialId = forge.materialId();
        if (materialId == null) {
            if (forge.workstationKind() == WorkstationKind.LAPIDARY_TABLE && !forge.hasAbrasive()) {
                return Component.translatable("message.mobstoolforging.lapidary_needs_abrasive");
            }
            return Component.translatable("message.mobstoolforging.armor_attachment_needs_material", template.displayName());
        }
        ArmorConstructionData construction = target.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        Optional<ResourceLocation> existingMaterial = construction == null ? Optional.empty() : existingMaterial(construction, template.id());
        if (existingMaterial.isPresent()) {
            return Component.translatable(
                    "message.mobstoolforging.armor_attachment_replacing",
                    MaterialCatalog.displayName(existingMaterial.get()),
                    template.displayName(),
                    MaterialCatalog.displayName(materialId),
                    template.displayName(),
                    forge.hitCount(),
                    template.requiredHits()
            );
        }
        if (forge.workstationKind() == WorkstationKind.LAPIDARY_TABLE) {
            return Component.translatable(
                    "message.mobstoolforging.armor_attachment_setting",
                    MaterialCatalog.displayName(materialId),
                    template.displayName(),
                    target.getHoverName(),
                    forge.hitCount(),
                    template.requiredHits()
            );
        }
        return Component.translatable(
                "message.mobstoolforging.armor_attachment_forging",
                MaterialCatalog.displayName(materialId),
                template.displayName(),
                target.getHoverName(),
                forge.hitCount(),
                template.requiredHits()
        );
    }

    private static Optional<ResourceLocation> existingMaterial(ArmorConstructionData construction, ResourceLocation templateId) {
        if (ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE.equals(templateId)) {
            return construction.chestplatePlateMaterial();
        }
        if (ToolTypeRegistry.HELMET_COMB_TEMPLATE.equals(templateId) || ToolTypeRegistry.LEGGINGS_KNEES_TEMPLATE.equals(templateId)) {
            return construction.combMaterial();
        }
        if (ToolTypeRegistry.HELMET_VISOR_TEMPLATE.equals(templateId) || ToolTypeRegistry.LEGGINGS_TASSETS_TEMPLATE.equals(templateId)) {
            return construction.visorMaterial();
        }
        return Optional.empty();
    }

    private static ArmorConstructionData updatedConstruction(ArmorConstructionData construction, ResourceLocation templateId, ResourceLocation attachmentMaterial) {
        Optional<ResourceLocation> firstOptional = construction.combMaterial();
        Optional<ResourceLocation> secondOptional = construction.visorMaterial();
        if (ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE.equals(templateId)) {
            return new ArmorConstructionData(
                    construction.armorType(),
                    MaterialCatalog.IRON,
                    Optional.of(attachmentMaterial),
                    Optional.of(MaterialCatalog.IRON),
                    construction.quality()
            );
        } else if (ToolTypeRegistry.HELMET_COMB_TEMPLATE.equals(templateId) || ToolTypeRegistry.LEGGINGS_KNEES_TEMPLATE.equals(templateId)) {
            firstOptional = Optional.of(attachmentMaterial);
        } else if (ToolTypeRegistry.HELMET_VISOR_TEMPLATE.equals(templateId) || ToolTypeRegistry.LEGGINGS_TASSETS_TEMPLATE.equals(templateId)) {
            secondOptional = Optional.of(attachmentMaterial);
        }
        return new ArmorConstructionData(construction.armorType(), construction.skullMaterial(), firstOptional, secondOptional, construction.quality());
    }
}
