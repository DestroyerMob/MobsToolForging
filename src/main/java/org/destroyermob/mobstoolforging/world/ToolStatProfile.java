package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ToolStatProfile(
        int maxDamage,
        float attackDamageBonus,
        float attackSpeedBonus,
        float miningSpeedMultiplier,
        float durabilityMultiplier,
        List<ResourceLocation> enchantAffinity,
        boolean fireResistant,
        List<ResourceLocation> traits,
        List<String> debugLines
) {
    public static final Codec<ToolStatProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("max_damage").forGetter(ToolStatProfile::maxDamage),
            Codec.FLOAT.fieldOf("attack_damage_bonus").forGetter(ToolStatProfile::attackDamageBonus),
            Codec.FLOAT.fieldOf("attack_speed_bonus").forGetter(ToolStatProfile::attackSpeedBonus),
            Codec.FLOAT.fieldOf("mining_speed_multiplier").forGetter(ToolStatProfile::miningSpeedMultiplier),
            Codec.FLOAT.fieldOf("durability_multiplier").forGetter(ToolStatProfile::durabilityMultiplier),
            ResourceLocation.CODEC.listOf().fieldOf("enchant_affinity").forGetter(ToolStatProfile::enchantAffinity),
            Codec.BOOL.fieldOf("fire_resistant").forGetter(ToolStatProfile::fireResistant),
            ResourceLocation.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(ToolStatProfile::traits),
            Codec.STRING.listOf().optionalFieldOf("debug_lines", List.of()).forGetter(ToolStatProfile::debugLines)
    ).apply(instance, ToolStatProfile::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolStatProfile> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public boolean hasAffinity(ResourceLocation affinity) {
        return enchantAffinity.contains(affinity);
    }
}
