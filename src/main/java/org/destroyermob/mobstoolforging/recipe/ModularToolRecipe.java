package org.destroyermob.mobstoolforging.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.registry.ModTags;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolTypeDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public class ModularToolRecipe extends CustomRecipe {
    private final ResourceLocation toolTypeId;

    public ModularToolRecipe(CraftingBookCategory category, ToolKind toolKind) {
        this(category, ToolConstructionData.toolType(toolKind));
    }

    public ModularToolRecipe(CraftingBookCategory category, ResourceLocation toolTypeId) {
        super(category);
        this.toolTypeId = toolTypeId;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return definition().map(definition -> findParts(input, definition).isValid(definition)).orElse(false);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ToolTypeDefinition definition = definition().orElse(null);
        if (definition == null) {
            return ItemStack.EMPTY;
        }
        Parts parts = findParts(input, definition);
        if (!parts.isValid(definition)) {
            return ItemStack.EMPTY;
        }
        return parts.construction(definition)
                .map(definition::createTool)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        Optional<ToolKind> builtInKind = ToolKind.byId(toolTypeId.getPath())
                .filter(kind -> ToolConstructionData.toolType(kind).equals(toolTypeId));
        return builtInKind
                .<RecipeSerializer<?>>map(kind -> ModRecipeSerializers.serializerFor(kind).get())
                .orElseGet(() -> ModRecipeSerializers.MODULAR_TOOL.get());
    }

    private Optional<ToolTypeDefinition> definition() {
        return ToolTypeRegistry.toolType(toolTypeId);
    }

    private Parts findParts(CraftingInput input, ToolTypeDefinition definition) {
        ItemStack part = ItemStack.EMPTY;
        ItemStack handle = ItemStack.EMPTY;
        ItemStack binding = ItemStack.EMPTY;
        ItemStack wrap = ItemStack.EMPTY;
        ItemStack focus = ItemStack.EMPTY;
        ItemStack treatment = ItemStack.EMPTY;
        Map<String, ItemStack> requiredParts = new LinkedHashMap<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (WorkpieceHeat.hasHeat(stack)) {
                return Parts.invalid();
            }
            ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
            if (matchesPart(definition, stack, partData, definition.primaryPartType())) {
                if (MaterialCatalog.definition(partData.materialId()).isEmpty()) {
                    return Parts.invalid();
                }
                if (!part.isEmpty()) {
                    return Parts.invalid();
                }
                part = stack;
                continue;
            }
            String requiredPart = matchingRequiredPart(definition, stack, partData).orElse(null);
            if (requiredPart != null) {
                if (MaterialCatalog.definition(partData.materialId()).isEmpty()) {
                    return Parts.invalid();
                }
                if (requiredParts.containsKey(requiredPart)) {
                    return Parts.invalid();
                }
                requiredParts.put(requiredPart, stack);
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
                if (!definition.requiredAssemblyParts().isEmpty()) {
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
        return new Parts(part, handle, binding, requiredParts, wrap, focus, treatment);
    }

    private static Optional<ResourceLocation> material(ItemStack stack, MaterialResolver resolver) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(resolver.resolve(stack));
    }

    private static boolean matchesPart(ToolTypeDefinition definition, ItemStack stack, ToolPartData partData, String partType) {
        if (partData == null || !partType.equals(partData.partType())) {
            return false;
        }
        return definition.matchesPartItem(partType, partData.materialId(), stack);
    }

    private static Optional<String> matchingRequiredPart(ToolTypeDefinition definition, ItemStack stack, ToolPartData partData) {
        return definition.requiredAssemblyParts().stream()
                .filter(partType -> matchesPart(definition, stack, partData, partType))
                .findFirst();
    }

    @FunctionalInterface
    private interface MaterialResolver {
        ResourceLocation resolve(ItemStack stack);
    }

    private record Parts(ItemStack part, ItemStack handle, ItemStack binding, Map<String, ItemStack> requiredParts, ItemStack wrap, ItemStack focus, ItemStack treatment) {
        private static Parts invalid() {
            return new Parts(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, Map.of(), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
        }

        private boolean isValid(ToolTypeDefinition definition) {
            Optional<ToolConstructionData> construction = construction(definition);
            return construction.isPresent() && definition.canAssemble(construction.get(), partDataByType());
        }

        private Optional<ToolConstructionData> construction(ToolTypeDefinition definition) {
            ToolPartData partData = part.get(ModDataComponents.TOOL_PART.get());
            if (partData == null || handle.isEmpty() || !requiredParts.keySet().containsAll(definition.requiredAssemblyParts())) {
                return Optional.empty();
            }
            return Optional.of(new ToolConstructionData(
                    definition.id(),
                    partData.materialId(),
                    MaterialCatalog.handleMaterial(handle),
                    bindingMaterial(),
                    material(wrap, MaterialCatalog::wrapMaterial),
                    material(focus, MaterialCatalog::focusMaterial),
                    material(treatment, MaterialCatalog::treatmentMaterial),
                    quality()
            ));
        }

        private Map<String, ToolPartData> partDataByType() {
            Map<String, ToolPartData> values = new LinkedHashMap<>();
            ToolPartData primary = part.get(ModDataComponents.TOOL_PART.get());
            if (primary != null) {
                values.put(primary.partType(), primary);
            }
            requiredParts.forEach((partType, stack) -> {
                ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
                if (data != null) {
                    values.put(partType, data);
                }
            });
            return values;
        }

        private Optional<ResourceLocation> bindingMaterial() {
            if (!requiredParts.isEmpty()) {
                return requiredParts.values().stream().findFirst().map(MaterialCatalog::bindingMaterial);
            }
            return binding.isEmpty() ? Optional.empty() : Optional.of(MaterialCatalog.bindingMaterial(binding));
        }

        private int quality() {
            int total = partQuality(part);
            int count = 1;
            for (ItemStack requiredPart : requiredParts.values()) {
                total += partQuality(requiredPart);
                count++;
            }
            return Math.round(total / (float) count);
        }

        private static int partQuality(ItemStack stack) {
            ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
            return data == null ? ToolPartData.DEFAULT_QUALITY : data.quality();
        }
    }

    public static final class Serializer implements RecipeSerializer<ModularToolRecipe> {
        private static final MapCodec<ModularToolRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.EQUIPMENT).forGetter(ModularToolRecipe::category),
                ResourceLocation.CODEC.fieldOf("tool_type").forGetter(recipe -> recipe.toolTypeId)
        ).apply(instance, ModularToolRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ModularToolRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::write,
                Serializer::read
        );

        @Override
        public MapCodec<ModularToolRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ModularToolRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ModularToolRecipe read(RegistryFriendlyByteBuf buffer) {
            return new ModularToolRecipe(CraftingBookCategory.STREAM_CODEC.decode(buffer), buffer.readResourceLocation());
        }

        private static void write(RegistryFriendlyByteBuf buffer, ModularToolRecipe recipe) {
            CraftingBookCategory.STREAM_CODEC.encode(buffer, recipe.category());
            buffer.writeResourceLocation(recipe.toolTypeId);
        }
    }
}
