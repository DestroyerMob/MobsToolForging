package org.destroyermob.mobstoolforging.tools;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public final class BasicFinishedToolTextureCompiler {
    private static final Path TEMPLATE_ROOT = Path.of(
            "src",
            "main",
            "resources",
            "assets",
            "mobstoolforging",
            "textures",
            "tool_templates"
    );
    private static final Path HANDLE_MASK_ROOT = Path.of(
            "src",
            "main",
            "resources",
            "assets",
            "mobstoolforging",
            "textures",
            "source",
            "tool_parts",
            "handle_masks"
    );
    private static final String OUTPUT_FOLDER = "finished";
    private static final List<ToolSpec> TOOLS = List.of(
            new ToolSpec("sword", List.of("handle", "sword_blade", "guard")),
            new ToolSpec("shovel", List.of("handle", "shovel_head")),
            new ToolSpec("pickaxe", List.of("handle", "pickaxe_head")),
            new ToolSpec("axe", List.of("handle", "axe_head")),
            new ToolSpec("hoe", List.of("handle", "hoe_head"))
    );

    private BasicFinishedToolTextureCompiler() {
    }

    public static void main(String[] args) throws IOException {
        Path projectRoot = args.length >= 1 ? Path.of(args[0]) : Path.of("").toAbsolutePath();
        Path templateRoot = projectRoot.resolve(TEMPLATE_ROOT).normalize();
        Path outputRoot = args.length >= 2 ? projectRoot.resolve(args[1]).normalize() : templateRoot.resolve(OUTPUT_FOLDER);

        if (!Files.isDirectory(templateRoot)) {
            throw new IOException("Tool template root does not exist: " + templateRoot);
        }

        Files.createDirectories(outputRoot);
        for (ToolSpec tool : TOOLS) {
            writeCompiledTool(projectRoot, templateRoot, outputRoot, tool, false);
            writeCompiledTool(projectRoot, templateRoot, outputRoot, tool, true);
        }
    }

    private static void writeCompiledTool(Path projectRoot, Path templateRoot, Path outputRoot, ToolSpec tool, boolean large) throws IOException {
        BufferedImage compiled = null;
        for (String layer : tool.layers()) {
            for (LayerImage layerImage : layerImages(projectRoot, templateRoot, tool, layer, large)) {
                if (compiled == null) {
                    compiled = new BufferedImage(layerImage.image().getWidth(), layerImage.image().getHeight(), BufferedImage.TYPE_INT_ARGB);
                }
                ensureSameSize(compiled, layerImage.image(), layerImage.path());
                composite(compiled, toGrayscale(layerImage.image()));
            }
        }

        if (compiled == null) {
            throw new IOException("No layers were configured for " + tool.name());
        }

        Path output = outputRoot.resolve((large ? "large_" : "") + tool.name() + ".png");
        ImageIO.write(compiled, "png", output.toFile());
        System.out.println("Wrote " + output);
    }

    private static List<LayerImage> layerImages(Path projectRoot, Path templateRoot, ToolSpec tool, String layer, boolean large) throws IOException {
        if (!"handle".equals(layer)) {
            Path layerPath = templateRoot.resolve(tool.name()).resolve((large ? "large_" : "") + layer + ".png");
            return List.of(new LayerImage(layerPath, readLayer(layerPath)));
        }

        Path handlePath = templateRoot.resolve(tool.name()).resolve((large ? "large_" : "") + layer + ".png");
        BufferedImage handle = readLayer(handlePath);
        List<LayerImage> layers = new ArrayList<>();
        readOptionalHandleMask(projectRoot, tool, large, handle)
                .ifPresent(layers::add);
        layers.add(new LayerImage(handlePath, handle));
        return List.copyOf(layers);
    }

    private static java.util.Optional<LayerImage> readOptionalHandleMask(Path projectRoot, ToolSpec tool, boolean large, BufferedImage handle) throws IOException {
        Path handleMaskRoot = projectRoot.resolve(HANDLE_MASK_ROOT);
        Path largeMaskPath = handleMaskRoot.resolve("large_" + tool.name() + "_handle_mask.png");
        Path maskPath = Files.isRegularFile(largeMaskPath)
                ? largeMaskPath
                : handleMaskRoot.resolve(tool.name() + "_handle_mask.png");

        if (!Files.isRegularFile(maskPath)) {
            return java.util.Optional.empty();
        }

        BufferedImage mask = readLayer(maskPath);
        if (mask.getWidth() != handle.getWidth() || mask.getHeight() != handle.getHeight()) {
            System.out.println("Skipping handle mask with different dimensions: " + maskPath);
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new LayerImage(maskPath, mask));
    }

    private static BufferedImage readLayer(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("Missing required tool template layer: " + path);
        }
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null) {
            throw new IOException("Could not read PNG image: " + path);
        }
        return image;
    }

    private static void ensureSameSize(BufferedImage compiled, BufferedImage layer, Path layerPath) throws IOException {
        if (compiled.getWidth() != layer.getWidth() || compiled.getHeight() != layer.getHeight()) {
            throw new IOException("Template layer size does not match the compiled canvas: " + layerPath
                    + " is " + layer.getWidth() + "x" + layer.getHeight()
                    + ", expected " + compiled.getWidth() + "x" + compiled.getHeight());
        }
    }

    private static BufferedImage toGrayscale(BufferedImage source) {
        BufferedImage grayscale = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;
                int value = Math.round(red * 0.299F + green * 0.587F + blue * 0.114F);
                grayscale.setRGB(x, y, (alpha << 24) | (value << 16) | (value << 8) | value);
            }
        }
        return grayscale;
    }

    private static void composite(BufferedImage target, BufferedImage source) {
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int sourceArgb = source.getRGB(x, y);
                int sourceAlpha = (sourceArgb >>> 24) & 0xFF;
                if (sourceAlpha == 0) {
                    continue;
                }

                int targetArgb = target.getRGB(x, y);
                target.setRGB(x, y, sourceOver(targetArgb, sourceArgb));
            }
        }
    }

    private static int sourceOver(int targetArgb, int sourceArgb) {
        int sourceAlpha = (sourceArgb >>> 24) & 0xFF;
        int targetAlpha = (targetArgb >>> 24) & 0xFF;
        int inverseSourceAlpha = 255 - sourceAlpha;
        int outAlpha = sourceAlpha + targetAlpha * inverseSourceAlpha / 255;
        if (outAlpha == 0) {
            return 0;
        }

        int red = blendChannel(targetArgb >>> 16, targetAlpha, sourceArgb >>> 16, sourceAlpha, inverseSourceAlpha, outAlpha);
        int green = blendChannel(targetArgb >>> 8, targetAlpha, sourceArgb >>> 8, sourceAlpha, inverseSourceAlpha, outAlpha);
        int blue = blendChannel(targetArgb, targetAlpha, sourceArgb, sourceAlpha, inverseSourceAlpha, outAlpha);
        return (outAlpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int blendChannel(int targetChannel, int targetAlpha, int sourceChannel, int sourceAlpha, int inverseSourceAlpha, int outAlpha) {
        int source = (sourceChannel & 0xFF) * sourceAlpha;
        int target = (targetChannel & 0xFF) * targetAlpha * inverseSourceAlpha / 255;
        return (source + target) / outAlpha;
    }

    private record ToolSpec(String name, List<String> layers) {
    }

    private record LayerImage(Path path, BufferedImage image) {
    }
}
