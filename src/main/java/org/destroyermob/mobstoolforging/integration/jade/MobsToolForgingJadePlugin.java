package org.destroyermob.mobstoolforging.integration.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.client.HeatVisuals;
import org.destroyermob.mobstoolforging.mixin.CampfireBlockEntityAccessor;
import org.destroyermob.mobstoolforging.world.CampfireWorkpieceHeating;
import org.destroyermob.mobstoolforging.world.DryingRackBlock;
import org.destroyermob.mobstoolforging.world.DryingRackBlockEntity;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlock;
import org.destroyermob.mobstoolforging.world.FoundryForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.FoundryAccess;
import org.destroyermob.mobstoolforging.world.FoundryDrainBlock;
import org.destroyermob.mobstoolforging.world.FoundryFuelTankBlock;
import org.destroyermob.mobstoolforging.world.FoundryFuelTankBlockEntity;
import org.destroyermob.mobstoolforging.world.HeatingRecipe;
import org.destroyermob.mobstoolforging.world.HeatingRecipeRegistry;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.LavaHeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.HeatingSource;
import org.destroyermob.mobstoolforging.world.PatternRackBlock;
import org.destroyermob.mobstoolforging.world.PatternRackBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.ToolWorkstationBlock;
import org.destroyermob.mobstoolforging.world.WorkstationKind;
import org.destroyermob.mobstoolforging.world.WorkpieceHeat;
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
    private static final DryingRackProvider DRYING_RACK_PROVIDER = new DryingRackProvider();
    private static final FoundryFuelTankProvider FOUNDRY_FUEL_TANK_PROVIDER = new FoundryFuelTankProvider();
    private static final FoundryContentsProvider FOUNDRY_CONTENTS_PROVIDER = new FoundryContentsProvider();
    private static final FoundryDrainProvider FOUNDRY_DRAIN_PROVIDER = new FoundryDrainProvider();

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(WORKSTATION_PROVIDER, ToolWorkstationBlock.class);
        registration.registerBlockComponent(HEATING_FORGE_PROVIDER, HeatingForgeBlock.class);
        registration.registerBlockComponent(CAMPFIRE_HEATING_PROVIDER, CampfireBlock.class);
        registration.registerBlockComponent(PATTERN_RACK_PROVIDER, PatternRackBlock.class);
        registration.registerBlockComponent(DRYING_RACK_PROVIDER, DryingRackBlock.class);
        registration.registerBlockComponent(FOUNDRY_FUEL_TANK_PROVIDER, FoundryFuelTankBlock.class);
        registration.registerBlockComponent(FOUNDRY_CONTENTS_PROVIDER, FoundryForgeBlock.class);
        registration.registerBlockComponent(FOUNDRY_DRAIN_PROVIDER, FoundryDrainBlock.class);
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
                    Component heat = heatValue(forge.materialHeatTemperature());
                    Component status = heatStatus(forge.materialHeatTemperature(), forge.materialHeatStatusKey());
                    tooltip.add(Component.translatable("jade.mobstoolforging.material_heat_compact", heat, status).withStyle(ChatFormatting.GRAY));
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
            if (forge instanceof LavaHeatingForgeBlockEntity fluidForge) {
                if (!fluidForge.fluidStack().isEmpty()) {
                    tooltip.add(Component.translatable(
                            "jade.mobstoolforging.heating_fluid",
                            fluidForge.fluidStack().getHoverName(),
                            fluidForge.fluidAmount(),
                            fluidForge.tankCapacity()
                    ).withStyle(ChatFormatting.GRAY));
                }
            } else if (forge.hasFuel()) {
                tooltip.add(line("jade.mobstoolforging.fuel", forge.fuelStack().getHoverName()));
            }
            for (int slot = 0; slot < forge.workpieceSlots(); slot++) {
                ItemStack workpiece = forge.workpieceStack(slot);
                if (workpiece.isEmpty()) {
                    continue;
                }
                float temperatureValue = forge.workpieceProgressTemperature(slot);
                float target = forge.workpieceTargetTemperature(slot);
                float readyTemperature = Math.min(target, MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue());
                String statusKey = WorkpieceHeat.statusKey(temperatureValue, WorkpieceHeat.isWorkable(workpiece), readyTemperature);
                addWorkpieceHeatLine(tooltip, slot, workpiece, temperatureValue, statusKey, null);
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
                int timerTicks = campfireTicksRemaining(campfire, slot, recipe.ticks());
                int heatTicks = CampfireWorkpieceHeating.remainingHeatTicks(workpiece, accessor.getLevel(), recipe);
                int ticksRemaining = heatTicks > 0 ? Math.min(timerTicks, heatTicks) : timerTicks;
                float temperature = WorkpieceHeat.temperature(workpiece, accessor.getLevel());
                float target = recipe.targetTemperature();
                String statusKey = WorkpieceHeat.statusKey(temperature, WorkpieceHeat.isWorkable(workpiece), target);
                addWorkpieceHeatLine(tooltip, slot, workpiece, temperature, statusKey, secondsLeft(ticksRemaining));
            }
        }
    }

    private static final class DryingRackProvider implements IBlockComponentProvider {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "drying_rack");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof DryingRackBlockEntity rack) || !rack.hasItem()) {
                return;
            }
            ItemStack displayStack = rack.displayStack();
            if (rack.isDrying()) {
                tooltip.add(Component.translatable("jade.mobstoolforging.drying", displayStack.getHoverName(), secondsLeft(rack.dryingTicksRemaining())).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(line("jade.mobstoolforging.drying_ready", displayStack.getHoverName()));
            }
        }
    }

    private static final class FoundryFuelTankProvider implements IBlockComponentProvider {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "foundry_fuel_tank");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getBlockEntity() instanceof FoundryFuelTankBlockEntity tank) {
                int percent = Math.round(tank.fluidAmountMb() * 100.0F / tank.capacityMb());
                Component fluidName = tank.fluidStack().isEmpty()
                        ? Component.translatable("message.mobstoolforging.foundry_tank_empty")
                        : tank.fluidStack().getHoverName();
                tooltip.add(Component.translatable(
                        "jade.mobstoolforging.foundry_fuel",
                        fluidName,
                        tank.fluidAmountMb(),
                        tank.capacityMb(),
                        percent
                ).withStyle(ChatFormatting.GRAY));
                if (!tank.fluidStack().isEmpty()) {
                    tooltip.add(Component.translatable(
                            "jade.mobstoolforging.foundry_fuel_temperature",
                            formatFoundryTemperature(tank.fuelTemperatureC())
                    ).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    private static final class FoundryContentsProvider implements IBlockComponentProvider {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "foundry_contents");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof FoundryForgeBlockEntity foundry)) {
                return;
            }
            if (foundry.isFormed()) {
                tooltip.add(volumeLine("jade.mobstoolforging.foundry_molten", foundry.moltenAmountMb(), foundry.fluidCapacityMb()));
                java.util.List<FoundryForgeBlockEntity.MoltenLayer> layers = foundry.moltenLayers();
                for (int index = 0; index < layers.size(); index++) {
                    FoundryForgeBlockEntity.MoltenLayer layer = layers.get(index);
                    String key = index == 0
                            ? "jade.mobstoolforging.foundry_next_layer"
                            : "jade.mobstoolforging.foundry_layer";
                    tooltip.add(Component.translatable(key, MaterialCatalog.displayName(layer.material()), layer.amountMb()).withStyle(ChatFormatting.GRAY));
                }
                int solids = foundry.solidItemCount();
                if (solids > 0) {
                    tooltip.add(Component.translatable("jade.mobstoolforging.foundry_solids", solids).withStyle(ChatFormatting.GRAY));
                    java.util.List<ItemStack> inputs = foundry.solidRenderStacks();
                    if (!inputs.isEmpty()) {
                        tooltip.add(Component.translatable(
                                "jade.mobstoolforging.foundry_melting",
                                inputs.getFirst().getHoverName(),
                                Math.round(foundry.meltProgressFraction() * 100.0F)
                        ).withStyle(ChatFormatting.GRAY));
                        tooltip.add(Component.translatable(
                                foundry.hasSufficientTemperature()
                                        ? "jade.mobstoolforging.foundry_temperature_ready"
                                        : "jade.mobstoolforging.foundry_temperature_low",
                                formatFoundryTemperature(foundry.activeFuelTemperatureC()),
                                formatFoundryTemperature(foundry.currentMeltingPointC())
                        ).withStyle(foundry.hasSufficientTemperature() ? ChatFormatting.GRAY : ChatFormatting.RED));
                    }
                }
                tooltip.add(Component.translatable(
                        "jade.mobstoolforging.foundry_connected_fuel",
                        foundry.connectedFuelMb(),
                        foundry.connectedTankCount()
                ).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("jade.mobstoolforging.foundry_molten_unformed", foundry.moltenAmountMb()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static final class FoundryDrainProvider implements IBlockComponentProvider {
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MobsToolForging.MOD_ID, "foundry_drain");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            FoundryAccess.findController(accessor.getLevel(), accessor.getPosition()).ifPresent(foundry ->
                    foundry.bottomMoltenLayer().ifPresentOrElse(
                            layer -> tooltip.add(Component.translatable(
                                    "jade.mobstoolforging.foundry_next_layer",
                                    MaterialCatalog.displayName(layer.material()),
                                    layer.amountMb()
                            ).withStyle(ChatFormatting.GRAY)),
                            () -> tooltip.add(Component.translatable("jade.mobstoolforging.foundry_next_empty").withStyle(ChatFormatting.GRAY))
                    ));
        }
    }

    private static Component line(String key, Component value) {
        return Component.translatable(key, value).withStyle(ChatFormatting.GRAY);
    }

    private static Component progressLine(int current, int required) {
        return Component.translatable("jade.mobstoolforging.progress", current, required, Math.round(required <= 0 ? 0.0F : current * 100.0F / required)).withStyle(ChatFormatting.GRAY);
    }

    private static Component volumeLine(String key, int amountMb, int capacityMb) {
        int percent = Math.round(capacityMb <= 0 ? 0.0F : amountMb * 100.0F / capacityMb);
        return Component.translatable(key, amountMb, capacityMb, percent).withStyle(ChatFormatting.GRAY);
    }

    private static String formatFoundryTemperature(float temperature) {
        return String.format(java.util.Locale.ROOT, "%.0f°C", temperature);
    }

    private static void addWorkpieceHeatLine(ITooltip tooltip, int slot, ItemStack workpiece, float temperature, String statusKey, Component timeRemaining) {
        Component heat = heatValue(temperature);
        Component status = heatStatus(temperature, statusKey);
        Component line = timeRemaining == null
                ? Component.translatable("jade.mobstoolforging.workpiece_heat_compact", slot + 1, workpiece.getHoverName(), heat, status)
                : Component.translatable("jade.mobstoolforging.workpiece_heat_compact_timed", slot + 1, workpiece.getHoverName(), heat, status, timeRemaining);
        tooltip.add(line.copy().withStyle(ChatFormatting.GRAY));
    }

    private static Component heatValue(float temperature) {
        float clampedTemperature = HeatVisuals.clamp(temperature);
        int color = HeatVisuals.interfaceColor(clampedTemperature);
        int temperaturePercent = WorkpieceHeat.displayPercent(clampedTemperature);
        return Component.translatable("jade.mobstoolforging.heat_value", temperaturePercent)
                .withStyle(style -> style.withColor(color & 0x00FFFFFF));
    }

    private static Component heatStatus(float temperature, String statusKey) {
        int color = HeatVisuals.interfaceColor(temperature);
        return Component.translatable("tooltip.mobstoolforging.workpiece_status." + statusKey)
                .withStyle(style -> style.withColor(color & 0x00FFFFFF));
    }

    private static int campfireTicksRemaining(CampfireBlockEntity campfire, int slot, int defaultDuration) {
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor) campfire;
        int[] cookingProgress = accessor.mobstoolforging$cookingProgress();
        int[] cookingTime = accessor.mobstoolforging$cookingTime();
        int duration = slot < cookingTime.length && cookingTime[slot] > 0 ? cookingTime[slot] : defaultDuration;
        int progress = slot < cookingProgress.length ? cookingProgress[slot] : 0;
        return Math.max(0, duration - progress);
    }

    private static Component secondsLeft(int ticks) {
        if (ticks <= 0) {
            return Component.literal("0s");
        }
        return Component.literal(Math.max(1, (int) Math.ceil(ticks / 20.0D)) + "s");
    }
}
