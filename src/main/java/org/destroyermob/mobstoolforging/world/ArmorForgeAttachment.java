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
        return ToolTypeRegistry.HELMET_PLATE_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.LEGGINGS_PLATE_TEMPLATE.equals(templateId)
                || ToolTypeRegistry.BOOTS_PLATE_TEMPLATE.equals(templateId);
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
        if (construction.hasLeatherBase()) {
            return false;
        }
        if (ToolTypeRegistry.HELMET_PLATE_TEMPLATE.equals(templateId)) {
            return stack.is(ModItems.MODULAR_HELMET.get()) && ArmorConstructionData.HELMET_TYPE.equals(construction.armorType());
        }
        if (ToolTypeRegistry.CHESTPLATE_BODY_TEMPLATE.equals(templateId)) {
            return stack.is(ModItems.MODULAR_CHESTPLATE.get()) && construction.isChestplate();
        }
        if (ToolTypeRegistry.LEGGINGS_PLATE_TEMPLATE.equals(templateId)) {
            return stack.is(ModItems.MODULAR_LEGGINGS.get()) && ArmorConstructionData.LEGGINGS_TYPE.equals(construction.armorType());
        }
        if (ToolTypeRegistry.BOOTS_PLATE_TEMPLATE.equals(templateId)) {
            return stack.is(ModItems.MODULAR_BOOTS.get()) && ArmorConstructionData.BOOTS_TYPE.equals(construction.armorType());
        }
        return false;
    }

    public static ItemStack apply(ItemStack target, ResourceLocation templateId, ResourceLocation attachmentMaterial) {
        return apply(target, templateId, attachmentMaterial, ArmorConstructionData.DEFAULT_QUALITY);
    }

    public static ItemStack apply(ItemStack target, ResourceLocation templateId, ResourceLocation attachmentMaterial, int attachmentQuality) {
        ArmorConstructionData construction = target.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !isCompatibleTarget(templateId, target)) {
            return ItemStack.EMPTY;
        }
        ItemStack output = target.copyWithCount(1);
        ArmorConstructionData updatedConstruction = updatedConstruction(construction, templateId, attachmentMaterial, attachmentQuality);
        output.set(ModDataComponents.ARMOR_CONSTRUCTION.get(), updatedConstruction);
        ArmorStatsCatalog.applyPreservingDamage(output, updatedConstruction);
        return output;
    }

    public static Component statusMessage(ToolForgeBlockEntity forge) {
        ForgeTemplateDefinition template = forge.template();
        if (template == null || !isAttachmentTemplate(template) || !forge.hasArmorAttachmentTarget()) {
            return Component.translatable("message.mobstoolforging.armor_attachment_needs_target");
        }
        ItemStack target = forge.armorAttachmentTarget();
        ResourceLocation materialId = forge.materialId();
        if (materialId == null) {
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
        if (ToolTypeRegistry.HELMET_PLATE_TEMPLATE.equals(templateId)) {
            return construction.helmetPlateMaterial();
        }
        if (ToolTypeRegistry.LEGGINGS_PLATE_TEMPLATE.equals(templateId)) {
            return construction.leggingsPlateMaterial();
        }
        if (ToolTypeRegistry.BOOTS_PLATE_TEMPLATE.equals(templateId)) {
            return construction.bootsPlateMaterial();
        }
        return Optional.empty();
    }

    private static ArmorConstructionData updatedConstruction(ArmorConstructionData construction, ResourceLocation templateId, ResourceLocation attachmentMaterial, int attachmentQuality) {
        int quality = combinedQuality(construction, attachmentQuality);
        return new ArmorConstructionData(construction.armorType(), primaryMaterial(construction), Optional.of(attachmentMaterial), Optional.empty(), quality);
    }

    private static int combinedQuality(ArmorConstructionData construction, int attachmentQuality) {
        return ForgingQuality.clampScore(Math.round((construction.quality() + ForgingQuality.clampScore(attachmentQuality)) / 2.0F));
    }

    private static ResourceLocation primaryMaterial(ArmorConstructionData construction) {
        if (ArmorConstructionData.HELMET_TYPE.equals(construction.armorType())) {
            return construction.helmetChainmailMaterial();
        }
        if (ArmorConstructionData.LEGGINGS_TYPE.equals(construction.armorType())) {
            return construction.leggingsChainmailMaterial();
        }
        if (ArmorConstructionData.BOOTS_TYPE.equals(construction.armorType())) {
            return construction.bootsChainmailMaterial();
        }
        return construction.chestplateChainmailMaterial();
    }
}
