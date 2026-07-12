package org.destroyermob.mobstoolforging.client.model;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ArmorPartData;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;

/** Resolves the silhouette removed from a physical pattern board. */
public final class PatternCutoutTextures {
    private PatternCutoutTextures() {
    }

    public static Optional<ResourceLocation> resolve(ForgeTemplateDefinition template) {
        Optional<ResourceLocation> armorTexture = armorTexture(template.partType());
        if (armorTexture.isPresent()) {
            return armorTexture;
        }
        return ToolTypeRegistry.toolType(template.toolType()).flatMap(definition -> {
            ToolVisualDefinition visual = ToolVisualManager.resolve(definition.visualId(), definition);
            ToolVisualLayer layer = visual.layerForSlot(partVisualSlot(template.partType()));
            return layer.templateId(true).or(() -> layer.templateId(false));
        });
    }

    private static Optional<ResourceLocation> armorTexture(String partType) {
        String texture = switch (partType) {
            case ArmorPartData.HELMET_CHAINMAIL, ArmorPartData.HELMET_PLATE -> "helmet";
            case ArmorPartData.CHESTPLATE_CHAINMAIL, ArmorPartData.CHESTPLATE_BODY -> "chestplate";
            case ArmorPartData.LEGGINGS_CHAINMAIL, ArmorPartData.LEGGINGS_PLATE -> "leggings";
            case ArmorPartData.BOOTS_CHAINMAIL, ArmorPartData.BOOTS_PLATE -> "boots";
            default -> null;
        };
        return texture == null
                ? Optional.empty()
                : Optional.of(ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "item/armor/" + texture));
    }

    private static String partVisualSlot(String partType) {
        return ToolPartData.SWORD_GUARD.equals(partType) ? "guard" : partType;
    }
}
