package org.destroyermob.mobstoolforging.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

final class ModularArmorTrimRenderer {
    private ModularArmorTrimRenderer() {
    }

    @Nullable
    static VertexConsumer consumer(ItemStack stack, MultiBufferSource bufferSource, boolean innerTexture) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim == null || !(stack.getItem() instanceof ArmorItem armorItem)) {
            return null;
        }

        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
        TextureAtlasSprite sprite = atlas.getSprite(
                innerTexture ? trim.innerTexture(armorItem.getMaterial()) : trim.outerTexture(armorItem.getMaterial())
        );
        return sprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
    }
}
