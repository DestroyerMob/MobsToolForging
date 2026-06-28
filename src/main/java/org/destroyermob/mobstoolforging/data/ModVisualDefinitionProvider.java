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
    private final PackOutput.PathProvider examples;

    public ModVisualDefinitionProvider(PackOutput output) {
        this.materialVisuals = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/material_visuals");
        this.toolVisuals = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/tool_visuals");
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
                MaterialCatalog.LEATHER,
                null,
                null
        ), examples.json(modLoc("iron_pickaxe"))));
        futures.add(DataProvider.saveStable(output, example(
                ToolKind.PICKAXE,
                MaterialCatalog.DIAMOND,
                MaterialCatalog.DARK_OAK,
                MaterialCatalog.COPPER,
                null,
                null,
                null
        ), examples.json(modLoc("diamond_pickaxe"))));
        futures.add(DataProvider.saveStable(output, example(
                ToolKind.SWORD,
                MaterialCatalog.IRON,
                MaterialCatalog.BLAZE,
                null,
                null,
                null,
                MaterialCatalog.NETHER
        ), examples.json(modLoc("nether_treated_iron_sword"))));
        futures.add(DataProvider.saveStable(output, example(
                ToolKind.PICKAXE,
                MaterialCatalog.DIAMOND,
                MaterialCatalog.OAK,
                MaterialCatalog.COPPER,
                null,
                MaterialCatalog.AMETHYST,
                null
        ), examples.json(modLoc("amethyst_focused_diamond_pickaxe"))));
    }

    private JsonObject toolVisual(ToolKind toolKind) {
        JsonObject json = new JsonObject();
        json.addProperty("type", modLoc("tool_visual").toString());
        json.addProperty("canvas", 16);
        json.addProperty("large_canvas", 32);
        json.addProperty("large_in_hand", true);
        JsonArray layers = new JsonArray();
        layers.add(layer(toolKind, "handle", "handleMaterial", 1, false, false));
        layers.add(layer(toolKind, "wrap", "wrapMaterial", 2, true, false));
        layers.add(layer(toolKind, toolKind.partType(), "headMaterial", 3, false, false));
        layers.add(layer(toolKind, jointSlot(toolKind), "bindingMaterial", 4, toolKind != ToolKind.SWORD, false));
        layers.add(layer(toolKind, "focus", "focusMaterial", 5, true, true));
        layers.add(layer(toolKind, "treatment_overlay", "treatment", 6, true, true));
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
        if ("headMaterial".equals(materialFrom) || "bindingMaterial".equals(materialFrom)) {
            json.addProperty("part_template", modLoc("tool_templates/" + toolKind.id() + "/" + slot + "_part").toString());
        }
        json.addProperty("large_template", modLoc("tool_templates/" + toolKind.id() + "/large_" + slot).toString());
        json.addProperty("z", z);
        json.addProperty("optional", optional);
        json.addProperty("emissive_allowed", emissiveAllowed);
        return json;
    }

    private JsonObject example(ToolKind toolKind, ResourceLocation head, ResourceLocation handle, ResourceLocation binding, ResourceLocation wrap, ResourceLocation focus, ResourceLocation treatment) {
        JsonObject json = new JsonObject();
        json.addProperty("type", modLoc("tool_visual_example").toString());
        json.addProperty("tool_type", toolType(toolKind).toString());
        json.addProperty("head_material", head.toString());
        json.addProperty("handle_material", handle.toString());
        if (binding != null) {
            json.addProperty("binding_material", binding.toString());
        }
        if (wrap != null) {
            json.addProperty("wrap_material", wrap.toString());
        }
        if (focus != null) {
            json.addProperty("focus_material", focus.toString());
        }
        if (treatment != null) {
            json.addProperty("treatment", treatment.toString());
        }
        json.addProperty("quality", 100);
        return json;
    }

    private static String jointSlot(ToolKind toolKind) {
        return toolKind == ToolKind.SWORD ? "guard" : "binding";
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
                new MaterialVisualSpec(MaterialCatalog.GOLD, "metal", "fine_scratches", false, 0, palette(0xFF6D4A06, 0xFF9A6B0D, 0xFFD59A1B, 0xFFFFC943, 0xFFFFE37D, 0xFFFFF2B2)),
                new MaterialVisualSpec(MaterialCatalog.COPPER, "metal", "fine_scratches", false, 0, palette(0xFF4A2A12, 0xFF7A431D, 0xFFB46A2E, 0xFFD58A43, 0xFFFFB16A, 0xFFFFD8A0)),
                new MaterialVisualSpec(MaterialCatalog.NETHERITE, "metal", "fine_scratches", false, 0, palette(0xFF1E1B20, 0xFF312B33, 0xFF4A414B, 0xFF675E68, 0xFF8C818D, 0xFFB0A6B2)),
                new MaterialVisualSpec(MaterialCatalog.DIAMOND, "gem", "facets", false, 0, palette(0xFF0F5563, 0xFF178397, 0xFF24B9C8, 0xFF65E7EE, 0xFFB7FFFF, 0xFFFFFFFF)),
                new MaterialVisualSpec(MaterialCatalog.EMERALD, "gem", "facets", false, 0, palette(0xFF06451F, 0xFF0C6C32, 0xFF159949, 0xFF33C967, 0xFF89F2A8, 0xFFE3FFE9)),
                new MaterialVisualSpec(MaterialCatalog.FLINT, "stone", "chipped_edges", false, 0, palette(0xFF15171A, 0xFF252932, 0xFF3D4550, 0xFF59636D, 0xFF7C8792, 0xFFB7C0C8)),
                new MaterialVisualSpec(MaterialCatalog.OAK, "wood", "wood_grain", false, 0, palette(0xFF3B2613, 0xFF5A371A, 0xFF7D5126, 0xFFA5733A, 0xFFC99758, 0xFFE2BE80)),
                new MaterialVisualSpec(MaterialCatalog.DARK_OAK, "wood", "wood_grain", false, 0, palette(0xFF17100B, 0xFF24180F, 0xFF3A2818, 0xFF5A3D22, 0xFF7D5732, 0xFFA77A4A)),
                new MaterialVisualSpec(MaterialCatalog.BLAZE, "nether", "heat_cracks", true, 7, palette(0xFF4A1604, 0xFF7C2705, 0xFFB84307, 0xFFF07412, 0xFFFFB12E, 0xFFFFF0A4)),
                new MaterialVisualSpec(MaterialCatalog.BREEZE, "crystal", "facets", false, 0, palette(0xFF5B6170, 0xFF7D879A, 0xFFA3B2C7, 0xFFC3D7E8, 0xFFE4F5FF, 0xFFFFFFFF)),
                new MaterialVisualSpec(MaterialCatalog.LEATHER, "leather", "rough_wrap", false, 0, palette(0xFF2A170E, 0xFF4A2817, 0xFF704024, 0xFF9C6139, 0xFFC68555, 0xFFE4AF7C)),
                new MaterialVisualSpec(MaterialCatalog.PLANT_FIBER, "plant", "fibrous", false, 0, palette(0xFF2B2A12, 0xFF4A4A22, 0xFF6D7133, 0xFF969D4B, 0xFFC0C876, 0xFFE7E3A8)),
                new MaterialVisualSpec(MaterialCatalog.AMETHYST, "crystal", "facets", false, 0, palette(0xFF28164A, 0xFF422478, 0xFF6636A8, 0xFF905CE0, 0xFFC8A4FF, 0xFFF5E9FF)),
                new MaterialVisualSpec(MaterialCatalog.NETHER, "nether", "heat_cracks", true, 8, palette(0xFF160503, 0xFF2A0904, 0xFF4D1309, 0xFF8A260D, 0xFFFF6A1A, 0xFFFFD06A)),
                new MaterialVisualSpec(MaterialCatalog.SCULK, "sculk", "veins", true, 8, palette(0xFF05090A, 0xFF071417, 0xFF0A2428, 0xFF0E3D45, 0xFF0C8395, 0xFF4AF5FF))
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
}
