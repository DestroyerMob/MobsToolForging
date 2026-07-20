package org.destroyermob.mobstoolforging.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModTags;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MobsToolForging.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(ModTags.Blocks.PATTERN_RACKS, ModTags.Items.PATTERN_RACKS);
        copy(ModTags.Blocks.TOOLMAKERS_BENCHES, ModTags.Items.TOOLMAKERS_BENCHES);
        copy(ModTags.Blocks.SAWMILLS, ModTags.Items.SAWMILLS);
        copy(ModTags.Blocks.LEATHER_STATIONS, ModTags.Items.LEATHER_STATIONS);
        copy(ModTags.Blocks.DRYING_RACKS, ModTags.Items.DRYING_RACKS);
        tag(ItemTags.SWORDS).add(ModItems.SWORD.get());
        tag(ItemTags.SHOVELS).add(ModItems.SHOVEL.get());
        tag(ItemTags.PICKAXES).add(ModItems.PICKAXE.get());
        tag(ItemTags.AXES).add(ModItems.AXE.get());
        tag(ItemTags.HOES).add(ModItems.HOE.get());
        tag(ItemTags.SHOVELS).add(ModItems.MATTOCK.get());
        tag(ItemTags.AXES).add(ModItems.MATTOCK.get());
        tag(ItemTags.HOES).add(ModItems.MATTOCK.get());
        tag(ItemTags.CROSSBOW_ENCHANTABLE).add(ModItems.CROSSBOW.get());
        tag(ItemTags.TRIMMABLE_ARMOR).add(
                ModItems.MODULAR_HELMET.get(),
                ModItems.MODULAR_CHESTPLATE.get(),
                ModItems.MODULAR_LEGGINGS.get(),
                ModItems.MODULAR_BOOTS.get()
        );
        tag(ModTags.Items.MATERIALS).addTag(ModTags.Items.MATERIALS_METALS).addTag(ModTags.Items.MATERIALS_GEMS);
        tag(ModTags.Items.MATERIALS_METALS)
                .add(ModItems.STEEL_INGOT.get(), ModItems.BRONZE_INGOT.get())
                .addTag(Tags.Items.INGOTS_IRON)
                .addTag(Tags.Items.INGOTS_GOLD)
                .addTag(Tags.Items.INGOTS_COPPER);
        tag(ModTags.Items.MATERIALS_GEMS)
                .addTag(Tags.Items.GEMS_DIAMOND)
                .addTag(Tags.Items.GEMS_EMERALD)
                .addOptionalTag(ModTags.Items.GEMS_AMETHYST.location())
                .addOptionalTag(ModTags.Items.GEMS_RUBY.location())
                .addOptionalTag(ModTags.Items.GEMS_SAPPHIRE.location())
                .addOptionalTag(ModTags.Items.GEMS_TOPAZ.location())
                .addOptionalTag(ModTags.Items.LEGACY_FORGE_GEMS_AMETHYST.location())
                .addOptionalTag(ModTags.Items.LEGACY_FORGE_GEMS_RUBY.location())
                .addOptionalTag(ModTags.Items.LEGACY_FORGE_GEMS_SAPPHIRE.location())
                .addOptionalTag(ModTags.Items.LEGACY_FORGE_GEMS_TOPAZ.location());
        tag(ModTags.Items.PARTS)
                .addTag(ModTags.Items.PART_HEADS)
                .addTag(ModTags.Items.PART_HANDLES)
                .addTag(ModTags.Items.PART_GUARDS)
                .addTag(ModTags.Items.PART_CROSSBOW_BODIES)
                .addTag(ModTags.Items.PART_CROSSBOW_LIMBS);
        tag(ModTags.Items.PART_HEADS)
                .addTag(ModTags.Items.PART_SWORD_BLADES)
                .addTag(ModTags.Items.PART_SHOVEL_HEADS)
                .addTag(ModTags.Items.PART_PICKAXE_HEADS)
                .addTag(ModTags.Items.PART_AXE_HEADS)
                .addTag(ModTags.Items.PART_HOE_HEADS)
                .addTag(ModTags.Items.PART_COOKING_KNIFE_HEADS)
                .addTag(ModTags.Items.PART_SCREWDRIVER_HEADS)
                .addTag(ModTags.Items.PART_GEM_CUTTERS_BLADES);
        tag(ModTags.Items.PART_GUARDS)
                .addTag(ModTags.Items.PART_SWORD_GUARDS);
        tag(ModTags.Items.ARMOR_PARTS)
                .addTag(ModTags.Items.PART_HELMET_CHAINMAILS)
                .addTag(ModTags.Items.PART_HELMET_PLATES)
                .addTag(ModTags.Items.PART_CHESTPLATE_CHAINMAILS)
                .addTag(ModTags.Items.PART_CHESTPLATE_BODIES)
                .addTag(ModTags.Items.PART_LEGGINGS_CHAINMAILS)
                .addTag(ModTags.Items.PART_LEGGINGS_PLATES)
                .addTag(ModTags.Items.PART_BOOTS_CHAINMAILS)
                .addTag(ModTags.Items.PART_BOOTS_PLATES);
        tag(ModTags.Items.PART_HANDLES)
                .add(Items.STICK, Items.BLAZE_ROD, Items.BREEZE_ROD);
        tag(ModTags.Items.PART_SWORD_BLADES)
                .add(ModItems.SWORD_BLADE.get());
        tag(ModTags.Items.PART_SWORD_GUARDS)
                .add(ModItems.SWORD_GUARD.get());
        tag(ModTags.Items.PART_SHOVEL_HEADS)
                .add(ModItems.SHOVEL_HEAD.get());
        tag(ModTags.Items.PART_PICKAXE_HEADS)
                .add(ModItems.PICKAXE_HEAD.get());
        tag(ModTags.Items.PART_AXE_HEADS)
                .add(ModItems.AXE_HEAD.get());
        tag(ModTags.Items.PART_HOE_HEADS)
                .add(ModItems.HOE_HEAD.get());
        tag(ModTags.Items.PART_COOKING_KNIFE_HEADS)
                .add(ModItems.COOKING_KNIFE_HEAD.get());
        tag(ModTags.Items.PART_SCREWDRIVER_HEADS)
                .add(ModItems.SCREWDRIVER_HEAD.get());
        tag(ModTags.Items.PART_GEM_CUTTERS_BLADES)
                .add(ModItems.GEM_CUTTERS_BLADE.get());
        tag(ModTags.Items.PART_CROSSBOW_BODIES)
                .add(ModItems.CROSSBOW_BODY.get());
        tag(ModTags.Items.PART_CROSSBOW_LIMBS)
                .add(ModItems.CROSSBOW_LIMBS.get());
        tag(ModTags.Items.PART_HELMET_CHAINMAILS)
                .add(ModItems.HELMET_CHAINMAIL.get());
        tag(ModTags.Items.PART_HELMET_PLATES)
                .add(ModItems.HELMET_PLATE.get());
        tag(ModTags.Items.PART_CHESTPLATE_CHAINMAILS)
                .add(ModItems.CHESTPLATE_CHAINMAIL.get());
        tag(ModTags.Items.PART_CHESTPLATE_BODIES)
                .add(ModItems.CHESTPLATE_BODY.get());
        tag(ModTags.Items.PART_LEGGINGS_CHAINMAILS)
                .add(ModItems.LEGGINGS_CHAINMAIL.get());
        tag(ModTags.Items.PART_LEGGINGS_PLATES)
                .add(ModItems.LEGGINGS_PLATE.get());
        tag(ModTags.Items.PART_BOOTS_CHAINMAILS)
                .add(ModItems.BOOTS_CHAINMAIL.get());
        tag(ModTags.Items.PART_BOOTS_PLATES)
                .add(ModItems.BOOTS_PLATE.get());
        tag(ModTags.Items.TOOL_HANDLES)
                .add(Items.STICK, Items.BLAZE_ROD, Items.BREEZE_ROD)
                .addTag(Tags.Items.RODS_WOODEN)
                .addTag(Tags.Items.RODS_BLAZE)
                .addTag(Tags.Items.RODS_BREEZE);
        tag(ModTags.Items.CROSSBOW_STRINGS)
                .add(Items.STRING, ModItems.PLANT_FIBER.get(), ModItems.BLAZE_THREAD.get());
        tag(ModTags.Items.LAPIDARY_ABRASIVES)
                .addTag(ModTags.Items.LAPIDARY_ABRASIVES_DIAMOND);
        tag(ModTags.Items.LAPIDARY_ABRASIVES_DIAMOND)
                .add(ModItems.DIAMOND_POWDER.get());
        tag(ModTags.Items.KNAPPING_TOOLS)
                .add(Items.FLINT);
        tag(ModTags.Items.HAMMER_STONES)
                .addTag(Tags.Items.STONES)
                .addTag(Tags.Items.COBBLESTONES)
                .addTag(ItemTags.STONE_CRAFTING_MATERIALS);
    }
}
