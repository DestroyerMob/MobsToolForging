package org.destroyermob.mobstoolforging.recipe;

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
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ArmorStatsCatalog;
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
        ItemStack base = ItemStack.EMPTY;
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
            if (armorKind.matchesBase(stack, data)) {
                if (!base.isEmpty()) {
                    return Parts.invalid();
                }
                base = stack;
                continue;
            }
            return Parts.invalid();
        }
        return new Parts(base);
    }

    public enum ArmorKind {
        HELMET(ArmorPartData.HELMET_SKULL, ModItems.HELMET_SKULL::get) {
            @Override
            ItemStack create(Parts parts) {
                return ModItems.MODULAR_HELMET.get().create(material(parts.base()), Optional.empty(), Optional.empty());
            }
        },
        CHESTPLATE(ArmorPartData.CHESTPLATE_BODY, ModItems.CHESTPLATE_BODY::get) {
            @Override
            ItemStack create(Parts parts) {
                return ModItems.MODULAR_CHESTPLATE.get().create(material(parts.base()));
            }
        },
        LEGGINGS(ArmorPartData.LEGGINGS_LEGS, ModItems.LEGGINGS_LEGS::get) {
            @Override
            ItemStack create(Parts parts) {
                return ModItems.MODULAR_LEGGINGS.get().create(material(parts.base()));
            }
        },
        BOOTS(ArmorPartData.BOOTS_FEET, ModItems.BOOTS_FEET::get) {
            @Override
            ItemStack create(Parts parts) {
                return ModItems.MODULAR_BOOTS.get().create(material(parts.base()));
            }
        };

        private final String basePartType;
        private final Supplier<Item> baseItem;

        ArmorKind(String basePartType, Supplier<Item> baseItem) {
            this.basePartType = basePartType;
            this.baseItem = baseItem;
        }

        boolean matchesBase(ItemStack stack, ArmorPartData data) {
            return matches(stack, data, basePartType, baseItem);
        }

        abstract ItemStack create(Parts parts);

        private static boolean matches(ItemStack stack, ArmorPartData data, String partType, Supplier<Item> item) {
            return partType != null && item != null && stack.is(item.get()) && partType.equals(data.partType());
        }

        private static ResourceLocation material(ItemStack stack) {
            ArmorPartData data = stack.get(ModDataComponents.ARMOR_PART.get());
            return data == null ? ResourceLocation.withDefaultNamespace("air") : data.materialId();
        }
    }

    private record Parts(ItemStack base) {
        private static Parts invalid() {
            return new Parts(ItemStack.EMPTY);
        }

        private boolean isValid() {
            return !base.isEmpty();
        }
    }
}
