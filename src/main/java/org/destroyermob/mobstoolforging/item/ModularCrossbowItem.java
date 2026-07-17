package org.destroyermob.mobstoolforging.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.CrossbowAssembly;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolTooltipBuilder;
import org.destroyermob.mobstoolforging.world.ToolTraitEffects;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;

/** A vanilla-behaving crossbow whose construction is retained as MTF components. */
public class ModularCrossbowItem extends CrossbowItem {
    public ModularCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return CrossbowAssembly.isCrossbow(stack.get(ModDataComponents.TOOL_CONSTRUCTION.get()))
                ? ToolTraitEffects.adjustDurabilityDamage(stack, amount, entity.getRandom())
                : super.damageItem(stack, amount, entity, onBroken);
    }

    @Override
    public Component getName(ItemStack stack) {
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (CrossbowAssembly.isCrossbow(construction)) {
            return Component.translatable("item.mobstoolforging.material_crossbow", MaterialCatalog.displayName(construction.headMaterial()));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolConstructionData construction = stack.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (!CrossbowAssembly.isCrossbow(construction)) {
            return;
        }
        ToolTypeRegistry.toolType(CrossbowAssembly.TOOL_TYPE)
                .ifPresent(definition -> tooltip.addAll(ToolTooltipBuilder.tooltip(stack, definition, context, flag)));
    }
}
