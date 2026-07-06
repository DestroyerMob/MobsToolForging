package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;

public class ModularArmorPartItem extends Item {
    private final String partType;

    public ModularArmorPartItem(String partType, Properties properties) {
        super(properties);
        this.partType = partType;
    }

    public ItemStack createPart(ResourceLocation materialId) {
        return createPart(materialId, ArmorPartData.DEFAULT_QUALITY);
    }

    public ItemStack createPart(ResourceLocation materialId, int quality) {
        ItemStack stack = new ItemStack(this);
        stack.set(ModDataComponents.ARMOR_PART.get(), new ArmorPartData(partType, materialId, quality));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
        if (data != null && partType.equals(data.partType())) {
            return Component.translatable("item.mobstoolforging.material_" + partType, MaterialCatalog.displayName(data.materialId()));
        }
        return super.getName(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
        return data != null && partType.equals(data.partType());
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
        if (data == null || !partType.equals(data.partType())) {
            return 0;
        }
        return MaterialCatalog.definition(data.materialId())
                .map(definition -> definition.tier().getEnchantmentValue())
                .orElse(0);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
        if (data != null && partType.equals(data.partType())) {
            tooltip.add(Component.translatable("tooltip.mobstoolforging.armor_part_material", MaterialCatalog.displayName(data.materialId())).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("tooltip.mobstoolforging.quality")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(data.qualityLevel().displayName()));
        }
    }
}
