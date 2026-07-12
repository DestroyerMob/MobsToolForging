package org.destroyermob.mobstoolforging.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.joml.Vector3f;

public final class PatternCutoutBakedModel implements BakedModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final float FRONT_Z = 7.5F;
    private static final float BACK_Z = 8.5F;
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private final TextureAtlasSprite boardSprite;
    private final ResourceLocation boardTexture;
    private final ModelState modelState;
    private final ItemTransforms transforms;
    private final ResolvedPartedItemModel fallback;
    private final Map<ResourceLocation, BakedModel> cache = new ConcurrentHashMap<>();
    private final ItemOverrides overrides;

    public PatternCutoutBakedModel(TextureAtlasSprite boardSprite, ResourceLocation boardTexture, ModelState modelState, ItemTransforms transforms) {
        this.boardSprite = boardSprite;
        this.boardTexture = boardTexture;
        this.modelState = modelState;
        this.transforms = transforms;
        this.fallback = new ResolvedPartedItemModel(bakeFullBoard(), boardSprite, transforms);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                if (stack.getItem() instanceof ToolTemplateItem pattern) {
                    return pattern.templateId(stack)
                            .map(templateId -> cache.computeIfAbsent(templateId, PatternCutoutBakedModel.this::compose))
                            .orElse(fallback);
                }
                return fallback;
            }
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return fallback.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return boardSprite;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public List<net.minecraft.client.renderer.RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return List.of(net.minecraft.client.renderer.RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
    }

    private BakedModel compose(ResourceLocation templateId) {
        ForgeTemplateDefinition template = ToolTypeRegistry.template(templateId).orElse(null);
        if (template == null) {
            warnOnce("missing_template|" + templateId, "Cannot render pattern cutout because template {} is not loaded.", templateId);
            return fallback;
        }
        Optional<ResourceLocation> cutoutTexture = cutoutTexture(template);
        if (cutoutTexture.isEmpty()) {
            warnOnce("missing_cutout|" + templateId, "Cannot render pattern cutout because template {} has no visual part mask.", templateId);
            return fallback;
        }

        TextureAtlasSprite cutoutSprite = sprite(cutoutTexture.get());
        if (isMissing(cutoutSprite)) {
            warnOnce("missing_cutout_texture|" + cutoutTexture.get(), "Cannot render pattern cutout because mask texture {} is missing from the atlas.", cutoutTexture.get());
            return fallback;
        }
        List<BakedQuad> quads = bakeBoardMinusMask(cutoutSprite);
        return quads.isEmpty() ? fallback : new ResolvedPartedItemModel(quads, boardSprite, transforms);
    }

    private Optional<ResourceLocation> cutoutTexture(ForgeTemplateDefinition template) {
        return PatternCutoutTextures.resolve(template);
    }

    private List<BakedQuad> bakeFullBoard() {
        return bakeRect(0, 0, 16, 16);
    }

    private List<BakedQuad> bakeBoardMinusMask(TextureAtlasSprite maskSprite) {
        List<BakedQuad> quads = new ArrayList<>();
        for (int y = 0; y < 16; y++) {
            int x = 0;
            while (x < 16) {
                while (x < 16 && !isVisibleBoardPixel(maskSprite, x, y)) {
                    x++;
                }
                int start = x;
                while (x < 16 && isVisibleBoardPixel(maskSprite, x, y)) {
                    x++;
                }
                if (start < x) {
                    quads.addAll(bakeRect(start, y, x, y + 1));
                }
            }
        }
        return List.copyOf(quads);
    }

    private boolean isVisibleBoardPixel(TextureAtlasSprite maskSprite, int x, int y) {
        return !isTransparent(boardSprite, x, y) && isTransparent(maskSprite, x, y);
    }

    private boolean isTransparent(TextureAtlasSprite sprite, int x, int y) {
        int sampleX = Math.min(sprite.contents().width() - 1, Math.max(0, x * sprite.contents().width() / 16));
        int sampleY = Math.min(sprite.contents().height() - 1, Math.max(0, y * sprite.contents().height() / 16));
        return sprite.contents().isTransparent(0, sampleX, sampleY);
    }

    private List<BakedQuad> bakeRect(int x0, int y0, int x1, int y1) {
        Vector3f from = new Vector3f(x0, 16 - y1, FRONT_Z);
        Vector3f to = new Vector3f(x1, 16 - y0, BACK_Z);
        BlockFaceUV uv = new BlockFaceUV(new float[]{x0, y0, x1, y1}, 0);
        BlockElementFace face = new BlockElementFace(null, BlockElementFace.NO_TINT, "#board", uv);
        return List.of(
                FACE_BAKERY.bakeQuad(from, to, face, boardSprite, Direction.NORTH, modelState, null, true),
                FACE_BAKERY.bakeQuad(from, to, face, boardSprite, Direction.SOUTH, modelState, null, true)
        );
    }

    private TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }

    private static boolean isMissing(TextureAtlasSprite sprite) {
        return MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    }

    private static void warnOnce(String key, String message, Object... args) {
        if (WARNED.add(key)) {
            MobsToolForging.LOGGER.warn(message, args);
        }
    }
}
