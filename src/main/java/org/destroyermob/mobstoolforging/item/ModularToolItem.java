package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
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
        ToolConstructionData data = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (data == null) {
            return;
        }
        tooltip.addAll(ToolTooltipBuilder.tooltip(stack, toolKind(), flag));
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
