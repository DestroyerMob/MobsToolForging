package org.destroyermob.mobstoolforging.data;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolPartSpriteKey;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MobsToolForging.MOD_ID, existingFileHelper);
        for (ToolKind toolKind : ToolKind.values()) {
            for (VisualLayerSpec layer : visualLayers(toolKind)) {
                for (ResourceLocation material : MaterialCatalog.visualMaterialIds(layer.materialFrom())) {
                    trackGeneratedTexture(existingFileHelper, generatedTexturePath(material, generatedTextureName(toolKind, layer.slot())));
                }
            }
        }
        for (ResourceLocation material : MaterialCatalog.visualMaterialIds("bindingMaterial")) {
            trackGeneratedTexture(existingFileHelper, generatedTexturePath(material, "sword_guard_part"));
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

        smithingHammerModel();
        itemModels().withExistingParent("flint_shard", mcLoc("item/generated")).texture("layer0", modLoc("item/flint_shard"));
        itemModels().withExistingParent("flint_knife", mcLoc("item/handheld")).texture("layer0", modLoc("item/flint_knife"));
        itemModels().withExistingParent("flint_hatchet", mcLoc("item/handheld")).texture("layer0", modLoc("item/flint_hatchet"));
        itemModels().withExistingParent("flint_pick", mcLoc("item/handheld")).texture("layer0", modLoc("item/flint_pick"));
        patternModel(ModItems.PICKAXE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.AXE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.SHOVEL_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.HOE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.SWORD_BLADE_PATTERN.getId().getPath());
        patternModel(ModItems.SWORD_GUARD_PATTERN.getId().getPath());
        for (ToolKind toolKind : ToolKind.values()) {
            partModel(toolKind);
            toolModel(toolKind);
        }
        swordGuardPartModel();
    }

    private void smithingHammerModel() {
        ItemModelBuilder builder = itemModels().getBuilder("smithing_hammer")
                .texture("0", mcLoc("block/oak_log"))
                .texture("1", mcLoc("block/stone"))
                .texture("particle", mcLoc("block/oak_log"));

        builder.transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, 90, 0).translation(0, 0, 2.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, 90, 0).translation(0, 0, 2.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 90, 0).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 90, 0).end()
                .transform(ItemDisplayContext.GROUND).translation(0, 2, 0).scale(0.5F).end()
                .transform(ItemDisplayContext.GUI).rotation(45, 45, -45).translation(0, -0.75F, 0).scale(0.5F).end()
                .end();

        var handle = builder.element().from(7, 0, 7).to(9, 20, 9);
        handle.face(Direction.NORTH).uvs(0, 0, 2, 16).texture("#0");
        handle.face(Direction.EAST).uvs(0, 0, 2, 16).texture("#0");
        handle.face(Direction.SOUTH).uvs(0, 0, 2, 16).texture("#0");
        handle.face(Direction.WEST).uvs(0, 0, 2, 16).texture("#0");
        handle.face(Direction.UP).uvs(0, 0, 2, 2).texture("#0");
        handle.face(Direction.DOWN).uvs(0, 0, 2, 2).texture("#0");
        handle.end();

        var head = builder.element().from(4, 18, 6).to(12, 21, 10);
        head.face(Direction.NORTH).uvs(0, 0, 8, 3).texture("#1");
        head.face(Direction.EAST).uvs(0, 0, 4, 3).texture("#1");
        head.face(Direction.SOUTH).uvs(1, 0, 9, 3).texture("#1");
        head.face(Direction.WEST).uvs(0, 0, 4, 3).texture("#1");
        head.face(Direction.UP).uvs(1, 0, 9, 4).texture("#1");
        head.face(Direction.DOWN).uvs(1, 0, 9, 4).texture("#1");
        head.end();
    }

    private void patternModel(String name) {
        itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", mcLoc("item/paper"));
    }

    private void partModel(ToolKind toolKind) {
        partModel(toolKind, toolKind.partType(), toolKind.partType(), toolKind.partType(), "headMaterial", generatedTextureName(toolKind, toolKind.partType()));
    }

    private void swordGuardPartModel() {
        partModel(ToolKind.SWORD, ToolPartData.SWORD_GUARD, ToolPartData.SWORD_GUARD, "guard", "bindingMaterial", "sword_guard_part");
    }

    private void partModel(ToolKind toolKind, String itemModelName, String partType, String partSlot, String materialFrom, String textureName) {
        ItemModelBuilder builder = itemModels().withExistingParent(itemModelName, mcLoc("item/generated"));
        builder.texture("particle", generatedTexture(MaterialCatalog.IRON, textureName));
        for (ResourceLocation material : MaterialCatalog.visualMaterialIds(materialFrom)) {
            builder.texture(
                    ToolPartSpriteKey.modelTextureKey(partSlot, material),
                    generatedTexture(material, textureName)
            );
        }
        builder.customLoader((modelBuilder, helper) -> new PartedItemModelBuilder(modelBuilder, helper, toolKind, true, partType, partSlot)).end();
    }

    private void toolModel(ToolKind toolKind) {
        ItemModelBuilder builder = itemModels().withExistingParent(toolKind.id(), mcLoc("item/handheld"));
        addVisualTextures(builder, toolKind);
        builder.customLoader((modelBuilder, helper) -> new PartedItemModelBuilder(modelBuilder, helper, toolKind, false, toolKind.partType(), toolKind.partType())).end();
    }

    private void addVisualTextures(ItemModelBuilder builder, ToolKind toolKind) {
        builder.texture("particle", generatedTexture(MaterialCatalog.IRON, toolKind.partType()));
        for (VisualLayerSpec layer : visualLayers(toolKind)) {
            for (ResourceLocation material : MaterialCatalog.visualMaterialIds(layer.materialFrom())) {
                builder.texture(
                        ToolPartSpriteKey.modelTextureKey(layer.slot(), material),
                        generatedTexture(material, generatedTextureName(toolKind, layer.slot()))
                );
            }
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

    private static String generatedTextureName(ToolKind toolKind, String slot) {
        if (slot.equals(toolKind.partType())) {
            return toolKind.partType();
        }
        return toolKind.id() + "_" + slot;
    }

    private static String jointSlot(ToolKind toolKind) {
        return toolKind == ToolKind.SWORD ? "guard" : "binding";
    }

    private static List<VisualLayerSpec> visualLayers(ToolKind toolKind) {
        return List.of(
                new VisualLayerSpec("handle", "handleMaterial"),
                new VisualLayerSpec("wrap", "wrapMaterial"),
                new VisualLayerSpec(toolKind.partType(), "headMaterial"),
                new VisualLayerSpec(jointSlot(toolKind), "bindingMaterial"),
                new VisualLayerSpec("focus", "focusMaterial"),
                new VisualLayerSpec("treatment_overlay", "treatment")
        );
    }

    private record VisualLayerSpec(String slot, String materialFrom) {
    }

    private static class PartedItemModelBuilder extends CustomLoaderBuilder<ItemModelBuilder> {
        private final ToolKind toolKind;
        private final boolean partModel;
        private final String partType;
        private final String partSlot;

        private PartedItemModelBuilder(ItemModelBuilder parent, ExistingFileHelper existingFileHelper, ToolKind toolKind, boolean partModel, String partType, String partSlot) {
            super(
                    ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, partModel ? "parted_tool_part" : "parted_tool"),
                    parent,
                    existingFileHelper,
                    false
            );
            this.toolKind = toolKind;
            this.partModel = partModel;
            this.partType = partType;
            this.partSlot = partSlot;
        }

        @Override
        public com.google.gson.JsonObject toJson(com.google.gson.JsonObject json) {
            json = super.toJson(json);
            json.addProperty("tool", toolKind.id());
            json.addProperty("visual", ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, toolKind.id()).toString());
            json.addProperty("part_model", partModel);
            if (partModel) {
                json.addProperty("part_type", partType);
                json.addProperty("part_slot", partSlot);
            }
            return json;
        }
    }
}
