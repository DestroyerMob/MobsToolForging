package org.destroyermob.mobstoolforging.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModVisualDefinitionProvider implements DataProvider {
    private static final int[] GREY_KEYS = {63, 102, 140, 178, 216, 255};

    private final PackOutput.PathProvider materialVisuals;
    private final PackOutput.PathProvider toolVisuals;
    private final PackOutput.PathProvider armorMaterialTextures;
    private final PackOutput.PathProvider examples;

    public ModVisualDefinitionProvider(PackOutput output) {
        this.materialVisuals = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/material_visuals");
        this.toolVisuals = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/tool_visuals");
        this.armorMaterialTextures = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/armor_material_textures");
        this.examples = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/examples");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (MaterialVisualSpec material : materialSpecs()) {
            futures.add(DataProvider.saveStable(output, material.toJson(), materialVisuals.json(material.id())));
        }
        for (ToolKind toolKind : ToolKind.values()) {
            futures.add(DataProvider.saveStable(output, toolVisual(toolKind), toolVisuals.json(toolType(toolKind))));
        }
        futures.add(DataProvider.saveStable(output, crossbowVisual(), toolVisuals.json(modLoc("crossbow"))));
        for (ArmorMaterialTextureSpec material : armorMaterialTextureSpecs()) {
            futures.add(DataProvider.saveStable(output, armorMaterialTexture(material), armorMaterialTextures.json(material.id())));
        }
        writeExamples(output, futures);

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Mobs Tool Forging visual definitions";
    }

    private void writeExamples(CachedOutput output, List<CompletableFuture<?>> futures) {
        futures.add(DataProvider.saveStable(output, example(
                ToolKind.PICKAXE,
                MaterialCatalog.IRON,
                MaterialCatalog.OAK,
                null,
                null
        ), examples.json(modLoc("iron_pickaxe"))));
        futures.add(DataProvider.saveStable(output, example(
                ToolKind.PICKAXE,
                MaterialCatalog.DIAMOND,
                MaterialCatalog.OAK,
                null,
                null
        ), examples.json(modLoc("diamond_pickaxe"))));
    }

    private JsonObject toolVisual(ToolKind toolKind) {
        if (toolKind == ToolKind.MATTOCK) {
            return mattockToolVisual();
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", modLoc("tool_visual").toString());
        json.addProperty("canvas", 16);
        json.addProperty("large_canvas", 32);
        json.addProperty("large_in_hand", false);
        JsonArray layers = new JsonArray();
        layers.add(layer(toolKind, "handle", "handleMaterial", 1, true, false));
        layers.add(layer(toolKind, toolKind.partType(), "headMaterial", 2, true, false));
        if (toolKind == ToolKind.SWORD) {
            layers.add(layer(toolKind, "guard", "guardMaterial", 3, true, false));
        }
        layers.add(layer(toolKind, treatmentSlot(toolKind), "treatment", 4, true, true));
        json.add("layers", layers);
        return json;
    }

    private JsonObject mattockToolVisual() {
        JsonObject json = new JsonObject();
        json.addProperty("type", modLoc("tool_visual").toString());
        json.addProperty("canvas", 16);
        json.addProperty("large_canvas", 32);
        json.addProperty("large_in_hand", false);
        JsonArray layers = new JsonArray();
        layers.add(layer(ToolKind.MATTOCK, "handle", "handleMaterial", 1, true, false));
        layers.add(layer(ToolKind.MATTOCK, "mattock_tool_axe", "headMaterial", 2, false, false));
        layers.add(layer(ToolKind.MATTOCK, "mattock_tool_hoe", "guardMaterial", 2, false, false));
        layers.add(layer(ToolKind.MATTOCK, treatmentSlot(ToolKind.MATTOCK), "treatment", 4, true, true));
        json.add("layers", layers);
        return json;
    }

    private JsonObject crossbowVisual() {
        JsonObject json = new JsonObject();
        json.addProperty("type", modLoc("tool_visual").toString());
        json.addProperty("canvas", 16);
        json.addProperty("large_canvas", 16);
        json.addProperty("large_in_hand", false);
        JsonArray layers = new JsonArray();

        JsonObject body = new JsonObject();
        body.addProperty("slot", "crossbow_body");
        body.addProperty("template", modLoc("item/crossbow_body").toString());
        body.addProperty("tool_template", modLoc("item/crossbow_body").toString());
        body.addProperty("part_template", modLoc("item/crossbow_body_part").toString());
        body.addProperty("z", 1);
        body.addProperty("optional", false);
        body.addProperty("emissive_allowed", false);
        layers.add(body);

        JsonObject limbs = new JsonObject();
        limbs.addProperty("slot", "crossbow_limbs");
        limbs.addProperty("material_from", "headMaterial");
        limbs.addProperty("template", modLoc("item/crossbow_limbs").toString());
        limbs.addProperty("tool_template", modLoc("item/crossbow_limbs").toString());
        limbs.addProperty("part_template", modLoc("item/crossbow_limbs").toString());
        limbs.addProperty("z", 2);
        limbs.addProperty("optional", false);
        limbs.addProperty("emissive_allowed", false);
        layers.add(limbs);

        JsonObject string = new JsonObject();
        string.addProperty("slot", "crossbow_string");
        string.addProperty("material_from", "guardMaterial");
        string.addProperty("template", modLoc("item/string_crossbow_string_standby").toString());
        string.addProperty("tool_template", modLoc("item/string_crossbow_string_standby").toString());
        string.addProperty("part_template", modLoc("item/string_crossbow_string_standby").toString());
        string.addProperty("z", 3);
        string.addProperty("optional", false);
        string.addProperty("emissive_allowed", false);
        layers.add(string);
        json.add("layers", layers);
        return json;
    }

    private JsonObject layer(ToolKind toolKind, String slot, String materialFrom, int z, boolean optional, boolean emissiveAllowed) {
        JsonObject json = new JsonObject();
        json.addProperty("slot", slot);
        json.addProperty("material_from", materialFrom);
        if ("handleMaterial".equals(materialFrom)) {
            json.addProperty("handle_strategy", "default_handle");
            json.addProperty("handle_template", modLoc("tool_templates/" + toolKind.id() + "/" + slot).toString());
        } else {
            json.addProperty("tool_template", modLoc("tool_templates/" + toolKind.id() + "/" + slot).toString());
        }
        json.addProperty("template", modLoc("tool_templates/" + toolKind.id() + "/" + slot).toString());
        if ("headMaterial".equals(materialFrom) || "guardMaterial".equals(materialFrom)) {
            json.addProperty("part_template", modLoc("tool_templates/" + toolKind.id() + "/" + slot + "_part").toString());
        }
        if ("treatment".equals(materialFrom)) {
            json.addProperty("texture_pattern", "source/tool_parts/{material}/{material}_" + slot + "_{usage}");
        }
        json.addProperty("large_template", modLoc("tool_templates/" + toolKind.id() + "/large_" + slot).toString());
        json.addProperty("z", z);
        json.addProperty("optional", optional);
        json.addProperty("emissive_allowed", emissiveAllowed);
        return json;
    }

    private static String treatmentSlot(ToolKind toolKind) {
        return toolKind.id() + "_treatment";
    }

    private JsonObject example(ToolKind toolKind, ResourceLocation head, ResourceLocation handle, ResourceLocation guard, ResourceLocation treatment) {
        JsonObject json = new JsonObject();
        json.addProperty("type", modLoc("tool_visual_example").toString());
        json.addProperty("tool_type", toolType(toolKind).toString());
        json.addProperty("head_material", head.toString());
        json.addProperty("handle_material", handle.toString());
        if (guard != null) {
            json.addProperty("guard_material", guard.toString());
        }
        if (treatment != null) {
            json.addProperty("treatment", treatment.toString());
        }
        json.addProperty("quality", 100);
        return json;
    }

    private JsonObject armorMaterialTexture(ArmorMaterialTextureSpec material) {
        JsonObject json = new JsonObject();
        json.addProperty("material", material.id().toString());
        json.addProperty("texture", material.texture().toString());
        if (material.tintItemTextures()) {
            json.addProperty("tint_item_textures", true);
        }
        JsonObject itemTextures = new JsonObject();
        itemTextures.addProperty("helmet", material.helmetTexture().toString());
        itemTextures.addProperty("chestplate", material.chestplateTexture().toString());
        itemTextures.addProperty("leggings", material.leggingsTexture().toString());
        itemTextures.addProperty("boots", material.bootsTexture().toString());
        json.add("item_textures", itemTextures);
        return json;
    }

    private static ResourceLocation toolType(ToolKind toolKind) {
        return modLoc(toolKind.id());
    }

    private static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    private static List<MaterialVisualSpec> materialSpecs() {
        return List.of(
                new MaterialVisualSpec(MaterialCatalog.IRON, "metal", "fine_scratches", false, 0, palette(0xFF3E4650, 0xFF59636F, 0xFF77828E, 0xFF9CA6B1, 0xFFC7D0D8, 0xFFF2F6F8)),
                new MaterialVisualSpec(MaterialCatalog.STEEL, "metal", "fine_scratches", false, 0, palette(0xFF303A43, 0xFF52616E, 0xFF788896, 0xFFA5B2BD, 0xFFD2DAE0, 0xFFF8FAFC)),
                new MaterialVisualSpec(MaterialCatalog.BRONZE, "metal", "fine_scratches", false, 0, palette(0xFF49250E, 0xFF743B16, 0xFFA75A22, 0xFFD17D35, 0xFFF0AA62, 0xFFFFD89B)),
                new MaterialVisualSpec(MaterialCatalog.GOLD, "metal", "fine_scratches", false, 0, palette(0xFF6D4A06, 0xFF9A6B0D, 0xFFD59A1B, 0xFFFFC943, 0xFFFFE37D, 0xFFFFF2B2)),
                new MaterialVisualSpec(MaterialCatalog.COPPER, "metal", "fine_scratches", false, 0, palette(0xFF4A2A12, 0xFF7A431D, 0xFFB46A2E, 0xFFD58A43, 0xFFFFB16A, 0xFFFFD8A0)),
                new MaterialVisualSpec(MaterialCatalog.NETHERITE, "metal", "fine_scratches", false, 0, palette(0xFF1E1B20, 0xFF312B33, 0xFF4A414B, 0xFF675E68, 0xFF8C818D, 0xFFB0A6B2)),
                new MaterialVisualSpec(MaterialCatalog.DIAMOND, "gem", "facets", false, 0, palette(0xFF0F5563, 0xFF178397, 0xFF24B9C8, 0xFF65E7EE, 0xFFB7FFFF, 0xFFFFFFFF)),
                new MaterialVisualSpec(MaterialCatalog.EMERALD, "gem", "facets", false, 0, palette(0xFF06451F, 0xFF0C6C32, 0xFF159949, 0xFF33C967, 0xFF89F2A8, 0xFFE3FFE9)),
                new MaterialVisualSpec(MaterialCatalog.AMETHYST, "gem", "facets", false, 0, palette(0xFF321456, 0xFF5D2A8C, 0xFF8A3FB8, 0xFFB26BE0, 0xFFD9A6F2, 0xFFF5E8FF)),
                new MaterialVisualSpec(MaterialCatalog.RUBY, "gem", "facets", false, 0, palette(0xFF4A0610, 0xFF7A0B1A, 0xFFB5152C, 0xFFE83E52, 0xFFFF8A98, 0xFFFFD8DD)),
                new MaterialVisualSpec(MaterialCatalog.SAPPHIRE, "gem", "facets", false, 0, palette(0xFF071A4A, 0xFF0B2E78, 0xFF174BB0, 0xFF3C78E8, 0xFF8AB7FF, 0xFFDCEAFF)),
                new MaterialVisualSpec(MaterialCatalog.TOPAZ, "gem", "facets", false, 0, palette(0xFF6A2D05, 0xFF9A4B08, 0xFFD8730E, 0xFFF5A623, 0xFFFFD16A, 0xFFFFF2BF)),
                new MaterialVisualSpec(MaterialCatalog.LEATHER, "leather", "wood_grain", false, 0, palette(0xFF4D2B1C, 0xFF6F4528, 0xFF8A5A35, 0xFFA9784C, 0xFFC99B6B, 0xFFE5C18F)),
                new MaterialVisualSpec(MaterialCatalog.FLINT, "stone", "chipped_edges", false, 0, palette(0xFF15171A, 0xFF252932, 0xFF3D4550, 0xFF59636D, 0xFF7C8792, 0xFFB7C0C8)),
                new MaterialVisualSpec(MaterialCatalog.OAK, "wood", "wood_grain", false, 0, palette(0xFF3B2613, 0xFF5A371A, 0xFF7D5126, 0xFFA5733A, 0xFFC99758, 0xFFE2BE80)),
                new MaterialVisualSpec(MaterialCatalog.DARK_OAK, "wood", "wood_grain", false, 0, palette(0xFF17100B, 0xFF24180F, 0xFF3A2818, 0xFF5A3D22, 0xFF7D5732, 0xFFA77A4A)),
                new MaterialVisualSpec(MaterialCatalog.BLAZE, "nether", "heat_cracks", true, 7, palette(0xFF4A1604, 0xFF7C2705, 0xFFB84307, 0xFFF07412, 0xFFFFB12E, 0xFFFFF0A4)),
                new MaterialVisualSpec(MaterialCatalog.BREEZE, "crystal", "facets", false, 0, palette(0xFF5B6170, 0xFF7D879A, 0xFFA3B2C7, 0xFFC3D7E8, 0xFFE4F5FF, 0xFFFFFFFF)),
                new MaterialVisualSpec(MaterialCatalog.SPIDER_SILK, "fiber", "fine_threads", false, 0, palette(0xFF4B4B4B, 0xFF6B6B6B, 0xFF929292, 0xFFB6B6B6, 0xFFD4D4D4, 0xFFF2F2F2)),
                new MaterialVisualSpec(MaterialCatalog.PLANT_FIBER, "fiber", "fine_threads", false, 0, palette(0xFF3E481E, 0xFF59652A, 0xFF77853A, 0xFF98A94F, 0xFFBDCF6A, 0xFFE0F59A)),
                new MaterialVisualSpec(MaterialCatalog.BLAZE_THREAD, "fiber", "fine_threads", true, 7, palette(0xFF4A1604, 0xFF7C2705, 0xFFB84307, 0xFFF07412, 0xFFFFB12E, 0xFFFFF0A4))
        );
    }

    private static List<ArmorMaterialTextureSpec> armorMaterialTextureSpecs() {
        return List.of(
                tintedLeatherArmor(MaterialCatalog.LEATHER, ResourceLocation.withDefaultNamespace("item/leather")),
                vanillaArmor(MaterialCatalog.IRON, ResourceLocation.withDefaultNamespace("block/iron_block"), "iron"),
                tintedLeatherArmor(MaterialCatalog.STEEL, ResourceLocation.withDefaultNamespace("block/iron_block")),
                tintedLeatherArmor(MaterialCatalog.BRONZE, ResourceLocation.withDefaultNamespace("block/copper_block")),
                vanillaArmor(MaterialCatalog.GOLD, ResourceLocation.withDefaultNamespace("block/gold_block"), "golden"),
                tintedLeatherArmor(MaterialCatalog.COPPER, ResourceLocation.withDefaultNamespace("block/copper_block")),
                vanillaArmor(MaterialCatalog.NETHERITE, ResourceLocation.withDefaultNamespace("block/netherite_block"), "netherite"),
                vanillaArmor(MaterialCatalog.DIAMOND, ResourceLocation.withDefaultNamespace("block/diamond_block"), "diamond"),
                tintedLeatherArmor(MaterialCatalog.EMERALD, ResourceLocation.withDefaultNamespace("block/emerald_block")),
                tintedLeatherArmor(MaterialCatalog.AMETHYST, ResourceLocation.fromNamespaceAndPath("extragems", "block/amethyst_block")),
                tintedLeatherArmor(MaterialCatalog.RUBY, ResourceLocation.withDefaultNamespace("block/redstone_block")),
                tintedLeatherArmor(MaterialCatalog.SAPPHIRE, ResourceLocation.withDefaultNamespace("block/lapis_block")),
                tintedLeatherArmor(MaterialCatalog.TOPAZ, ResourceLocation.fromNamespaceAndPath("extragems", "block/topaz_block"))
        );
    }

    private static ArmorMaterialTextureSpec vanillaArmor(ResourceLocation material, ResourceLocation texture, String vanillaPrefix) {
        return materialArmor(material, texture);
    }

    private static ArmorMaterialTextureSpec tintedLeatherArmor(ResourceLocation material, ResourceLocation texture) {
        return materialArmor(material, texture);
    }

    private static ArmorMaterialTextureSpec materialArmor(ResourceLocation material, ResourceLocation texture) {
        return new ArmorMaterialTextureSpec(
                material,
                texture,
                modLoc("item/armor/helmet"),
                modLoc("item/armor/chestplate"),
                modLoc("item/armor/leggings"),
                modLoc("item/armor/boots"),
                true
        );
    }

    private static int[] palette(int c0, int c1, int c2, int c3, int c4, int c5) {
        return new int[]{c0, c1, c2, c3, c4, c5};
    }

    private record MaterialVisualSpec(ResourceLocation id, String family, String textureNoise, boolean emissive, int luminosity, int[] palette) {
        private JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", modLoc("material_visual").toString());
            json.addProperty("family", family);
            JsonObject paletteJson = new JsonObject();
            for (int i = 0; i < GREY_KEYS.length; i++) {
                paletteJson.addProperty(String.valueOf(GREY_KEYS[i]), String.format("0x%08X", palette[i]));
            }
            json.add("palette", paletteJson);
            json.addProperty("texture_noise", textureNoise);
            JsonArray fallbacks = new JsonArray();
            fallbacks.add(family);
            json.add("fallbacks", fallbacks);
            json.addProperty("emissive", emissive);
            if (luminosity > 0) {
                json.addProperty("luminosity", luminosity);
            }
            return json;
        }
    }

    private record ArmorMaterialTextureSpec(
            ResourceLocation id,
            ResourceLocation texture,
            ResourceLocation helmetTexture,
            ResourceLocation chestplateTexture,
            ResourceLocation leggingsTexture,
            ResourceLocation bootsTexture,
            boolean tintItemTextures
    ) {
    }
}
