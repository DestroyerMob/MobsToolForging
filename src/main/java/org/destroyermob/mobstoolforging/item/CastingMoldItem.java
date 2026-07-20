package org.destroyermob.mobstoolforging.item;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;

public class CastingMoldItem extends ToolTemplateItem {
    public CastingMoldItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(ResourceLocation templateId) {
        ItemStack stack = new ItemStack(ModItems.CASTING_MOLD.get());
        stack.set(ModDataComponents.FORGE_TEMPLATE.get(), templateId);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        Optional<ForgeTemplateDefinition> template = template(stack);
        return template.isPresent()
                ? Component.translatable("item.mobstoolforging.casting_mold.named", template.get().displayName())
                : super.getName(stack);
    }
}
