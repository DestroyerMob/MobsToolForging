package org.destroyermob.mobstoolforging.client.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolVisualKey;

/** Builds the component-coloured crossbow while retaining vanilla charge and projectile states. */
public final class ComponentCrossbowBakedModel {
    private static final ResourceLocation BODY = texture("crossbow_body");
    private static final ResourceLocation LIMBS = texture("crossbow_limbs");
    private static final ResourceLocation WOODEN_LIMBS = texture("wooden_crossbow_limbs");
    private static final ResourceLocation STRING_STANDBY = texture("string_crossbow_string_standby");
    private static final ResourceLocation STRING_PULLING_0 = texture("string_crossbow_string_pulling_0");
    private static final ResourceLocation STRING_PULLING_1 = texture("string_crossbow_string_pulling_1");
    private static final ResourceLocation STRING_PULLING_2 = texture("string_crossbow_string_pulling_2");
    private static final ResourceLocation LOADED_BOLT = texture("crossbow_loaded_bolt");
    private static final ResourceLocation LOADED_FIREWORK = texture("crossbow_loaded_firework");

    private final BakedModel fallback;
    private final PartedToolQuadFactory quadFactory = new PartedToolQuadFactory(BlockModelRotation.X0_Y0);
    private final Map<VariantKey, BakedModel> variants = new ConcurrentHashMap<>();

    public ComponentCrossbowBakedModel(BakedModel fallback) {
        this.fallback = fallback;
    }

    public BakedModel resolve(ToolVisualKey construction, ItemStack stack, @Nullable LivingEntity entity) {
        VariantKey key = new VariantKey(
                construction.headMaterial(),
                construction.guardMaterial().orElse(MaterialCatalog.SPIDER_SILK),
                stringState(stack, entity),
                loadedProjectile(stack)
        );
        return variants.computeIfAbsent(key, this::compose);
    }

    private BakedModel compose(VariantKey key) {
        Map<Integer, List<BakedQuad>> layers = new LinkedHashMap<>();
        TextureAtlasSprite body = sprite(BODY);
        addLayer(layers, 0, quadFactory.bakeLayer(0, body));

        boolean woodenLimbs = MaterialCatalog.OAK.equals(key.limbMaterial());
        TextureAtlasSprite limbs = sprite(woodenLimbs ? WOODEN_LIMBS : LIMBS);
        int limbTint = woodenLimbs ? 0xFFFFFFFF : ToolMaterialVisualManager.INSTANCE.tintColor(key.limbMaterial());
        addLayer(layers, 1, quadFactory.bakeLayer(1, limbs, limbTint));

        TextureAtlasSprite string = sprite(key.stringState().texture());
        addLayer(layers, 2, quadFactory.bakeLayer(2, string, ToolMaterialVisualManager.INSTANCE.tintColor(key.stringMaterial())));
        if (key.projectile().texture() != null) {
            addLayer(layers, 3, quadFactory.bakeLayer(3, sprite(key.projectile().texture())));
        }
        return ResolvedPartedItemModel.compose(layers, body, fallback);
    }

    private static StringState stringState(ItemStack stack, @Nullable LivingEntity entity) {
        if (entity == null
                || !entity.isUsingItem()
                || !entity.getUseItem().is(stack.getItem())
                || CrossbowItem.isCharged(stack)) {
            return StringState.STANDBY;
        }
        int chargeDuration = Math.max(1, CrossbowItem.getChargeDuration(stack, entity));
        float progress = Math.min(1.0F, entity.getTicksUsingItem() / (float) chargeDuration);
        if (progress < 0.33F) {
            return StringState.PULLING_0;
        }
        return progress < 0.67F ? StringState.PULLING_1 : StringState.PULLING_2;
    }

    private static LoadedProjectile loadedProjectile(ItemStack stack) {
        if (!CrossbowItem.isCharged(stack)) {
            return LoadedProjectile.NONE;
        }
        return stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, net.minecraft.world.item.component.ChargedProjectiles.EMPTY).contains(Items.FIREWORK_ROCKET)
                ? LoadedProjectile.FIREWORK
                : LoadedProjectile.BOLT;
    }

    private static void addLayer(Map<Integer, List<BakedQuad>> layers, int z, List<BakedQuad> quads) {
        layers.merge(z, quads, (existing, additions) -> {
            java.util.ArrayList<BakedQuad> combined = new java.util.ArrayList<>(existing.size() + additions.size());
            combined.addAll(existing);
            combined.addAll(additions);
            return List.copyOf(combined);
        });
    }

    private static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "item/" + name);
    }

    private record VariantKey(ResourceLocation limbMaterial, ResourceLocation stringMaterial, StringState stringState, LoadedProjectile projectile) {
    }

    private enum StringState {
        STANDBY(STRING_STANDBY),
        PULLING_0(STRING_PULLING_0),
        PULLING_1(STRING_PULLING_1),
        PULLING_2(STRING_PULLING_2);

        private final ResourceLocation texture;

        StringState(ResourceLocation texture) {
            this.texture = texture;
        }

        private ResourceLocation texture() {
            return texture;
        }
    }

    private enum LoadedProjectile {
        NONE(null),
        BOLT(LOADED_BOLT),
        FIREWORK(LOADED_FIREWORK);

        @Nullable
        private final ResourceLocation texture;

        LoadedProjectile(@Nullable ResourceLocation texture) {
            this.texture = texture;
        }

        @Nullable
        private ResourceLocation texture() {
            return texture;
        }
    }
}
