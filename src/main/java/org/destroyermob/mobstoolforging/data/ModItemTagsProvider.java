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
                .addTag(Tags.Items.INGOTS_COPPER)
                .addTag(Tags.Items.INGOTS_NETHERITE);
        tag(ModTags.Items.MATERIALS_GEMS)
                .addTag(Tags.Items.GEMS_DIAMOND)
                .addTag(Tags.Items.GEMS_EMERALD);
        tag(ModTags.Items.TOOL_HANDLES)
                .add(Items.STICK, Items.BLAZE_ROD, Items.BREEZE_ROD)
                .addTag(Tags.Items.RODS_WOODEN)
                .addTag(Tags.Items.RODS_BLAZE)
                .addTag(Tags.Items.RODS_BREEZE);
    }
}
