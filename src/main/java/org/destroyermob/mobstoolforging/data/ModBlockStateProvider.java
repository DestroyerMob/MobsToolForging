package org.destroyermob.mobstoolforging.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MobsToolForging.MOD_ID, existingFileHelper);
        for (ToolKind toolKind : ToolKind.values()) {
            for (ResourceLocation material : MaterialCatalog.starterMaterialIds()) {
                trackGeneratedTexture(existingFileHelper, generatedTexturePath(material, toolKind.partType()));
            }
            for (ResourceLocation handle : MaterialCatalog.handleMaterialIds()) {
                trackGeneratedTexture(existingFileHelper, generatedTexturePath(handle, toolKind.id() + "_handle"));
            }
        }
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
        for (ToolKind toolKind : ToolKind.values()) {
            partModel(toolKind);
            toolModel(toolKind);
        }
    }

    private void partModel(ToolKind toolKind) {
        ItemModelBuilder builder = itemModels().withExistingParent(toolKind.partType(), mcLoc("item/generated"));
        addHeadTextures(builder, toolKind);
        builder.customLoader((modelBuilder, helper) -> new PartedItemModelBuilder(modelBuilder, helper, toolKind, true)).end();
    }

    private void toolModel(ToolKind toolKind) {
        ItemModelBuilder builder = itemModels().withExistingParent(toolKind.id(), mcLoc("item/handheld"));
        addHeadTextures(builder, toolKind);
        addHandleTextures(builder, toolKind);
        builder.customLoader((modelBuilder, helper) -> new PartedItemModelBuilder(modelBuilder, helper, toolKind, false)).end();
    }

    private void addHeadTextures(ItemModelBuilder builder, ToolKind toolKind) {
        builder.texture("particle", generatedTexture(MaterialCatalog.IRON, toolKind.partType()));
        for (ResourceLocation material : MaterialCatalog.starterMaterialIds()) {
            builder.texture(headKey(material), generatedTexture(material, toolKind.partType()));
        }
    }

    private void addHandleTextures(ItemModelBuilder builder, ToolKind toolKind) {
        for (ResourceLocation handle : MaterialCatalog.handleMaterialIds()) {
            builder.texture(handleKey(handle), generatedTexture(handle, toolKind.id() + "_handle"));
        }
    }

    private static void trackGeneratedTexture(ExistingFileHelper existingFileHelper, String path) {
        existingFileHelper.trackGenerated(
                ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path),
                PackType.CLIENT_RESOURCES,
                ".png",
                "textures"
        );
    }

    private static ResourceLocation generatedTexture(ResourceLocation material, String spriteName) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, generatedTexturePath(material, spriteName));
    }

    private static String generatedTexturePath(ResourceLocation material, String spriteName) {
        return "generated/tool_parts/" + materialPath(material) + "/" + spriteName;
    }

    private static String materialPath(ResourceLocation material) {
        if (material.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return material.getPath();
        }
        return material.getNamespace() + "/" + material.getPath();
    }

    private static String headKey(ResourceLocation material) {
        return "head_" + materialKey(material);
    }

    private static String handleKey(ResourceLocation material) {
        return "handle_" + materialKey(material);
    }

    private static String materialKey(ResourceLocation material) {
        if (material.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return material.getPath();
        }
        return material.getNamespace() + "_" + material.getPath().replace('/', '_');
    }

    private static class PartedItemModelBuilder extends CustomLoaderBuilder<ItemModelBuilder> {
        private final ToolKind toolKind;
        private final boolean partModel;

        private PartedItemModelBuilder(ItemModelBuilder parent, ExistingFileHelper existingFileHelper, ToolKind toolKind, boolean partModel) {
            super(
                    ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, partModel ? "parted_tool_part" : "parted_tool"),
                    parent,
                    existingFileHelper,
                    false
            );
            this.toolKind = toolKind;
            this.partModel = partModel;
        }

        @Override
        public com.google.gson.JsonObject toJson(com.google.gson.JsonObject json) {
            json = super.toJson(json);
            json.addProperty("tool", toolKind.id());
            json.addProperty("part_model", partModel);
            return json;
        }
    }
}
