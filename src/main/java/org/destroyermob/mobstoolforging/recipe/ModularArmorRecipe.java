package org.destroyermob.mobstoolforging.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ArmorStatsCatalog;
import org.destroyermob.mobstoolforging.world.ForgingQuality;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;

public class ModularArmorRecipe extends CustomRecipe {
    private final ArmorKind armorKind;

    public ModularArmorRecipe(CraftingBookCategory category, ArmorKind armorKind) {
        super(category);
        this.armorKind = armorKind;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findParts(input).isValid();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Parts parts = findParts(input);
        if (!parts.isValid()) {
            return ItemStack.EMPTY;
        }
        return armorKind.create(parts);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return switch (armorKind) {
            case HELMET -> ModRecipeSerializers.MODULAR_HELMET.get();
            case CHESTPLATE -> ModRecipeSerializers.MODULAR_CHESTPLATE.get();
            case LEGGINGS -> ModRecipeSerializers.MODULAR_LEGGINGS.get();
            case BOOTS -> ModRecipeSerializers.MODULAR_BOOTS.get();
        };
    }

    private Parts findParts(CraftingInput input) {
        Parts parts = new Parts();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (WorkpieceHeat.hasHeat(stack)) {
                return Parts.invalid();
            }
            ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
            if (data == null || !ArmorStatsCatalog.isSupportedArmorMaterial(data.materialId())) {
                return Parts.invalid();
            }
            if (!armorKind.accept(parts, stack, data)) {
                return Parts.invalid();
            }
        }
        return parts;
    }

    public enum ArmorKind {
        HELMET(ArmorPartData.HELMET_CHAINMAIL, ModItems.HELMET_CHAINMAIL::get, ArmorPartData.HELMET_PLATE, ModItems.HELMET_PLATE::get) {
            @Override
            ItemStack create(Parts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_HELMET.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_HELMET.get().createChainmail(quality(parts)));
            }
        },
        CHESTPLATE(ArmorPartData.CHESTPLATE_CHAINMAIL, ModItems.CHESTPLATE_CHAINMAIL::get, ArmorPartData.CHESTPLATE_BODY, ModItems.CHESTPLATE_BODY::get) {
            @Override
            ItemStack create(Parts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_CHESTPLATE.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_CHESTPLATE.get().createChainmail(quality(parts)));
            }
        },
        LEGGINGS(ArmorPartData.LEGGINGS_CHAINMAIL, ModItems.LEGGINGS_CHAINMAIL::get, ArmorPartData.LEGGINGS_PLATE, ModItems.LEGGINGS_PLATE::get) {
            @Override
            ItemStack create(Parts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_LEGGINGS.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_LEGGINGS.get().createChainmail(quality(parts)));
            }
        },
        BOOTS(ArmorPartData.BOOTS_CHAINMAIL, ModItems.BOOTS_CHAINMAIL::get, ArmorPartData.BOOTS_PLATE, ModItems.BOOTS_PLATE::get) {
            @Override
            ItemStack create(Parts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_BOOTS.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_BOOTS.get().createChainmail(quality(parts)));
            }
        };

        private final String basePartType;
        private final Supplier<Item> baseItem;
        private final String platePartType;
        private final Supplier<Item> plateItem;

        ArmorKind(String basePartType, Supplier<Item> baseItem, String platePartType, Supplier<Item> plateItem) {
            this.basePartType = basePartType;
            this.baseItem = baseItem;
            this.platePartType = platePartType;
            this.plateItem = plateItem;
        }

        boolean matchesBase(ItemStack stack, ArmorPartData data) {
            return matches(stack, data, basePartType, baseItem) && MaterialCatalog.IRON.equals(data.materialId());
        }

        boolean accept(Parts parts, ItemStack stack, ArmorPartData data) {
            if (matchesBase(stack, data)) {
                return parts.setBase(stack);
            }
            return matches(stack, data, platePartType, plateItem) && parts.setPlate(stack);
        }

        abstract ItemStack create(Parts parts);

        private static boolean matches(ItemStack stack, ArmorPartData data, String partType, Supplier<Item> item) {
            return partType != null && item != null && stack.is(item.get()) && partType.equals(data.partType());
        }

        private static ResourceLocation material(ItemStack stack) {
            ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
            return data == null ? ResourceLocation.withDefaultNamespace("air") : data.materialId();
        }

        private static Optional<ResourceLocation> optionalMaterial(ItemStack stack) {
            return stack.isEmpty() ? Optional.empty() : Optional.of(material(stack));
        }

        private static int quality(Parts parts) {
            if (!MobsToolForgingConfig.ENABLE_QUALITY.get()) {
                return ArmorPartData.DEFAULT_QUALITY;
            }
            List<ArmorPartData> partData = parts.stacks().stream()
                    .map(stack -> stack.get(ModDataComponents.ARMOR_PART.get()))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (partData.isEmpty()) {
                return ArmorPartData.DEFAULT_QUALITY;
            }
            float total = 0.0F;
            for (ArmorPartData data : partData) {
                total += data.quality();
            }
            return ForgingQuality.clampScore(Math.round(total / partData.size()));
        }
    }

    private static final class Parts {
        private ItemStack base = ItemStack.EMPTY;
        private ItemStack plate = ItemStack.EMPTY;
        private boolean invalid;

        private static Parts invalid() {
            Parts parts = new Parts();
            parts.invalid = true;
            return parts;
        }

        private boolean isValid() {
            return !invalid && !base.isEmpty();
        }

        private boolean setBase(ItemStack stack) {
            if (!base.isEmpty()) {
                return false;
            }
            base = stack;
            return true;
        }

        private boolean setPlate(ItemStack stack) {
            if (!plate.isEmpty()) {
                return false;
            }
            plate = stack;
            return true;
        }

        private List<ItemStack> stacks() {
            List<ItemStack> stacks = new ArrayList<>();
            addIfPresent(stacks, base);
            addIfPresent(stacks, plate);
            return stacks;
        }

        private static void addIfPresent(List<ItemStack> stacks, ItemStack stack) {
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
    }
}
