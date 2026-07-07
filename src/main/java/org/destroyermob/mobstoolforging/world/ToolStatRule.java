package org.destroyermob.mobstoolforging.world;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public record ToolStatRule(
        ResourceLocation id,
        Optional<ResourceLocation> toolType,
        String slot,
        ResourceLocation material,
        float durabilityMultiplier,
        float miningSpeedMultiplier,
        float attackDamageBonus,
        float attackSpeedBonus,
        boolean fireResistant,
        List<ResourceLocation> traits,
        List<ResourceLocation> affinities,
        @Nullable String debugLine
) implements ToolTypeRegistry.ToolStatModifier {
    public ToolStatRule {
        traits = List.copyOf(traits);
        affinities = List.copyOf(affinities);
    }

    @Override
    public void apply(ToolTypeDefinition definition, ToolConstructionData construction, ToolStatBuilder.MutableStats stats) {
        if (toolType.isPresent() && !toolType.get().equals(definition.id())) {
            return;
        }
        if (!matches(construction)) {
            return;
        }
        stats.multiplyDurability(durabilityMultiplier);
        stats.multiplyMiningSpeed(miningSpeedMultiplier);
        stats.addAttackDamage(attackDamageBonus);
        stats.addAttackSpeed(attackSpeedBonus);
        if (fireResistant) {
            stats.setFireResistant();
        }
        traits.forEach(stats::addTrait);
        affinities.forEach(stats::addAffinities);
        if (debugLine != null && !debugLine.isBlank()) {
            stats.addDebug(debugLine);
        }
    }

    private boolean matches(ToolConstructionData construction) {
        return switch (slot) {
            case "head", "headMaterial" -> material.equals(construction.headMaterial());
            case "head_base", "headBase", "core", "coreMaterial" -> construction.headBaseMaterial().filter(material::equals).isPresent();
            case "handle", "handleMaterial" -> material.equals(construction.handleMaterial());
            case "guard", "guardMaterial" -> construction.guardMaterial().filter(material::equals).isPresent();
            case "treatment" -> construction.treatment().filter(material::equals).isPresent();
            case "any", "anyPart" -> material.equals(construction.headMaterial())
                    || construction.headBaseMaterial().filter(material::equals).isPresent()
                    || material.equals(construction.handleMaterial())
                    || construction.guardMaterial().filter(material::equals).isPresent();
            default -> false;
        };
    }
}
