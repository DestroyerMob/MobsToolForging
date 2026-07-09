package org.destroyermob.mobstoolforging.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.world.ArmorExternalComponents;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ArmorStatsCatalog;
import org.destroyermob.mobstoolforging.world.ForgingQuality;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolAssemblyEnchantments;
import org.destroyermob.mobstoolforging.world.ToolAssemblyParts;
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
        ItemStack output = armorKind.create(parts);
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!ToolAssemblyEnchantments.mergeOnto(output, parts.enchantmentSources(), registries)) {
            return ItemStack.EMPTY;
        }
        ArmorExternalComponents.copyArmorPartComponentsToArmor(parts.base, parts.plate, output);
        output.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(parts.stacks()));
        ToolAssemblyEnchantments.syncRoutedToolEnchantments(output, registries);
        return output;
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
        HELMET(ArmorPartData.HELMET_CHAINMAIL, ModItems.HELMET_CHAINMAIL::get, ArmorPartData.HELMET_PLATE, ModItems.HELMET_PLATE::get, "LLL", "L L") {
            @Override
            ItemStack create(Parts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_HELMET.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_HELMET.get().createChainmail(quality(parts)));
            }

            @Override
            ItemStack createLeather() {
                return ModItems.MODULAR_HELMET.get().create(MaterialCatalog.LEATHER, Optional.empty());
            }
        },
        CHESTPLATE(ArmorPartData.CHESTPLATE_CHAINMAIL, ModItems.CHESTPLATE_CHAINMAIL::get, ArmorPartData.CHESTPLATE_BODY, ModItems.CHESTPLATE_BODY::get, "L L", "LLL", "LLL") {
            @Override
            ItemStack create(Parts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_CHESTPLATE.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_CHESTPLATE.get().createChainmail(quality(parts)));
            }

            @Override
            ItemStack createLeather() {
                return ModItems.MODULAR_CHESTPLATE.get().createBase(MaterialCatalog.LEATHER);
            }
        },
        LEGGINGS(ArmorPartData.LEGGINGS_CHAINMAIL, ModItems.LEGGINGS_CHAINMAIL::get, ArmorPartData.LEGGINGS_PLATE, ModItems.LEGGINGS_PLATE::get, "LLL", "L L", "L L") {
            @Override
            ItemStack create(Parts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_LEGGINGS.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_LEGGINGS.get().createChainmail(quality(parts)));
            }

            @Override
            ItemStack createLeather() {
                return ModItems.MODULAR_LEGGINGS.get().create(MaterialCatalog.LEATHER, Optional.empty());
            }
        },
        BOOTS(ArmorPartData.BOOTS_CHAINMAIL, ModItems.BOOTS_CHAINMAIL::get, ArmorPartData.BOOTS_PLATE, ModItems.BOOTS_PLATE::get, "L L", "L L") {
            @Override
            ItemStack create(Parts parts) {
                return optionalMaterial(parts.plate)
                        .map(material -> ModItems.MODULAR_BOOTS.get().create(material, quality(parts)))
                        .orElseGet(() -> ModItems.MODULAR_BOOTS.get().createChainmail(quality(parts)));
            }

            @Override
            ItemStack createLeather() {
                return ModItems.MODULAR_BOOTS.get().create(MaterialCatalog.LEATHER, Optional.empty());
            }
        };

        private final String basePartType;
        private final Supplier<Item> baseItem;
        private final String platePartType;
        private final Supplier<Item> plateItem;
        private final String[] leatherPattern;

        ArmorKind(String basePartType, Supplier<Item> baseItem, String platePartType, Supplier<Item> plateItem, String... leatherPattern) {
            this.basePartType = basePartType;
            this.baseItem = baseItem;
            this.platePartType = platePartType;
            this.plateItem = plateItem;
            this.leatherPattern = leatherPattern;
        }

        boolean matchesLeatherRecipe(CraftingInput input) {
            return matchesLeatherPattern(input, leatherPattern);
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

        abstract ItemStack createLeather();

        private static boolean matches(ItemStack stack, ArmorPartData data, String partType, Supplier<Item> item) {
            return partType != null && item != null && stack.is(item.get()) && partType.equals(data.partType());
        }

        private static boolean matchesLeatherPattern(CraftingInput input, String[] pattern) {
            int minX = input.width();
            int minY = input.height();
            int maxX = -1;
            int maxY = -1;
            for (int y = 0; y < input.height(); y++) {
                for (int x = 0; x < input.width(); x++) {
                    ItemStack stack = input.getItem(x + y * input.width());
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (!stack.is(Items.LEATHER)) {
                        return false;
                    }
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
            if (maxX < 0 || pattern.length == 0) {
                return false;
            }
            int patternHeight = pattern.length;
            int patternWidth = pattern[0].length();
            if (maxX - minX + 1 != patternWidth || maxY - minY + 1 != patternHeight) {
                return false;
            }
            for (int y = 0; y < patternHeight; y++) {
                String row = pattern[y];
                if (row.length() != patternWidth) {
                    return false;
                }
                for (int x = 0; x < patternWidth; x++) {
                    boolean expectedLeather = row.charAt(x) == 'L';
                    ItemStack stack = input.getItem(minX + x + (minY + y) * input.width());
                    if (expectedLeather != stack.is(Items.LEATHER)) {
                        return false;
                    }
                }
            }
            return true;
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

        private List<ItemStack> enchantmentSources() {
            return stacks();
        }

        private static void addIfPresent(List<ItemStack> stacks, ItemStack stack) {
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
    }
}
