package org.destroyermob.mobstoolforging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.recipe.ModularArmorRecipe;
import org.destroyermob.mobstoolforging.recipe.ModularToolRecipe;
import org.destroyermob.mobstoolforging.recipe.ToolConversionRecipe;
import org.destroyermob.mobstoolforging.world.ToolKind;

public final class ModRecipeSerializers {
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            Registries.RECIPE_SERIALIZER,
            MobsToolForging.MOD_ID
    );

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularToolRecipe>> MODULAR_SWORD =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_sword",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularToolRecipe(category, ToolKind.SWORD))
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularToolRecipe>> MODULAR_SHOVEL =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_shovel",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularToolRecipe(category, ToolKind.SHOVEL))
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularToolRecipe>> MODULAR_PICKAXE =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_pickaxe",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularToolRecipe(category, ToolKind.PICKAXE))
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularToolRecipe>> MODULAR_AXE =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_axe",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularToolRecipe(category, ToolKind.AXE))
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularToolRecipe>> MODULAR_HOE =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_hoe",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularToolRecipe(category, ToolKind.HOE))
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularToolRecipe>> MODULAR_MATTOCK =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_mattock",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularToolRecipe(category, ToolKind.MATTOCK))
            );
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ModularToolRecipe>> MODULAR_TOOL =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_tool",
                    ModularToolRecipe.Serializer::new
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ToolConversionRecipe>> TOOL_CONVERSION =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_tool_conversion",
                    () -> new SimpleCraftingRecipeSerializer<>(ToolConversionRecipe::new)
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularArmorRecipe>> MODULAR_HELMET =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_helmet",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularArmorRecipe(category, ModularArmorRecipe.ArmorKind.HELMET))
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularArmorRecipe>> MODULAR_CHESTPLATE =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_chestplate",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularArmorRecipe(category, ModularArmorRecipe.ArmorKind.CHESTPLATE))
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularArmorRecipe>> MODULAR_LEGGINGS =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_leggings",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularArmorRecipe(category, ModularArmorRecipe.ArmorKind.LEGGINGS))
            );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularArmorRecipe>> MODULAR_BOOTS =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_modular_boots",
                    () -> new SimpleCraftingRecipeSerializer<>(category -> new ModularArmorRecipe(category, ModularArmorRecipe.ArmorKind.BOOTS))
            );

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }

    public static DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ModularToolRecipe>> serializerFor(ToolKind toolKind) {
        return switch (toolKind) {
            case SWORD -> MODULAR_SWORD;
            case SHOVEL -> MODULAR_SHOVEL;
            case PICKAXE -> MODULAR_PICKAXE;
            case AXE -> MODULAR_AXE;
            case HOE -> MODULAR_HOE;
            case MATTOCK -> MODULAR_MATTOCK;
        };
    }
}
