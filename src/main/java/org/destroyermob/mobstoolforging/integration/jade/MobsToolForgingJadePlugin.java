package org.destroyermob.mobstoolforging.integration.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.HeatingRecipe;
import org.destroyermob.mobstoolforging.world.HeatingRecipeRegistry;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.HeatingSource;
import org.destroyermob.mobstoolforging.world.PatternRackBlock;
import org.destroyermob.mobstoolforging.world.PatternRackBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;
import org.destroyermob.mobstoolforging.world.WorkstationKind;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin(MobsToolForging.MOD_ID)
public class MobsToolForgingJadePlugin implements IWailaPlugin {
    private static final WorkstationProvider WORKSTATION_PROVIDER = new WorkstationProvider();
    private static final HeatingForgeProvider HEATING_FORGE_PROVIDER = new HeatingForgeProvider();
    private static final CampfireHeatingProvider CAMPFIRE_HEATING_PROVIDER = new CampfireHeatingProvider();
    private static final PatternRackProvider PATTERN_RACK_PROVIDER = new PatternRackProvider();

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(WORKSTATION_PROVIDER, ToolWorkstationBlock.class);
        registration.registerBlockComponent(HEATING_FORGE_PROVIDER, HeatingForgeBlock.class);
        registration.registerBlockComponent(CAMPFIRE_HEATING_PROVIDER, CampfireBlock.class);
        registration.registerBlockComponent(PATTERN_RACK_PROVIDER, PatternRackBlock.class);
    }

    private static final class WorkstationProvider implements IBlockComponentProvider {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "workstation");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof ToolForgeBlockEntity forge)) {
                return;
            }
            if (forge.hasAbrasive()) {
                tooltip.add(line("jade.mobstoolforging.abrasive", forge.abrasiveStack().getHoverName()));
            }
            if (forge.linkedRackCount() > 0) {
                tooltip.add(Component.translatable("jade.mobstoolforging.linked_racks", forge.linkedRackCount(), ToolForgeBlockEntity.maxLinkedPatternRacks()).withStyle(ChatFormatting.GRAY));
            }
            if (forge.selectedPatternMissing()) {
                tooltip.add(line("jade.mobstoolforging.pattern", Component.translatable("message.mobstoolforging.pattern_missing")));
                return;
            }
            ForgeTemplateDefinition template = forge.template();
            if (template != null) {
                tooltip.add(line("jade.mobstoolforging.pattern", template.displayName()));
                if (forge.hasPatternSource()) {
                    tooltip.add(line("jade.mobstoolforging.source", Component.translatable("jade.mobstoolforging.source_pattern_rack")));
                }
            }
            if (forge.looseWorkRecipe() != null) {
                tooltip.add(line("jade.mobstoolforging.work", forge.looseWorkRecipe().outputCopy().getHoverName()));
                tooltip.add(progressLine(forge.hitCount(), forge.looseWorkRecipe().requiredHits()));
                return;
            }
            if (forge.isComplete()) {
                tooltip.add(line("jade.mobstoolforging.output", forge.outputStack().getHoverName()));
                return;
            }
            if (template != null) {
                tooltip.add(Component.translatable("jade.mobstoolforging.materials", forge.materialCount(), template.requiredMaterials()).withStyle(ChatFormatting.GRAY));
                if (forge.hasMaterialHeat()) {
                    tooltip.add(Component.translatable(
                            "jade.mobstoolforging.material_heat",
                            Math.round(forge.materialHeatTemperature() * 100.0F),
                            Component.translatable("tooltip.mobstoolforging.workpiece_status." + forge.materialHeatStatusKey())
                    ).withStyle(forge.materialIsForgeReady() ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
                }
                tooltip.add(progressLine(forge.hitCount(), template.requiredHits()));
            }
        }
    }

    private static final class PatternRackProvider implements IBlockComponentProvider {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "pattern_rack");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getBlockEntity() instanceof PatternRackBlockEntity rack) {
                tooltip.add(Component.translatable("jade.mobstoolforging.patterns", rack.occupiedSlots(), PatternRackBlockEntity.SLOT_COUNT).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static final class HeatingForgeProvider implements IBlockComponentProvider {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "heating_forge");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof HeatingForgeBlockEntity forge)) {
                return;
            }
            tooltip.add(Component.translatable(forge.isLit() ? "jade.mobstoolforging.lit" : "jade.mobstoolforging.unlit").withStyle(forge.isLit() ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
            if (forge.hasFuel()) {
                tooltip.add(line("jade.mobstoolforging.fuel", forge.fuelStack().getHoverName()));
            }
            for (int slot = 0; slot < forge.workpieceSlots(); slot++) {
                ItemStack workpiece = forge.workpieceStack(slot);
                if (workpiece.isEmpty()) {
                    continue;
                }
                float temperatureValue = forge.workpieceProgressTemperature(slot);
                int temperature = Math.round(temperatureValue * 100.0F);
                int target = Math.round(forge.workpieceTargetTemperature(slot) * 100.0F);
                String statusKey = org.destroyermob.mobstoolforging.world.WorkpieceHeat.statusKey(temperatureValue, org.destroyermob.mobstoolforging.world.WorkpieceHeat.isWorkable(workpiece), org.destroyermob.mobstoolforging.MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue());
                tooltip.add(Component.translatable("jade.mobstoolforging.workpiece", slot + 1, workpiece.getHoverName(), temperature, target, Component.translatable("tooltip.mobstoolforging.workpiece_status." + statusKey)).withStyle(ChatFormatting.GRAY));
                tooltip.add(progressLine(forge.heatProgress(slot), forge.requiredHeatTicks(slot)));
            }
        }
    }

    private static final class CampfireHeatingProvider implements IBlockComponentProvider {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "campfire_heating");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof CampfireBlockEntity campfire)) {
                return;
            }
            NonNullList<ItemStack> items = campfire.getItems();
            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack workpiece = items.get(slot);
                if (workpiece.isEmpty()) {
                    continue;
                }
                HeatingRecipe recipe = HeatingRecipeRegistry.find(HeatingSource.CAMPFIRE, workpiece).orElse(null);
                if (recipe == null) {
                    continue;
                }
                int temperature = Math.round(org.destroyermob.mobstoolforging.world.WorkpieceHeat.temperature(workpiece, accessor.getLevel()) * 100.0F);
                int target = Math.round(recipe.targetTemperature() * 100.0F);
                tooltip.add(Component.translatable("jade.mobstoolforging.campfire_workpiece", slot + 1, workpiece.getHoverName(), temperature, target, recipe.ticks()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static Component line(String key, Component value) {
        return Component.translatable(key, value).withStyle(ChatFormatting.GRAY);
    }

    private static Component progressLine(int current, int required) {
        return Component.translatable("jade.mobstoolforging.progress", current, required, Math.round(required <= 0 ? 0.0F : current * 100.0F / required)).withStyle(ChatFormatting.GRAY);
    }
}
