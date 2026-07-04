package org.destroyermob.mobstoolforging.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public final class WorkshopHeat {
    private static final int HEAT_RADIUS = 3;

    private WorkshopHeat() {
    }

    public static HeatLevel heatForJob(Level level, BlockPos stationPos, ItemStack stack, WorkstationKind workstationKind, ToolMaterialDefinition material) {
        HeatLevel stackHeat = stackHeatLevel(level, stack);
        HeatLevel workshopHeat = nearbyHeat(level, stationPos, stackHeat != HeatLevel.NONE);
        HeatLevel heat = max(stackHeat, workshopHeat);
        if (heat == HeatLevel.NONE && workstationKind == WorkstationKind.LAPIDARY_TABLE) {
            return HeatLevel.NONE;
        }
        return heat;
    }

    public static float stackTemperature(Level level, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0.0F;
        }
        HeatedWorkpieceData heatData = stack.get(ModDataComponents.HEATED_WORKPIECE.get());
        return heatData == null ? 0.0F : heatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
    }

    private static HeatLevel stackHeatLevel(Level level, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return HeatLevel.NONE;
        }
        HeatedWorkpieceData heatData = stack.get(ModDataComponents.HEATED_WORKPIECE.get());
        if (heatData == null || !heatData.workable()) {
            return HeatLevel.NONE;
        }
        float temperature = heatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
        if (temperature >= MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue()) {
            return HeatLevel.HOT;
        }
        if (temperature >= MobsToolForgingConfig.LOW_HEAT_MINIMUM_FORGE_TEMPERATURE.get().floatValue()) {
            return HeatLevel.LOW;
        }
        return HeatLevel.NONE;
    }

    public static HeatLevel nearbyHeat(Level level, BlockPos stationPos) {
        return nearbyHeat(level, stationPos, true);
    }

    private static HeatLevel nearbyHeat(Level level, BlockPos stationPos, boolean includeCampfires) {
        HeatLevel best = HeatLevel.NONE;
        for (BlockPos pos : BlockPos.betweenClosed(stationPos.offset(-HEAT_RADIUS, -1, -HEAT_RADIUS), stationPos.offset(HEAT_RADIUS, 1, HEAT_RADIUS))) {
            if (pos.equals(stationPos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (includeCampfires
                    && MobsToolForgingConfig.ENABLE_CAMPFIRE_LOW_HEAT.get()
                    && state.getBlock() instanceof CampfireBlock
                    && state.hasProperty(CampfireBlock.LIT)
                    && state.getValue(CampfireBlock.LIT)) {
                best = max(best, MobsToolForgingConfig.campfireHeatLevel());
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof HeatingForgeBlockEntity forge && forge.isWorkshopHot(level)) {
                best = max(best, HeatLevel.HOT);
            }
            if (best == HeatLevel.HIGH) {
                return best;
            }
        }
        return best;
    }

    public static boolean canStartMetalWork(HeatLevel heatLevel, float materialTemperature, ToolMaterialDefinition material, ForgeTemplateDefinition template) {
        HeatLevel requiredHeat = material.minimumForgeHeat();
        if (requiredHeat == HeatLevel.NONE) {
            return true;
        }
        if (heatLevel == HeatLevel.NONE) {
            return false;
        }
        if (heatLevel == HeatLevel.LOW && !MobsToolForgingConfig.ENABLE_CAMPFIRE_LOW_HEAT.get()) {
            return false;
        }
        if (requiredHeat == HeatLevel.LOW && heatLevel == HeatLevel.LOW) {
            return materialTemperature >= minimumForgeTemperature(material, template);
        }
        return heatLevel.atLeast(requiredHeat);
    }

    public static float minimumForgeTemperature(ToolMaterialDefinition material, ForgeTemplateDefinition template) {
        if (material.minimumForgeHeat() == HeatLevel.LOW) {
            return MobsToolForgingConfig.LOW_HEAT_MINIMUM_FORGE_TEMPERATURE.get().floatValue();
        }
        return template == null ? MobsToolForgingConfig.MINIMUM_FORGE_TEMPERATURE.get().floatValue() : template.minimumTemperature();
    }

    public static HeatLevel max(HeatLevel left, HeatLevel right) {
        return left.ordinal() >= right.ordinal() ? left : right;
    }
}
