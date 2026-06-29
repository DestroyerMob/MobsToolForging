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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorConstructionData;
import org.destroyermob.mobstoolforging.world.ArmorStatsCatalog;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;

public class ModularHelmetItem extends ArmorItem {
    public ModularHelmetItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties);
    }

    public ItemStack create(ResourceLocation skullMaterial, Optional<ResourceLocation> combMaterial, Optional<ResourceLocation> visorMaterial) {
        ArmorConstructionData construction = ArmorConstructionData.helmet(skullMaterial, combMaterial, visorMaterial);
        ItemStack stack = new ItemStack(this);
        stack.set(ModDataComponents.ARMOR_CONSTRUCTION.get(), construction);
        ArmorStatsCatalog.apply(stack, construction);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null) {
            return super.getName(stack);
        }
        return Component.translatable("item.mobstoolforging.material_modular_helmet", MaterialCatalog.displayName(construction.skullMaterial()));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        return construction == null ? super.getEnchantmentValue(stack) : ArmorStatsCatalog.helmetStats(construction.skullMaterial()).enchantmentValue();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction == null) {
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
        if (construction == null) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.mobstoolforging.construction").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mobstoolforging.armor_part.skull", MaterialCatalog.displayName(construction.skullMaterial())).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                "tooltip.mobstoolforging.armor_part.comb",
                construction.combMaterial().map(MaterialCatalog::displayName).orElseGet(() -> Component.translatable("tooltip.mobstoolforging.none"))
        ).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                "tooltip.mobstoolforging.armor_part.visor",
                construction.visorMaterial().map(MaterialCatalog::displayName).orElseGet(() -> Component.translatable("tooltip.mobstoolforging.none"))
        ).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction != null && ArmorStatsCatalog.helmetStats(construction.skullMaterial()).fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE);
        }
        return false;
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        ArmorConstructionData construction = stack.get(ModDataComponents.ARMOR_CONSTRUCTION.get());
        if (construction != null && source.is(DamageTypeTags.IS_FIRE) && ArmorStatsCatalog.helmetStats(construction.skullMaterial()).fireResistant()) {
            stack.set(DataComponents.FIRE_RESISTANT, net.minecraft.util.Unit.INSTANCE);
            return false;
        }
        return super.canBeHurtBy(stack, source);
    }
}
