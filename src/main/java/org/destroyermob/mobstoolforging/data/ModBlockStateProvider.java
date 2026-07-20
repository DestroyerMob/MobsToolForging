package org.destroyermob.mobstoolforging.data;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.AshBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.LeatherStationBlock;
import org.destroyermob.mobstoolforging.world.LapidaryTableBlock;
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

        Block crudeAnvil = ModBlocks.CRUDE_ANVIL.get();
        ModelFile crudeAnvilModel = new ModelFile.UncheckedModelFile(modLoc("block/crude_anvil"));
        horizontalBlock(crudeAnvil, crudeAnvilModel);
        simpleBlockItem(crudeAnvil, crudeAnvilModel);

        Block lapidaryTable = ModBlocks.LAPIDARY_TABLE.get();
        ModelFile lapidaryModel = new ModelFile.UncheckedModelFile(modLoc("block/lapidary_table"));
        ModelFile invisibleLapidaryModel = models().getBuilder("block/invisible_lapidary_table")
                .texture("particle", mcLoc("block/iron_block"));
        getVariantBuilder(lapidaryTable).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LapidaryTableBlock.PART) == BedPart.FOOT ? lapidaryModel : invisibleLapidaryModel)
                .rotationY(horizontalRotation(state.getValue(LapidaryTableBlock.FACING)))
                .build()
        );
        simpleBlockItem(lapidaryTable, lapidaryModel);

        Block diamondSaw = ModBlocks.DIAMOND_SAW.get();
        ModelFile diamondSawModel = new ModelFile.UncheckedModelFile(modLoc("block/diamond_saw"));
        horizontalBlock(diamondSaw, diamondSawModel);
        simpleBlockItem(diamondSaw, diamondSawModel);

        ModelFile invisibleSawmillModel = models().getBuilder("block/invisible_sawmill")
                .texture("particle", mcLoc("block/oak_planks"));
        for (ModBlocks.SawmillVariant variant : ModBlocks.SAWMILL_VARIANTS) {
            sawmillBlock(variant, invisibleSawmillModel);
        }

        Block patternCreationStation = ModBlocks.PATTERN_CREATION_STATION.get();
        ModelFile patternCreationStationModel = new ModelFile.UncheckedModelFile(modLoc("block/pattern_creation_station"));
        horizontalBlock(patternCreationStation, patternCreationStationModel);
        simpleBlockItem(patternCreationStation, patternCreationStationModel);

        for (ModBlocks.PatternRackVariant variant : ModBlocks.PATTERN_RACK_VARIANTS) {
            Block patternRack = variant.block().get();
            ModelFile patternRackModel = patternRackModel(variant);
            horizontalBlock(patternRack, patternRackModel);
            simpleBlockItem(patternRack, patternRackModel);
        }

        for (ModBlocks.ToolmakerStationVariant variant : ModBlocks.TOOLMAKER_STATION_VARIANTS) {
            Block station = variant.block().get();
            ModelFile stationModel = toolmakerStationModel(variant);
            horizontalBlock(station, stationModel);
            simpleBlockItem(station, stationModel);
        }

        ModelFile invisibleLeatherStationModel = models().getBuilder("block/invisible_leather_station")
                .texture("particle", mcLoc("block/oak_planks"));
        for (ModBlocks.LeatherStationVariant variant : ModBlocks.LEATHER_STATION_VARIANTS) {
            leatherStationBlock(variant, invisibleLeatherStationModel);
        }

        for (ModBlocks.DryingRackVariant variant : ModBlocks.DRYING_RACK_VARIANTS) {
            Block dryingRack = variant.block().get();
            ModelFile dryingRackModel = dryingRackModel(variant);
            horizontalBlock(dryingRack, dryingRackModel);
            simpleBlockItem(dryingRack, dryingRackModel);
        }

        Block heatingForge = ModBlocks.HEATING_FORGE.get();
        ModelFile heatingForgeModel = new ModelFile.UncheckedModelFile(modLoc("block/heating_forge"));
        heatingForgeBlock(heatingForge, heatingForgeModel);
        simpleBlockItem(heatingForge, heatingForgeModel);

        Block lavaHeatingForge = ModBlocks.LAVA_HEATING_FORGE.get();
        ModelFile lavaHeatingForgeModel = new ModelFile.UncheckedModelFile(modLoc("block/lava_heating_forge"));
        heatingForgeBlock(lavaHeatingForge, lavaHeatingForgeModel);
        simpleBlockItem(lavaHeatingForge, lavaHeatingForgeModel);

        ModelFile foundryFuelTankModel = models().cubeAll("foundry_fuel_tank", mcLoc("block/glass"))
                .renderType("translucent");
        simpleBlockWithItem(ModBlocks.FOUNDRY_FUEL_TANK.get(), foundryFuelTankModel);

        ModelFile foundryGlassModel = new ModelFile.UncheckedModelFile(modLoc("block/foundry_glass"));
        simpleBlockWithItem(ModBlocks.FOUNDRY_GLASS.get(), foundryGlassModel);

        ModelFile foundryDrainModel = models().cubeAll("foundry_drain", mcLoc("block/polished_blackstone_bricks"));
        horizontalBlock(ModBlocks.FOUNDRY_DRAIN.get(), foundryDrainModel);
        simpleBlockItem(ModBlocks.FOUNDRY_DRAIN.get(), foundryDrainModel);

        ModelFile foundryFaucetModel = new ModelFile.UncheckedModelFile(modLoc("block/foundry_faucet"));
        getVariantBuilder(ModBlocks.FOUNDRY_FAUCET.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(foundryFaucetModel)
                .rotationY(horizontalRotation(state.getValue(org.destroyermob.mobstoolforging.world.FoundryFaucetBlock.FACING)))
                .build());
        simpleBlockItem(ModBlocks.FOUNDRY_FAUCET.get(), foundryFaucetModel);

        ModelFile foundryCastingTableModel = new ModelFile.UncheckedModelFile(modLoc("block/foundry_casting_table"));
        simpleBlockWithItem(ModBlocks.FOUNDRY_CASTING_TABLE.get(), foundryCastingTableModel);
        ModelFile foundryCastingBasinModel = new ModelFile.UncheckedModelFile(modLoc("block/foundry_casting_basin"));
        simpleBlockWithItem(ModBlocks.FOUNDRY_CASTING_BASIN.get(), foundryCastingBasinModel);

        ashBlock();

        ModelFile invisibleGroundModel = models().getBuilder("block/invisible_ground")
                .texture("particle", mcLoc("block/gravel"));
        horizontalBlock(ModBlocks.KNAPPING_FLINT.get(), invisibleGroundModel);
        horizontalBlock(ModBlocks.GROUND_TOOL_ASSEMBLY.get(), invisibleGroundModel);

        smithingHammerModel();
        itemModels().withExistingParent("pattern_board", mcLoc("item/generated")).texture("layer0", textureExists(modLoc("item/pattern_board")) ? modLoc("item/pattern_board") : mcLoc("block/oak_planks"));
        itemModels().withExistingParent("flint_shard", mcLoc("item/generated")).texture("layer0", modLoc("item/flint_shard"));
        itemModels().withExistingParent("plant_fiber", mcLoc("item/generated")).texture("layer0", modLoc("item/plant_fiber"));
        itemModels().withExistingParent("blaze_thread", mcLoc("item/generated")).texture("layer0", modLoc("item/blaze_thread"));
        itemModels().withExistingParent("fire_stick", mcLoc("item/handheld")).texture("layer0", mcLoc("item/stick"));
        itemModels().withExistingParent("diamond_powder", mcLoc("item/generated")).texture("layer0", modLoc("item/diamond_powder"));
        itemModels().withExistingParent("steel_ingot", mcLoc("item/generated")).texture("layer0", mcLoc("item/iron_ingot"));
        itemModels().withExistingParent("bronze_ingot", mcLoc("item/generated")).texture("layer0", mcLoc("item/copper_ingot"));
        cookingKnifeHeadModel();
        modularHelmetModel();
        modularChestplateModel();
        itemModels().withExistingParent("modular_leggings", mcLoc("item/generated")).texture("layer0", mcLoc("item/leather_leggings"));
        itemModels().withExistingParent("modular_boots", mcLoc("item/generated")).texture("layer0", mcLoc("item/leather_boots"));
        patternModel(ModItems.PICKAXE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.AXE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.SHOVEL_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.HOE_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.SWORD_BLADE_PATTERN.getId().getPath());
        patternModel(ModItems.SWORD_GUARD_PATTERN.getId().getPath());
        patternModel(ModItems.SMITHING_HAMMER_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.SCREWDRIVER_HEAD_PATTERN.getId().getPath());
        patternModel(ModItems.GEM_CUTTERS_BLADE_PATTERN.getId().getPath());
        patternModel(ModItems.HELMET_CHAINMAIL_PATTERN.getId().getPath());
        patternModel(ModItems.HELMET_PLATE_PATTERN.getId().getPath());
        patternModel(ModItems.CHESTPLATE_CHAINMAIL_PATTERN.getId().getPath());
        patternModel(ModItems.CHESTPLATE_BODY_PATTERN.getId().getPath());
        patternModel(ModItems.LEGGINGS_CHAINMAIL_PATTERN.getId().getPath());
        patternModel(ModItems.LEGGINGS_PLATE_PATTERN.getId().getPath());
        patternModel(ModItems.BOOTS_CHAINMAIL_PATTERN.getId().getPath());
        patternModel(ModItems.BOOTS_PLATE_PATTERN.getId().getPath());
        patternModel(ModItems.CROSSBOW_BODY_PATTERN.getId().getPath());
        patternModel(ModItems.CROSSBOW_LIMBS_PATTERN.getId().getPath());
        patternModel(ModItems.TEMPLATE_PATTERN.getId().getPath());
        castingMoldModel();
        armorPartModel(ModItems.HELMET_CHAINMAIL.getId().getPath(), mcLoc("item/chainmail_helmet"));
        armorPartModel(ModItems.HELMET_PLATE.getId().getPath(), mcLoc("item/iron_helmet"));
        armorPartModel(ModItems.CHESTPLATE_CHAINMAIL.getId().getPath(), mcLoc("item/chainmail_chestplate"));
        armorPartModel(ModItems.CHESTPLATE_BODY.getId().getPath(), mcLoc("item/iron_chestplate"));
        armorPartModel(ModItems.LEGGINGS_CHAINMAIL.getId().getPath(), mcLoc("item/chainmail_leggings"));
        armorPartModel(ModItems.LEGGINGS_PLATE.getId().getPath(), mcLoc("item/iron_leggings"));
        armorPartModel(ModItems.BOOTS_CHAINMAIL.getId().getPath(), mcLoc("item/chainmail_boots"));
        armorPartModel(ModItems.BOOTS_PLATE.getId().getPath(), mcLoc("item/iron_boots"));
        for (ToolKind toolKind : ToolKind.values()) {
            if (toolKind != ToolKind.MATTOCK) {
                partModel(toolKind);
            }
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

    private void ashBlock() {
        getVariantBuilder(ModBlocks.ASH.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(new ModelFile.UncheckedModelFile(modLoc("block/ash_layer_" + state.getValue(AshBlock.LAYERS))))
                .build()
        );
        itemModels().getBuilder("ash")
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/ash_layer_1")));
    }

    private ModelFile patternRackModel(ModBlocks.PatternRackVariant variant) {
        return models().withExistingParent("block/" + variant.id(), modLoc("block/template_pattern_rack"))
                .texture("log", variant.logTexture())
                .texture("planks", variant.planksTexture())
                .texture("particle", variant.logTexture());
    }

    private ModelFile toolmakerStationModel(ModBlocks.ToolmakerStationVariant variant) {
        if (variant.block() == ModBlocks.TOOLMAKERS_BENCH) {
            return new ModelFile.UncheckedModelFile(modLoc("block/toolmakers_bench"));
        }
        return models().withExistingParent("block/" + variant.id(), modLoc("block/template_toolmakers_bench"))
                .texture("top", variant.topTexture())
                .texture("side", variant.sideTexture())
                .texture("particle", variant.topTexture());
    }

    private void leatherStationBlock(ModBlocks.LeatherStationVariant variant, ModelFile invisibleModel) {
        Block station = variant.block().get();
        ModelFile stationModel = leatherStationModel(variant);
        getVariantBuilder(station).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LeatherStationBlock.PART) == BedPart.FOOT ? stationModel : invisibleModel)
                .rotationY(horizontalRotation(state.getValue(LeatherStationBlock.FACING)))
                .build()
        );
        simpleBlockItem(station, stationModel);
    }

    private void sawmillBlock(ModBlocks.SawmillVariant variant, ModelFile invisibleModel) {
        Block sawmill = variant.block().get();
        ModelFile sawmillModel = sawmillModel(variant);
        getVariantBuilder(sawmill).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LapidaryTableBlock.PART) == BedPart.FOOT ? sawmillModel : invisibleModel)
                .rotationY(horizontalRotation(state.getValue(LapidaryTableBlock.FACING)))
                .build()
        );
        simpleBlockItem(sawmill, sawmillModel);
    }

    private ModelFile sawmillModel(ModBlocks.SawmillVariant variant) {
        if (variant.block() == ModBlocks.SAWMILL) {
            return new ModelFile.UncheckedModelFile(modLoc("block/sawmill"));
        }
        return models().withExistingParent("block/" + variant.id(), modLoc("block/sawmill"))
                .texture("log", variant.logTexture())
                .texture("planks", variant.planksTexture())
                .texture("particle", mcLoc("block/stonecutter_saw"));
    }

    private ModelFile leatherStationModel(ModBlocks.LeatherStationVariant variant) {
        return models().withExistingParent("block/" + variant.id(), modLoc("block/template_leather_station"))
                .texture("log", variant.logTexture())
                .texture("planks", variant.planksTexture())
                .texture("display_left", mcLoc("item/leather"))
                .texture("display_right", modLoc("item/plant_fiber"))
                .texture("particle", variant.logTexture());
    }

    private ModelFile dryingRackModel(ModBlocks.DryingRackVariant variant) {
        return models().withExistingParent("block/" + variant.id(), modLoc("block/template_drying_rack"))
                .texture("planks", variant.planksTexture())
                .texture("particle", variant.planksTexture());
    }

    private static int horizontalRotation(Direction facing) {
        return ((int)facing.toYRot() + 180) % 360;
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

    private void modularHelmetModel() {
        itemModels().withExistingParent("modular_helmet", mcLoc("item/generated"))
                .texture("layer0", mcLoc("item/leather_helmet"));
    }

    private void modularChestplateModel() {
        itemModels().withExistingParent("modular_chestplate", mcLoc("item/generated"))
                .texture("layer0", mcLoc("item/leather_chestplate"));
    }

    private void patternModel(String name) {
        ResourceLocation texture = textureExists(modLoc("item/pattern_board")) ? modLoc("item/pattern_board") : mcLoc("block/oak_planks");
        ItemModelBuilder builder = itemModels().withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", texture)
                .texture("board", texture);
        builder.customLoader(PatternCutoutModelBuilder::new).end();
    }

    private void castingMoldModel() {
        ResourceLocation texture = mcLoc("block/gold_block");
        ItemModelBuilder builder = itemModels().withExistingParent(ModItems.CASTING_MOLD.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", texture)
                .texture("board", texture);
        builder.customLoader(PatternCutoutModelBuilder::new).end();
    }

    private void armorPartModel(String name, ResourceLocation texture) {
        itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", texture);
    }

    private void partModel(ToolKind toolKind) {
        partModel(toolKind, toolKind.partType(), toolKind.partType(), toolKind.partType(), "headMaterial", toolKind.partType());
    }

    private void swordGuardPartModel() {
        partModel(ToolKind.SWORD, ToolPartData.SWORD_GUARD, ToolPartData.SWORD_GUARD, "guard", "guardMaterial", ToolPartData.SWORD_GUARD);
    }

    private void cookingKnifeHeadModel() {
        ResourceLocation visualId = modLoc("cooking_knife");
        ItemModelBuilder builder = itemModels().withExistingParent("cooking_knife_head", mcLoc("item/generated"));
        builder.texture("particle", modLoc("tool_templates/cooking_knife/cooking_knife_head"));
        builder.customLoader((modelBuilder, helper) -> new PartedItemModelBuilder(
                modelBuilder,
                helper,
                java.util.Optional.empty(),
                visualId,
                true,
                ToolPartData.COOKING_KNIFE_HEAD,
                ToolPartData.COOKING_KNIFE_HEAD
        )).end();
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
        if (toolKind == ToolKind.MATTOCK) {
            builder.texture("template_mattock_tool_axe", modLoc("tool_templates/mattock/mattock_tool_axe"));
            builder.texture("template_mattock_tool_hoe", modLoc("tool_templates/mattock/mattock_tool_hoe"));
        }
        builder.customLoader((modelBuilder, helper) -> new PartedItemModelBuilder(modelBuilder, helper, toolKind, false, toolKind.partType(), toolKind.partType())).end();
    }

    private void addVisualTextures(ItemModelBuilder builder, ToolKind toolKind) {
        builder.texture("particle", particleTexture(toolKind));
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

    private ResourceLocation particleTexture(ToolKind toolKind) {
        for (VisualLayerSpec layer : visualLayers(toolKind)) {
            if (!"headMaterial".equals(layer.materialFrom())) {
                continue;
            }
            ResourceLocation exactTexture = sourceTexture(MaterialCatalog.IRON, toolTextureName(toolKind, layer.slot(), MaterialCatalog.IRON));
            if (sourceTextureExists(exactTexture)) {
                return exactTexture;
            }
            ResourceLocation templateTexture = toolTemplateTexture(toolKind, layer.slot());
            if (textureExists(templateTexture)) {
                return templateTexture;
            }
        }
        return sourceTexture(MaterialCatalog.IRON, toolTextureName(toolKind, toolKind.partType(), MaterialCatalog.IRON));
    }

    private ResourceLocation toolTemplateTexture(ToolKind toolKind, String slot) {
        return modLoc("tool_templates/" + toolKind.id() + "/" + slot);
    }

    private static ResourceLocation sourceTexture(ResourceLocation material, String spriteName) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, sourceTexturePath(material, spriteName));
    }

    private static String sourceTexturePath(ResourceLocation material, String spriteName) {
        return "source/tool_parts/" + sourceMaterialPath(material) + "/" + spriteName;
    }

    private static boolean sourceTextureExists(ResourceLocation texture) {
        return textureExists(texture);
    }

    private static boolean textureExists(ResourceLocation texture) {
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
        return slot;
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

    private static List<VisualLayerSpec> visualLayers(ToolKind toolKind) {
        if (toolKind == ToolKind.SWORD) {
            return List.of(
                    new VisualLayerSpec("handle", "handleMaterial"),
                    new VisualLayerSpec(toolKind.partType(), "headMaterial"),
                    new VisualLayerSpec("guard", "guardMaterial"),
                    new VisualLayerSpec(treatmentSlot(toolKind), "treatment")
            );
        }
        if (toolKind == ToolKind.MATTOCK) {
            return List.of(
                    new VisualLayerSpec("handle", "handleMaterial"),
                    new VisualLayerSpec("mattock_tool_axe", "headMaterial"),
                    new VisualLayerSpec("mattock_tool_hoe", "guardMaterial"),
                    new VisualLayerSpec(treatmentSlot(toolKind), "treatment")
            );
        }
        return List.of(
                new VisualLayerSpec("handle", "handleMaterial"),
                new VisualLayerSpec(toolKind.partType(), "headMaterial"),
                new VisualLayerSpec(treatmentSlot(toolKind), "treatment")
        );
    }

    private static String treatmentSlot(ToolKind toolKind) {
        return toolKind.id() + "_treatment";
    }

    private record VisualLayerSpec(String slot, String materialFrom) {
    }

    private static class PartedItemModelBuilder extends CustomLoaderBuilder<ItemModelBuilder> {
        private final java.util.Optional<ResourceLocation> toolTypeId;
        private final ResourceLocation visualId;
        private final boolean partModel;
        private final String partType;
        private final String partSlot;

        private PartedItemModelBuilder(ItemModelBuilder parent, ExistingFileHelper existingFileHelper, ToolKind toolKind, boolean partModel, String partType, String partSlot) {
            this(
                    parent,
                    existingFileHelper,
                    java.util.Optional.of(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, toolKind.id())),
                    ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, toolKind.id()),
                    partModel,
                    partType,
                    partSlot
            );
        }

        private PartedItemModelBuilder(ItemModelBuilder parent, ExistingFileHelper existingFileHelper, java.util.Optional<ResourceLocation> toolTypeId, ResourceLocation visualId, boolean partModel, String partType, String partSlot) {
            super(
                    ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, partModel ? "parted_tool_part" : "parted_tool"),
                    parent,
                    existingFileHelper,
                    false
            );
            this.toolTypeId = toolTypeId;
            this.visualId = visualId;
            this.partModel = partModel;
            this.partType = partType;
            this.partSlot = partSlot;
        }

        @Override
        public com.google.gson.JsonObject toJson(com.google.gson.JsonObject json) {
            json = super.toJson(json);
            if (toolTypeId.isPresent()) {
                json.addProperty("tool", toolTypeId.get().toString());
            }
            json.addProperty("visual", visualId.toString());
            json.addProperty("part_model", partModel);
            if (partModel) {
                json.addProperty("part_type", partType);
                json.addProperty("part_slot", partSlot);
            }
            return json;
        }
    }

    private static class PatternCutoutModelBuilder extends CustomLoaderBuilder<ItemModelBuilder> {
        private PatternCutoutModelBuilder(ItemModelBuilder parent, ExistingFileHelper existingFileHelper) {
            super(
                    ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "pattern_cutout"),
                    parent,
                    existingFileHelper,
                    false
            );
        }
    }
}
