package org.destroyermob.mobstoolforging.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MobsToolForging.MOD_ID, existingFileHelper);
        trackGeneratedTexture(existingFileHelper, "generated/tool_parts/diamond/sword_blade");
        trackGeneratedTexture(existingFileHelper, "generated/tool_parts/diamond/shovel_head");
        trackGeneratedTexture(existingFileHelper, "generated/tool_parts/diamond/pickaxe_head");
        trackGeneratedTexture(existingFileHelper, "generated/tool_parts/diamond/axe_head");
        trackGeneratedTexture(existingFileHelper, "generated/tool_parts/diamond/hoe_head");
    }

    @Override
    protected void registerStatesAndModels() {
        Block forge = ModBlocks.TOOL_FORGE.get();
        ModelFile forgeModel = models().withExistingParent("tool_forge", mcLoc("block/anvil"));
        horizontalBlock(forge, forgeModel);
        simpleBlockItem(forge, forgeModel);

        Block lapidaryTable = ModBlocks.LAPIDARY_TABLE.get();
        ModelFile lapidaryModel = models().cubeBottomTop("lapidary_table", mcLoc("block/smooth_stone"), mcLoc("block/smooth_stone"), mcLoc("block/amethyst_block"));
        horizontalBlock(lapidaryTable, lapidaryModel);
        simpleBlockItem(lapidaryTable, lapidaryModel);

        itemModels().withExistingParent("smithing_hammer", mcLoc("item/handheld")).texture("layer0", mcLoc("item/iron_axe"));
        itemModels().withExistingParent("sword_blade", mcLoc("item/generated")).texture("layer0", modLoc("generated/tool_parts/diamond/sword_blade"));
        itemModels().withExistingParent("shovel_head", mcLoc("item/generated")).texture("layer0", modLoc("generated/tool_parts/diamond/shovel_head"));
        itemModels().withExistingParent("pickaxe_head", mcLoc("item/generated")).texture("layer0", modLoc("generated/tool_parts/diamond/pickaxe_head"));
        itemModels().withExistingParent("axe_head", mcLoc("item/generated")).texture("layer0", modLoc("generated/tool_parts/diamond/axe_head"));
        itemModels().withExistingParent("hoe_head", mcLoc("item/generated")).texture("layer0", modLoc("generated/tool_parts/diamond/hoe_head"));
        itemModels().withExistingParent("sword", mcLoc("item/handheld")).texture("layer0", mcLoc("item/diamond_sword"));
        itemModels().withExistingParent("shovel", mcLoc("item/handheld")).texture("layer0", mcLoc("item/diamond_shovel"));
        itemModels().withExistingParent("pickaxe", mcLoc("item/handheld")).texture("layer0", mcLoc("item/diamond_pickaxe"));
        itemModels().withExistingParent("axe", mcLoc("item/handheld")).texture("layer0", mcLoc("item/diamond_axe"));
        itemModels().withExistingParent("hoe", mcLoc("item/handheld")).texture("layer0", mcLoc("item/diamond_hoe"));
    }

    private static void trackGeneratedTexture(ExistingFileHelper existingFileHelper, String path) {
        existingFileHelper.trackGenerated(
                ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path),
                PackType.CLIENT_RESOURCES,
                ".png",
                "textures"
        );
    }
}
