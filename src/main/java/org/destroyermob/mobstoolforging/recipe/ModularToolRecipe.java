package org.destroyermob.mobstoolforging.recipe;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.item.ModularToolItem;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public class ModularToolRecipe extends CustomRecipe {
    private final ToolKind toolKind;

    public ModularToolRecipe(CraftingBookCategory category, ToolKind toolKind) {
        super(category);
        this.toolKind = toolKind;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findParts(input).isValid(toolKind);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Parts parts = findParts(input);
        if (!parts.isValid(toolKind)) {
            return ItemStack.EMPTY;
        }
        ToolPartData partData = parts.part().get(ModDataComponents.TOOL_PART.get());
        Item item = toolKind.toolItem().get();
        if (!(item instanceof ModularToolItem modularTool)) {
            return ItemStack.EMPTY;
        }
        return modularTool.create(new ToolConstructionData(
                ToolConstructionData.toolType(toolKind),
                partData.materialId(),
                MaterialCatalog.handleMaterial(parts.handle()),
                material(parts.binding(), MaterialCatalog::bindingMaterial),
                material(parts.wrap(), MaterialCatalog::wrapMaterial),
                material(parts.focus(), MaterialCatalog::focusMaterial),
                material(parts.treatment(), MaterialCatalog::treatmentMaterial),
                ToolConstructionData.DEFAULT_QUALITY
        ));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= (toolKind == ToolKind.SWORD ? 3 : 2);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.serializerFor(toolKind).get();
    }

    private Parts findParts(CraftingInput input) {
        ItemStack part = ItemStack.EMPTY;
        ItemStack handle = ItemStack.EMPTY;
        ItemStack binding = ItemStack.EMPTY;
        ItemStack wrap = ItemStack.EMPTY;
        ItemStack focus = ItemStack.EMPTY;
        ItemStack treatment = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
            if (stack.is(toolKind.partItem().get()) && partData != null && toolKind.partType().equals(partData.partType())) {
                if (MaterialCatalog.definition(partData.materialId()).isEmpty()) {
                    return Parts.invalid();
                }
                if (!part.isEmpty()) {
                    return Parts.invalid();
                }
                part = stack;
                continue;
            }
            if (toolKind == ToolKind.SWORD && stack.is(ModItems.SWORD_GUARD.get()) && partData != null && ToolPartData.SWORD_GUARD.equals(partData.partType())) {
                if (MaterialCatalog.definition(partData.materialId()).isEmpty()) {
                    return Parts.invalid();
                }
                if (!binding.isEmpty()) {
                    return Parts.invalid();
                }
                binding = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TOOL_HANDLES)) {
                if (!handle.isEmpty()) {
                    return Parts.invalid();
                }
                handle = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TOOL_BINDINGS)) {
                if (toolKind == ToolKind.SWORD) {
                    return Parts.invalid();
                }
                if (!binding.isEmpty()) {
                    return Parts.invalid();
                }
                binding = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TOOL_WRAPS)) {
                if (!wrap.isEmpty()) {
                    return Parts.invalid();
                }
                wrap = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TOOL_FOCI)) {
                if (!focus.isEmpty()) {
                    return Parts.invalid();
                }
                focus = stack;
                continue;
            }
            if (stack.is(ModTags.Items.TREATMENT_CATALYSTS)) {
                if (!treatment.isEmpty()) {
                    return Parts.invalid();
                }
                treatment = stack;
                continue;
            }
            return Parts.invalid();
        }
        return new Parts(part, handle, binding, wrap, focus, treatment);
    }

    private Optional<ResourceLocation> material(ItemStack stack, MaterialResolver resolver) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(resolver.resolve(stack));
    }

    @FunctionalInterface
    private interface MaterialResolver {
        ResourceLocation resolve(ItemStack stack);
    }

    private record Parts(ItemStack part, ItemStack handle, ItemStack binding, ItemStack wrap, ItemStack focus, ItemStack treatment) {
        private static Parts invalid() {
            return new Parts(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
        }

        private boolean isValid(ToolKind toolKind) {
            return !part.isEmpty() && !handle.isEmpty() && (toolKind != ToolKind.SWORD || !binding.isEmpty());
        }
    }
}
