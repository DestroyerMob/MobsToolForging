package org.destroyermob.mobstoolforging.data;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolPartSpriteKey;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MobsToolForging.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        Block forge = ModBlocks.TOOL_FORGE.get();
        ModelFile forgeModel = new ModelFile.UncheckedModelFile(modLoc("block/tool_forge"));
        horizontalBlock(forge, forgeModel);
        simpleBlockItem(forge, forgeModel);

        Block lapidaryTable = ModBlocks.LAPIDARY_TABLE.get();
        ModelFile lapidaryModel = new ModelFile.UncheckedModelFile(modLoc("block/lapidary_table"));
        horizontalBlock(lapidaryTable, lapidaryModel);
        simpleBlockItem(lapidaryTable, lapidaryModel);

        Block patternCreationStation = ModBlocks.PATTERN_CREATION_STATION.get();
        ModelFile patternCreationStationModel = new ModelFile.UncheckedModelFile(modLoc("block/pattern_creation_station"));
        horizontalBlock(patternCreationStation, patternCreationStationModel);
        simpleBlockItem(patternCreationStation, patternCreationStationModel);

        Block toolmakersBench = ModBlocks.TOOLMAKERS_BENCH.get();
        ModelFile toolmakersBenchModel = new ModelFile.UncheckedModelFile(modLoc("block/toolmakers_bench"));
        horizontalBlock(toolmakersBench, toolmakersBenchModel);
        simpleBlockItem(toolmakersBench, toolmakersBenchModel);

        Block heatingForge = ModBlocks.HEATING_FORGE.get();
        ModelFile heatingForgeModel = new ModelFile.UncheckedModelFile(modLoc("block/heating_forge"));
        heatingForgeBlock(heatingForge, heatingForgeModel);
        simpleBlockItem(heatingForge, heatingForgeModel);

        Block crucible = ModBlocks.CRUCIBLE.get();
        ModelFile crucibleModel = new ModelFile.UncheckedModelFile(modLoc("block/crucible"));
        simpleBlock(crucible, crucibleModel);
        simpleBlockItem(crucible, crucibleModel);

        Block foundryForge = ModBlocks.FOUNDRY_FORGE.get();
        ModelFile foundryForgeModel = new ModelFile.UncheckedModelFile(modLoc("block/lava_foundry_forge"));
        heatingForgeBlock(foundryForge, foundryForgeModel);
        simpleBlockItem(foundryForge, foundryForgeModel);

        smithingHammerModel();
        itemModels().withExistingParent("flint_shard", mcLoc("item/generated")).texture("layer0", modLoc("item/flint_shard"));
        itemModels().withExistingParent("fire_stick", mcLoc("item/handheld")).texture("layer0", mcLoc("item/stick"));
        itemModels().withExistingParent("diamond_powder", mcLoc("item/generated")).texture("layer0", modLoc("item/diamond_powder"));
        patternModel(ModItems.PICKAXE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.AXE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.SHOVEL_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.HOE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.SWORD_BLADE_PATTERN.getId().getPath());
        patternModel(ModItems.SWORD_GUARD_PATTERN.getId().getPath());
        patternModel(ModItems.SMITHING_HAMMER_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.SCREWDRIVER_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.GEM_CUTTERS_BLADE_PATTERN.getId().getPath());
        patternModel(ModItems.TEMPLATE_PATTERN.getId().getPath());
        for (ToolKind toolKind : ToolKind.values()) {
            partModel(toolKind);
            toolModel(toolKind);
        }
        swordGuardPartModel();
    }

    private void heatingForgeBlock(Block block, ModelFile model) {
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(model)
                .rotationY(heatingForgeRotation(state.getValue(HeatingForgeBlock.FACING)))
                .build()
        );
    }

    private static int heatingForgeRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 180;
            case SOUTH -> 270;
            case WEST -> 0;
            default -> 90;
        };
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
                .transform(ItemDisplayContext.GUI).rotation(45, 45, 0).scale(0.5F).end()
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
        partModel(toolKind, toolKind.partType(), toolKind.partType(), toolKind.partType(), "headMaterial", toolKind.partType());
    }

    private void swordGuardPartModel() {
        partModel(ToolKind.SWORD, ToolPartData.SWORD_GUARD, ToolPartData.SWORD_GUARD, "guard", "bindingMaterial", ToolPartData.SWORD_GUARD);
    }

    private void partModel(ToolKind toolKind, String itemModelName, String partType, String partSlot, String materialFrom, String textureName) {
        ItemModelBuilder builder = itemModels().withExistingParent(itemModelName, mcLoc("item/generated"));
        builder.texture("particle", sourceTexture(MaterialCatalog.IRON, partTextureName(MaterialCatalog.IRON, textureName)));
        for (ResourceLocation material : MaterialCatalog.visualMaterialIds(materialFrom)) {
            ResourceLocation texture = sourceTexture(material, partTextureName(material, textureName));
            if (!sourceTextureExists(texture)) {
                continue;
            }
            builder.texture(
                    ToolPartSpriteKey.modelTextureKey(partSlot, material),
                    texture
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
        builder.texture("particle", sourceTexture(MaterialCatalog.IRON, toolTextureName(toolKind, toolKind.partType(), MaterialCatalog.IRON)));
        for (VisualLayerSpec layer : visualLayers(toolKind)) {
            for (ResourceLocation material : MaterialCatalog.visualMaterialIds(layer.materialFrom())) {
                ResourceLocation texture = sourceTexture(material, toolTextureName(toolKind, layer.slot(), material));
                if (!sourceTextureExists(texture)) {
                    continue;
                }
                builder.texture(
                        ToolPartSpriteKey.modelTextureKey(layer.slot(), material),
                        texture
                );
                if ("handle".equals(layer.slot())) {
                    ResourceLocation handleBodyTexture = sourceTexture(material, handleBodyTextureName(toolKind, material));
                    if (sourceTextureExists(handleBodyTexture)) {
                        builder.texture(ToolPartSpriteKey.handleBodyTextureKey(material), handleBodyTexture);
                    }
                }
            }
        }
    }

    private static ResourceLocation sourceTexture(ResourceLocation material, String spriteName) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, sourceTexturePath(material, spriteName));
    }

    private static String sourceTexturePath(ResourceLocation material, String spriteName) {
        return "source/tool_parts/" + sourceMaterialPath(material) + "/" + spriteName;
    }

    private static boolean sourceTextureExists(ResourceLocation texture) {
        String resourcePath = "/assets/" + texture.getNamespace() + "/textures/" + texture.getPath() + ".png";
        return ModBlockStateProvider.class.getResource(resourcePath) != null;
    }

    private static String materialPath(ResourceLocation material) {
        if (material.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return material.getPath();
        }
        return material.getNamespace() + "/" + material.getPath();
    }

    private static String partTextureName(ResourceLocation material, String partName) {
        return textureMaterialPrefix(material) + "_" + partName + "_part";
    }

    private static String toolTextureName(ToolKind toolKind, String slot, ResourceLocation material) {
        String materialPrefix = textureMaterialPrefix(material);
        if (slot.equals("handle")) {
            String toolSpecific = materialPrefix + "_" + toolKind.id() + "_handle_tool";
            if (sourceTextureExists(sourceTexture(material, toolSpecific))) {
                return toolSpecific;
            }
            return materialPrefix + "_handle_tool";
        }
        return materialPrefix + "_" + toolLayerPartName(toolKind, slot) + "_tool";
    }

    private static String handleBodyTextureName(ToolKind toolKind, ResourceLocation material) {
        return textureMaterialPrefix(material) + "_" + toolKind.id() + "_handle_body_tool";
    }

    private static String toolLayerPartName(ToolKind toolKind, String slot) {
        if (toolKind == ToolKind.SWORD && slot.equals("guard")) {
            return ToolPartData.SWORD_GUARD;
        }
        if (slot.equals(toolKind.partType())) {
            return toolKind.partType();
        }
        return toolKind.id() + "_" + slot;
    }

    private static String sourceMaterialPath(ResourceLocation material) {
        if (MaterialCatalog.OAK.equals(material)) {
            return "stick";
        }
        if (MaterialCatalog.BLAZE.equals(material)) {
            return "blaze_rod";
        }
        if (MaterialCatalog.BREEZE.equals(material)) {
            return "breeze_rod";
        }
        return materialPath(material);
    }

    private static String textureMaterialPrefix(ResourceLocation material) {
        return sourceMaterialPath(material).replace('/', '_').replace('-', '_');
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
