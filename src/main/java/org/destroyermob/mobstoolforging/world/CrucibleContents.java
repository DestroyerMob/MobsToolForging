package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record CrucibleContents(
        ItemStack item,
        Optional<ResourceLocation> moltenMaterial,
        int moltenAmount,
        float heat
) {
    public static final int DEFAULT_MOLTEN_AMOUNT = 1;
    public static final CrucibleContents EMPTY = new CrucibleContents(ItemStack.EMPTY, Optional.empty(), 0, 0.0F);
    public static final Codec<CrucibleContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("item", ItemStack.EMPTY).forGetter(CrucibleContents::item),
            ResourceLocation.CODEC.optionalFieldOf("molten_material").forGetter(CrucibleContents::moltenMaterial),
            Codec.INT.optionalFieldOf("molten_amount", 0).forGetter(CrucibleContents::moltenAmount),
            Codec.FLOAT.optionalFieldOf("heat", 0.0F).forGetter(CrucibleContents::heat)
    ).apply(instance, CrucibleContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleContents> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public CrucibleContents {
        item = item.copyWithCount(Math.min(1, item.getCount()));
        moltenAmount = Math.max(0, moltenAmount);
        heat = Math.max(0.0F, Math.min(1.0F, heat));
        if (!item.isEmpty() && moltenMaterial.isPresent()) {
            item = ItemStack.EMPTY;
        }
        if (moltenAmount == 0) {
            moltenMaterial = Optional.empty();
        }
    }

    public static CrucibleContents ofItem(ItemStack stack) {
        return new CrucibleContents(stack.copyWithCount(1), Optional.empty(), 0, 0.0F);
    }

    public static CrucibleContents molten(ResourceLocation material, int amount) {
        return new CrucibleContents(ItemStack.EMPTY, Optional.of(material), amount, 1.0F);
    }

    public boolean isEmpty() {
        return item.isEmpty() && moltenMaterial.isEmpty();
    }

    public boolean hasItem() {
        return !item.isEmpty();
    }

    public boolean hasMoltenMaterial() {
        return moltenMaterial.isPresent() && moltenAmount > 0;
    }

    public boolean isWhiteHot() {
        return heat >= 0.98F;
    }

    public CrucibleContents withHeat(float value) {
        return new CrucibleContents(item, moltenMaterial, moltenAmount, value);
    }

    public CrucibleContents melt(ResourceLocation material) {
        return molten(material, DEFAULT_MOLTEN_AMOUNT);
    }

    public CrucibleContents consumeMoltenUnit() {
        if (moltenMaterial.isEmpty() || moltenAmount <= 1) {
            return EMPTY;
        }
        return new CrucibleContents(ItemStack.EMPTY, moltenMaterial, moltenAmount - 1, heat);
    }
}
