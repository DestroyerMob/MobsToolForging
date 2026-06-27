package org.destroyermob.mobstoolforging.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolStatBuilder;
import org.destroyermob.mobstoolforging.world.ToolTooltipBuilder;

public interface ModularToolItem extends IItemExtension {
    ToolKind toolKind();

    default boolean allowsFinishedToolEnchanting(ItemStack stack, boolean fallback) {
        if (stack.get(ModDataComponents.TOOL_CONSTRUCTION.get()) != null && !MobsToolForgingConfig.ALLOW_FINISHED_TOOL_ENCHANTING.get()) {
            return false;
        }
        return fallback;
    }

    default int finishedToolEnchantmentValue(ItemStack stack, int fallback) {
        return allowsFinishedToolEnchanting(stack, true) ? fallback : 0;
    }

    @Override
    default boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return allowsFinishedToolEnchanting(stack, true) && IItemExtension.super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    default boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return allowsFinishedToolEnchanting(stack, true) && IItemExtension.super.supportsEnchantment(stack, enchantment);
    }

    @Override
    default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return allowsFinishedToolEnchanting(stack, true) && IItemExtension.super.isBookEnchantable(stack, book);
    }

    @Override
    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        if (amount <= 0 || isBrokenTool(stack) || stack.get(ModDataComponents.TOOL_CONSTRUCTION.get()) == null || !stack.isDamageableItem()) {
            return isBrokenTool(stack) ? 0 : amount;
        }
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0 || stack.getDamageValue() + amount < maxDamage) {
            return amount;
        }
        setBrokenTool(stack);
        return 0;
    }

    default boolean isBrokenTool(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(ModDataComponents.TOOL_BROKEN.get()));
    }

    default void setBrokenTool(ItemStack stack) {
        stack.set(ModDataComponents.TOOL_BROKEN.get(), true);
        if (stack.isDamageableItem()) {
            stack.setDamageValue(Math.max(0, stack.getMaxDamage() - 1));
        }
        stack.remove(DataComponents.TOOL);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    default void refreshBrokenTool(ItemStack stack) {
        if (!isBrokenTool(stack) || !stack.isDamageableItem() || stack.getDamageValue() >= Math.max(0, stack.getMaxDamage() - 1)) {
            return;
        }
        stack.remove(ModDataComponents.TOOL_BROKEN.get());
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction == null) {
            return;
        }
        int repairedDamage = stack.getDamageValue();
        ToolStatBuilder.apply(stack, toolKind(), construction);
        if (stack.isDamageableItem()) {
            stack.setDamageValue(Math.min(repairedDamage, Math.max(0, stack.getMaxDamage() - 1)));
        }
    }

    default ItemStack create(ResourceLocation headMaterialId, ItemStack handle) {
        ResourceLocation handleMaterial = MaterialCatalog.handleMaterial(handle);
        return create(ToolConstructionData.basic(toolKind(), headMaterialId, handleMaterial));
    }

    default ItemStack create(ToolConstructionData construction) {
        ItemStack stack = new ItemStack((Item) this);
        stack.set(ModDataComponents.TOOL_CONSTRUCTION.get(), construction);
        ToolStatBuilder.apply(stack, toolKind(), construction);
        return stack;
    }

    default Component getModularName(ItemStack stack, Component fallback) {
        ToolConstructionData data = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        return data == null ? fallback : toolKind().toolName(data.headMaterial());
    }

    default void appendModularTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        if (isBrokenTool(stack)) {
            tooltip.add(Component.translatable("tooltip.mobstoolforging.broken_tool").withStyle(ChatFormatting.RED));
        }
        ToolConstructionData data = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (data == null) {
            return;
        }
        tooltip.addAll(ToolTooltipBuilder.tooltip(stack, toolKind(), flag));
    }

    default void modularInventoryTick(ItemStack stack, Level level) {
        if (!level.isClientSide) {
            refreshBrokenTool(stack);
        }
    }

    default boolean isValidModularRepairItem(ItemStack stack, ItemStack repairCandidate, boolean fallback) {
        return stack.get(ModDataComponents.TOOL_CONSTRUCTION.get()) == null ? fallback : false;
    }

    default boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        ToolStatBuilder.ensureFireResistanceComponent(stack, toolKind());
        return false;
    }

    default boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE) && ToolStatBuilder.shouldBeFireResistant(stack, toolKind())) {
            ToolStatBuilder.ensureFireResistanceComponent(stack, toolKind());
            return false;
        }
        return true;
    }
}
