package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public class ModularToolPartItem extends Item {
    private final String partType;

    public ModularToolPartItem(ToolKind toolKind, Properties properties) {
        this(toolKind, toolKind.partType(), properties);
    }

    public ModularToolPartItem(ToolKind toolKind, String partType, Properties properties) {
        this(partType, properties);
    }

    public ModularToolPartItem(String partType, Properties properties) {
        super(properties);
        this.partType = partType;
    }

    public ItemStack createPart(ResourceLocation materialId) {
        return createPart(materialId, ToolPartData.DEFAULT_QUALITY);
    }

    public ItemStack createPart(ResourceLocation materialId, int quality) {
        ItemStack stack = new ItemStack(this);
        stack.set(ModDataComponents.TOOL_PART.get(), new ToolPartData(partType, materialId, quality));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        if (data != null && partType.equals(data.partType())) {
            return Component.translatable("item.mobstoolforging.material_" + partType, MaterialCatalog.displayName(data.materialId()));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        if (data != null && flag.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.mobstoolforging.part_quality", data.quality()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
