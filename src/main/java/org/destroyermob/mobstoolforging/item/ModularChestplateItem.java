package org.destroyermob.mobstoolforging.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.ArmorStatsCatalog;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.MetallurgyTooltips;

public class ModularChestplateItem extends ArmorItem implements ModularArmorItem {
    public ModularChestplateItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.CHESTPLATE, properties);
    }

    public ItemStack createBase(ResourceLocation baseMaterial) {
        return createBase(baseMaterial, ArmorConstructionData.DEFAULT_QUALITY);
    }

    public ItemStack createBase(ResourceLocation baseMaterial, int quality) {
        ArmorConstructionData construction = ArmorConstructionData.chestplateBase(baseMaterial, quality);
        ItemStack stack = new ItemStack(this);
        stack.set(ModDataComponents.ARMOR_CONSTRUCTION.get(), construction);
        ArmorStatsCatalog.apply(stack, construction);
        return stack;
    }

    public ItemStack create(ResourceLocation bodyMaterial) {
        return create(bodyMaterial, ArmorConstructionData.DEFAULT_QUALITY);
    }

    public ItemStack create(ResourceLocation bodyMaterial, int quality) {
        ArmorConstructionData construction = ArmorConstructionData.chestplate(bodyMaterial, quality);
        ItemStack stack = new ItemStack(this);
        stack.set(ModDataComponents.ARMOR_CONSTRUCTION.get(), construction);
        ArmorStatsCatalog.apply(stack, construction);
        return stack;
    }

    public ItemStack createChainmail() {
        return createChainmail(ArmorConstructionData.DEFAULT_QUALITY);
    }

    public ItemStack createChainmail(int quality) {
        ArmorConstructionData construction = ArmorConstructionData.chainmailChestplate(quality);
        ItemStack stack = new ItemStack(this);
        stack.set(ModDataComponents.ARMOR_CONSTRUCTION.get(), construction);
        ArmorStatsCatalog.apply(stack, construction);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !ArmorConstructionData.CHESTPLATE_TYPE.equals(construction.armorType())) {
            return super.getName(stack);
        }
        return construction.chestplatePlateMaterial()
                .map(material -> Component.translatable("item.mobstoolforging.material_modular_chestplate", MaterialCatalog.displayName(material)))
                .orElseGet(() -> MaterialCatalog.LEATHER.equals(construction.chestplateChainmailMaterial())
                        ? Component.translatable("item.mobstoolforging.material_modular_chestplate", MaterialCatalog.displayName(MaterialCatalog.LEATHER))
                        : Component.translatable("item.mobstoolforging.modular_chainmail_chestplate"));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return allowsFinishedArmorEnchanting(stack, super.isEnchantable(stack));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        return finishedArmorEnchantmentValue(stack, construction == null ? super.getEnchantmentValue(stack) : ArmorStatsCatalog.stats(construction).enchantmentValue());
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || isBrokenArmor(stack)) {
            return ItemAttributeModifiers.EMPTY;
        }
        ItemStack copy = stack.copy();
        ArmorStatsCatalog.apply(copy, construction);
        return copy.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return toRepair.get(ModDataComponents.ARMOR_CONSTRUCTION.get()) == null && super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null || !ArmorConstructionData.CHESTPLATE_TYPE.equals(construction.armorType())) {
            return;
        }
        if (isBrokenArmor(stack)) {
            tooltip.add(Component.translatable("tooltip.mobstoolforging.broken_armor").withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.translatable("tooltip.mobstoolforging.construction").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mobstoolforging.quality")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(construction.qualityLevel().displayName()));
        ResourceLocation baseMaterial = construction.chestplateChainmailMaterial();
        String baseKey = MaterialCatalog.LEATHER.equals(baseMaterial) ? "tooltip.mobstoolforging.armor_part.base" : "tooltip.mobstoolforging.armor_part.chainmail";
        tooltip.add(Component.translatable(baseKey, MaterialCatalog.displayName(baseMaterial)).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                "tooltip.mobstoolforging.armor_part.plate",
                construction.chestplatePlateMaterial().map(MaterialCatalog::displayName).orElseGet(() -> Component.translatable("tooltip.mobstoolforging.none"))
        ).withStyle(ChatFormatting.DARK_GRAY));
        construction.overlayBaseMaterial().ifPresent(base -> tooltip.add(Component.translatable(
                "tooltip.mobstoolforging.coating_base",
                MaterialCatalog.displayName(base)
        ).withStyle(ChatFormatting.DARK_GRAY)));
        if (flag.hasShiftDown()) {
            MetallurgyTooltips.appendToolParts(tooltip, stack);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        modularArmorInventoryTick(stack, level);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction != null && ArmorStatsCatalog.stats(construction).fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE);
        }
        return false;
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction != null && source.is(DamageTypeTags.IS_FIRE) && ArmorStatsCatalog.stats(construction).fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE);
            return false;
        }
        return super.canBeHurtBy(stack, source);
    }
}
