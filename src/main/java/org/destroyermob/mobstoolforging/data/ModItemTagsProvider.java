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
        tag(ItemTags.SWORDS).add(ModItems.SWORD.get());
        tag(ItemTags.SHOVELS).add(ModItems.SHOVEL.get());
        tag(ItemTags.PICKAXES).add(ModItems.PICKAXE.get());
        tag(ItemTags.AXES).add(ModItems.AXE.get());
        tag(ItemTags.HOES).add(ModItems.HOE.get());
        tag(ModTags.Items.MATERIALS).addTag(ModTags.Items.MATERIALS_METALS).addTag(ModTags.Items.MATERIALS_GEMS);
        tag(ModTags.Items.MATERIALS_METALS)
                .addTag(Tags.Items.INGOTS_IRON)
                .addTag(Tags.Items.INGOTS_GOLD)
                .addTag(Tags.Items.INGOTS_COPPER);
        tag(ModTags.Items.MATERIALS_GEMS)
                .addTag(Tags.Items.GEMS_DIAMOND)
                .addTag(Tags.Items.GEMS_EMERALD);
        tag(ModTags.Items.PARTS)
                .addTag(ModTags.Items.PART_HANDLES)
                .addTag(ModTags.Items.PART_SWORD_BLADES)
                .addTag(ModTags.Items.PART_SWORD_GUARDS)
                .addTag(ModTags.Items.PART_SHOVEL_HEADS)
                .addTag(ModTags.Items.PART_PICKAXE_HEADS)
                .addTag(ModTags.Items.PART_AXE_HEADS)
                .addTag(ModTags.Items.PART_HOE_HEADS)
                .addTag(ModTags.Items.PART_SCREWDRIVER_HEADS)
                .addTag(ModTags.Items.PART_GEM_CUTTERS_BLADES);
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
        tag(ModTags.Items.PART_SCREWDRIVER_HEADS)
                .add(ModItems.SCREWDRIVER_HEAD.get());
        tag(ModTags.Items.PART_GEM_CUTTERS_BLADES)
                .add(ModItems.GEM_CUTTERS_BLADE.get());
        tag(ModTags.Items.TOOL_HANDLES)
                .add(Items.STICK, Items.BLAZE_ROD, Items.BREEZE_ROD)
                .addTag(Tags.Items.RODS_WOODEN)
                .addTag(Tags.Items.RODS_BLAZE)
                .addTag(Tags.Items.RODS_BREEZE);
        tag(ModTags.Items.TOOL_BINDINGS)
                .addTag(ModTags.Items.MATERIALS);
        tag(ModTags.Items.TOOL_WRAPS)
                .add(Items.LEATHER);
        tag(ModTags.Items.TOOL_FOCI)
                .add(Items.AMETHYST_SHARD);
        tag(ModTags.Items.TREATMENT_CATALYSTS)
                .add(Items.BLAZE_POWDER, Items.MAGMA_CREAM, Items.NETHERITE_SCRAP, Items.ECHO_SHARD, Items.SCULK_CATALYST);
        tag(ModTags.Items.LAPIDARY_ABRASIVES)
                .add(ModItems.DIAMOND_POWDER.get());
        tag(ModTags.Items.KNAPPING_TOOLS)
                .add(Items.FLINT);
    }
}
