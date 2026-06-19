package org.destroyermob.mobstoolforging.data;

import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModVisualDefinitionProvider implements DataProvider {
    private static final int[] GREY_KEYS = {63, 102, 140, 178, 216, 255};
    private static final String SOURCE_TOOL_PART_TEXTURES = "/assets/" + MobsToolForging.MOD_ID + "/textures/source/tool_parts/";

    private final PackOutput.PathProvider materialVisuals;
    private final PackOutput.PathProvider toolVisuals;
    private final PackOutput.PathProvider examples;
    private final PackOutput.PathProvider generatedToolParts;

    public ModVisualDefinitionProvider(PackOutput output) {
        this.materialVisuals = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/material_visuals");
        this.toolVisuals = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/tool_visuals");
        this.examples = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "tooling/examples");
        this.generatedToolParts = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures/generated/tool_parts");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<MaterialVisualSpec> materials = materialSpecs();

        for (MaterialVisualSpec material : materials) {
            futures.add(DataProvider.saveStable(output, material.toJson(), materialVisuals.json(material.id())));
        }
        for (ToolKind toolKind : ToolKind.values()) {
            futures.add(DataProvider.saveStable(output, toolVisual(toolKind), toolVisuals.json(toolType(toolKind))));
        }
        futures.add(DataProvider.saveStable(output, greatswordToolVisual(), toolVisuals.json(ResourceLocation.fromNamespaceAndPath("mobs_more_weapons", "greatsword"))));
        writeExamples(output, futures);
        writeGeneratedSprites(output, futures, materials);

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
                MaterialCatalog.STICK,
                null,
                null,
                null,
                null
        ), examples.json(modLoc("iron_pickaxe"))));
        futures.add(DataProvider.saveStable(output, example(
                ToolKind.AXE,
                MaterialCatalog.GOLD,
                MaterialCatalog.BLAZE_ROD,
                null,
                null,
                null,
                null
        ), examples.json(modLoc("gold_axe"))));
        futures.add(DataProvider.saveStable(output, example(
                ToolKind.SWORD,
                MaterialCatalog.DIAMOND,
                MaterialCatalog.BREEZE_ROD,
                null,
                null,
                null,
                null
        ), examples.json(modLoc("diamond_sword"))));
    }

    private void writeGeneratedSprites(CachedOutput output, List<CompletableFuture<?>> futures, List<MaterialVisualSpec> materials) {
        Map<ResourceLocation, MaterialVisualSpec> byId = new LinkedHashMap<>();
        for (MaterialVisualSpec material : materials) {
            byId.put(material.id(), material);
        }

        for (ToolKind toolKind : ToolKind.values()) {
            for (ResourceLocation materialId : MaterialCatalog.starterMaterialIds()) {
                MaterialVisualSpec material = byId.get(materialId);
                if (material != null) {
                    futures.add(savePng(output, generatedTexturePath(materialId, toolKind.partType()), spriteFor(toolKind, toolKind.partType(), material)));
                }
            }
            for (ResourceLocation handle : MaterialCatalog.handleMaterialIds()) {
                MaterialVisualSpec material = byId.get(handle);
                if (material != null) {
                    futures.add(savePng(output, generatedTexturePath(handle, toolKind.id() + "_handle"), spriteFor(toolKind, "handle", material)));
                }
            }
        }
    }

    private JsonObject toolVisual(ToolKind toolKind) {
        JsonObject json = new JsonObject();
        json.addProperty("type", modLoc("tool_visual").toString());
        json.addProperty("canvas", 16);
        json.addProperty("large_canvas", 32);
        json.addProperty("large_in_hand", true);
        JsonArray layers = new JsonArray();
        layers.add(layer(toolKind, "handle", "handleMaterial", 1, false, false));
        layers.add(layer(toolKind, toolKind.partType(), "headMaterial", 2, false, false));
        json.add("layers", layers);
        return json;
    }

    private JsonObject greatswordToolVisual() {
        JsonObject json = new JsonObject();
        json.addProperty("type", modLoc("tool_visual").toString());
        json.addProperty("canvas", 16);
        json.addProperty("large_canvas", 32);
        json.addProperty("large_in_hand", true);
        json.addProperty("bridge_stub", true);
        JsonArray layers = new JsonArray();
        layers.add(layer("handle", "handleMaterial", "mobs_more_weapons:tool_templates/greatsword/handle", "mobs_more_weapons:generated/tool_parts/{material}/greatsword_handle", 1, false, false));
        layers.add(layer("wrap", "wrapMaterial", "mobs_more_weapons:tool_templates/greatsword/wrap", "mobs_more_weapons:generated/tool_parts/{material}/greatsword_wrap", 2, true, false));
        layers.add(layer("blade", "headMaterial", "mobs_more_weapons:tool_templates/greatsword/blade", "mobs_more_weapons:generated/tool_parts/{material}/greatsword_blade", 3, false, false));
        layers.add(layer("guard", "bindingMaterial", "mobs_more_weapons:tool_templates/greatsword/guard", "mobs_more_weapons:generated/tool_parts/{material}/greatsword_guard", 4, true, false));
        layers.add(layer("focus", "focusMaterial", "mobs_more_weapons:tool_templates/greatsword/focus", "mobs_more_weapons:generated/tool_parts/{material}/greatsword_focus", 5, true, true));
        layers.add(layer("treatment_overlay", "treatment", "mobs_more_weapons:tool_templates/greatsword/treatment_overlay", "mobs_more_weapons:generated/tool_parts/{material}/greatsword_treatment_overlay", 6, true, true));
        json.add("layers", layers);
        return json;
    }

    private JsonObject layer(ToolKind toolKind, String slot, String materialFrom, int z, boolean optional, boolean emissiveAllowed) {
        return layer(
                slot,
                materialFrom,
                modLoc("tool_templates/" + toolKind.id() + "/" + slot).toString(),
                MobsToolForging.MOD_ID + ":generated/tool_parts/{material}/" + generatedTextureName(toolKind, slot),
                z,
                optional,
                emissiveAllowed
        );
    }

    private JsonObject layer(String slot, String materialFrom, String template, String generated, int z, boolean optional, boolean emissiveAllowed) {
        JsonObject json = new JsonObject();
        json.addProperty("slot", slot);
        json.addProperty("material_from", materialFrom);
        json.addProperty("template", template);
        json.addProperty("large_template", largeTemplate(template));
        json.addProperty("generated", generated);
        json.addProperty("z", z);
        json.addProperty("optional", optional);
        json.addProperty("emissive_allowed", emissiveAllowed);
        return json;
    }

    private JsonObject damageLayer(ToolKind toolKind, int z) {
        JsonObject json = new JsonObject();
        json.addProperty("slot", "damage_overlay");
        json.addProperty("template", modLoc("tool_templates/" + toolKind.id() + "/damage_overlay").toString());
        json.addProperty("large_template", modLoc("tool_templates/" + toolKind.id() + "/large_damage_overlay").toString());
        json.addProperty("z", z);
        json.addProperty("optional", true);
        json.addProperty("emissive_allowed", false);
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

    private BufferedImage drawSprite(ToolKind toolKind, String slot, MaterialVisualSpec material) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        if (slot.equals("handle")) {
            drawHandle(image, toolKind, material);
        } else if (slot.equals("wrap")) {
            drawWrap(image, toolKind, material);
        } else if (slot.equals("binding") || slot.equals("guard")) {
            drawJoint(image, toolKind, material);
        } else if (slot.equals("focus")) {
            drawFocus(image, toolKind, material);
        } else if (slot.equals("treatment_overlay")) {
            drawTreatment(image, toolKind, material);
        } else {
            drawHead(image, toolKind, material);
        }
        return image;
    }

    private BufferedImage spriteFor(ToolKind toolKind, String slot, MaterialVisualSpec material) {
        BufferedImage source = loadSourceSprite(material.id(), sourceTextureName(toolKind, slot));
        if (source != null) {
            return source;
        }
        return drawSprite(toolKind, slot, material);
    }

    private BufferedImage loadSourceSprite(ResourceLocation material, String spriteName) {
        String path = SOURCE_TOOL_PART_TEXTURES + materialPath(material) + "/" + spriteName + ".png";
        try (InputStream stream = ModVisualDefinitionProvider.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new IllegalStateException("Source tool part sprite is not a readable PNG: " + path);
            }
            if (image.getWidth() != 16 || image.getHeight() != 16) {
                throw new IllegalStateException("Source tool part sprite must be 16x16: " + path);
            }
            BufferedImage normalized = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            var graphics = normalized.createGraphics();
            try {
                graphics.drawImage(image, 0, 0, null);
            } finally {
                graphics.dispose();
            }
            return normalized;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read source tool part sprite " + path, exception);
        }
    }

    private void drawHead(BufferedImage image, ToolKind toolKind, MaterialVisualSpec material) {
        switch (toolKind) {
            case SWORD -> {
                line(image, 8, 1, 8, 10, material.color(4));
                line(image, 7, 2, 7, 9, material.color(2));
                line(image, 9, 2, 9, 9, material.color(5));
                pixel(image, 8, 0, material.color(5));
            }
            case SHOVEL -> {
                diamond(image, 8, 4, 4, material);
                pixel(image, 8, 8, material.color(2));
            }
            case PICKAXE -> {
                line(image, 3, 3, 12, 3, material.color(4));
                line(image, 2, 4, 6, 4, material.color(2));
                line(image, 10, 4, 13, 4, material.color(5));
                pixel(image, 4, 5, material.color(1));
                pixel(image, 12, 5, material.color(3));
            }
            case AXE -> {
                rect(image, 6, 2, 11, 6, material.color(4));
                rect(image, 5, 3, 6, 7, material.color(2));
                pixel(image, 12, 4, material.color(5));
                pixel(image, 10, 7, material.color(1));
            }
            case HOE -> {
                line(image, 5, 3, 12, 3, material.color(4));
                line(image, 11, 4, 12, 8, material.color(2));
                pixel(image, 13, 3, material.color(5));
            }
        }
        addPattern(image, material);
    }

    private void drawHandle(BufferedImage image, ToolKind toolKind, MaterialVisualSpec material) {
        switch (toolKind) {
            case SWORD -> line(image, 8, 10, 8, 15, material.color(2));
            case SHOVEL -> line(image, 8, 7, 8, 15, material.color(2));
            case PICKAXE, AXE, HOE -> line(image, 8, 4, 8, 15, material.color(2));
        }
        line(image, 9, 6, 9, 14, material.color(4));
        addPattern(image, material);
    }

    private void drawWrap(BufferedImage image, ToolKind toolKind, MaterialVisualSpec material) {
        int y0 = toolKind == ToolKind.SWORD ? 11 : 10;
        for (int y = y0; y < 15; y += 2) {
            pixel(image, 7, y, material.color(2));
            pixel(image, 8, y, material.color(4));
            pixel(image, 9, y, material.color(2));
        }
    }

    private void drawJoint(BufferedImage image, ToolKind toolKind, MaterialVisualSpec material) {
        if (toolKind == ToolKind.SWORD) {
            line(image, 5, 10, 11, 10, material.color(3));
            pixel(image, 8, 9, material.color(5));
            return;
        }
        rect(image, 7, 4, 9, 6, material.color(3));
        pixel(image, 8, 3, material.color(5));
    }

    private void drawFocus(BufferedImage image, ToolKind toolKind, MaterialVisualSpec material) {
        int y = toolKind == ToolKind.SWORD ? 10 : 6;
        pixel(image, 8, y, material.color(5));
        pixel(image, 7, y, material.color(2));
        pixel(image, 9, y, material.color(2));
    }

    private void drawTreatment(BufferedImage image, ToolKind toolKind, MaterialVisualSpec material) {
        int color = withAlpha(material.color(5), material.emissive() ? 210 : 150);
        switch (toolKind) {
            case SWORD -> {
                pixel(image, 9, 2, color);
                pixel(image, 7, 5, color);
                pixel(image, 9, 8, color);
            }
            case SHOVEL -> {
                pixel(image, 8, 2, color);
                pixel(image, 6, 5, color);
                pixel(image, 10, 6, color);
            }
            case PICKAXE -> {
                pixel(image, 4, 3, color);
                pixel(image, 9, 3, color);
                pixel(image, 12, 4, color);
            }
            case AXE -> {
                pixel(image, 7, 3, color);
                pixel(image, 10, 5, color);
                pixel(image, 6, 6, color);
            }
            case HOE -> {
                pixel(image, 7, 3, color);
                pixel(image, 12, 4, color);
                pixel(image, 11, 7, color);
            }
        }
    }

    private void addPattern(BufferedImage image, MaterialVisualSpec material) {
        if (material.textureNoise().equals("facets")) {
            pixelIfFilled(image, 5, 3, material.color(5));
            pixelIfFilled(image, 10, 4, material.color(1));
            pixelIfFilled(image, 8, 7, material.color(5));
        } else if (material.textureNoise().equals("wood_grain")) {
            pixelIfFilled(image, 8, 9, material.color(1));
            pixelIfFilled(image, 9, 12, material.color(5));
        } else if (material.textureNoise().equals("heat_cracks")) {
            pixelIfFilled(image, 7, 4, material.color(5));
            pixelIfFilled(image, 10, 6, material.color(4));
        } else {
            pixelIfFilled(image, 4, 4, material.color(5));
            pixelIfFilled(image, 11, 5, material.color(1));
        }
    }

    private CompletableFuture<?> savePng(CachedOutput output, Path path, BufferedImage image) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", bytes);
                byte[] data = bytes.toByteArray();
                output.writeIfNeeded(path, data, Hashing.sha1().hashBytes(data));
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to save generated tool part sprite " + path, exception);
            }
        }, Util.backgroundExecutor());
    }

    private Path generatedTexturePath(ResourceLocation material, String spriteName) {
        return generatedToolParts.file(modLoc(materialPath(material) + "/" + spriteName), "png");
    }

    private static String generatedTextureName(ToolKind toolKind, String slot) {
        if (slot.equals(toolKind.partType())) {
            return toolKind.partType();
        }
        return toolKind.id() + "_" + slot;
    }

    private static String sourceTextureName(ToolKind toolKind, String slot) {
        return slot.equals("handle") ? "handle" : generatedTextureName(toolKind, slot);
    }

    private static String jointSlot(ToolKind toolKind) {
        return toolKind == ToolKind.SWORD ? "guard" : "binding";
    }

    private static String largeTemplate(String template) {
        int slash = template.lastIndexOf('/');
        if (slash < 0) {
            return template;
        }
        return template.substring(0, slash + 1) + "large_" + template.substring(slash + 1);
    }

    private static ResourceLocation toolType(ToolKind toolKind) {
        return modLoc(toolKind.id());
    }

    private static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, path);
    }

    private static String materialPath(ResourceLocation material) {
        if (material.getNamespace().equals(MobsToolForging.MOD_ID)) {
            return material.getPath();
        }
        return material.getNamespace() + "/" + material.getPath();
    }

    private static void line(BufferedImage image, int x0, int y0, int x1, int y1, int color) {
        int dx = Integer.compare(x1, x0);
        int dy = Integer.compare(y1, y0);
        int x = x0;
        int y = y0;
        pixel(image, x, y, color);
        while (x != x1 || y != y1) {
            if (x != x1) {
                x += dx;
            }
            if (y != y1) {
                y += dy;
            }
            pixel(image, x, y, color);
        }
    }

    private static void rect(BufferedImage image, int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                pixel(image, x, y, color);
            }
        }
    }

    private static void diamond(BufferedImage image, int centerX, int centerY, int radius, MaterialVisualSpec material) {
        for (int y = centerY - radius; y <= centerY + radius; y++) {
            int span = radius - Math.abs(centerY - y);
            for (int x = centerX - span; x <= centerX + span; x++) {
                int shade = y < centerY ? 4 : 2;
                pixel(image, x, y, material.color(shade));
            }
        }
        pixel(image, centerX, centerY - radius, material.color(5));
    }

    private static void pixelIfFilled(BufferedImage image, int x, int y, int color) {
        if (x >= 0 && x < 16 && y >= 0 && y < 16 && (image.getRGB(x, y) >>> 24) != 0) {
            image.setRGB(x, y, color);
        }
    }

    private static void pixel(BufferedImage image, int x, int y, int color) {
        if (x >= 0 && x < 16 && y >= 0 && y < 16) {
            image.setRGB(x, y, color);
        }
    }

    private static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    private static List<MaterialVisualSpec> materialSpecs() {
        return List.of(
                new MaterialVisualSpec(MaterialCatalog.IRON, "metal", "fine_scratches", false, 0, palette(0xFF3E4650, 0xFF59636F, 0xFF77828E, 0xFF9CA6B1, 0xFFC7D0D8, 0xFFF2F6F8)),
                new MaterialVisualSpec(MaterialCatalog.GOLD, "metal", "fine_scratches", false, 0, palette(0xFF6D4A06, 0xFF9A6B0D, 0xFFD59A1B, 0xFFFFC943, 0xFFFFE37D, 0xFFFFF2B2)),
                new MaterialVisualSpec(MaterialCatalog.DIAMOND, "gem", "facets", false, 0, palette(0xFF0F5563, 0xFF178397, 0xFF24B9C8, 0xFF65E7EE, 0xFFB7FFFF, 0xFFFFFFFF)),
                new MaterialVisualSpec(MaterialCatalog.STICK, "handle", "wood_grain", false, 0, palette(0xFF3B2613, 0xFF5A371A, 0xFF7D5126, 0xFFA5733A, 0xFFC99758, 0xFFE2BE80)),
                new MaterialVisualSpec(MaterialCatalog.BLAZE_ROD, "handle", "heat_cracks", true, 7, palette(0xFF4A1604, 0xFF7C2705, 0xFFB84307, 0xFFF07412, 0xFFFFB12E, 0xFFFFF0A4)),
                new MaterialVisualSpec(MaterialCatalog.BREEZE_ROD, "handle", "facets", false, 0, palette(0xFF5B6170, 0xFF7D879A, 0xFFA3B2C7, 0xFFC3D7E8, 0xFFE4F5FF, 0xFFFFFFFF))
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

        private int color(int index) {
            return palette[Math.max(0, Math.min(index, palette.length - 1))];
        }
    }
}
