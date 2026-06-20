package org.destroyermob.mobstoolforging.integration.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlock;
import org.destroyermob.mobstoolforging.world.HeatingForgeBlockEntity;
import org.destroyermob.mobstoolforging.world.LapidaryTableBlock;
import org.destroyermob.mobstoolforging.world.ToolForgeBlock;
import org.destroyermob.mobstoolforging.world.ToolForgeBlockEntity;
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

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(WorkstationProvider.UID, true);
        registration.addConfig(HeatingForgeProvider.UID, true);
        registration.registerBlockComponent(WORKSTATION_PROVIDER, ToolForgeBlock.class);
        registration.registerBlockComponent(WORKSTATION_PROVIDER, LapidaryTableBlock.class);
        registration.registerBlockComponent(HEATING_FORGE_PROVIDER, HeatingForgeBlock.class);
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
            ForgeTemplateDefinition template = forge.template();
            if (template != null) {
                tooltip.add(line("jade.mobstoolforging.pattern", template.displayName()));
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
                tooltip.add(progressLine(forge.hitCount(), template.requiredHits()));
            } else if (accessor.getBlock() instanceof LapidaryTableBlock && !forge.hasAbrasive()) {
                tooltip.add(Component.translatable("message.mobstoolforging.lapidary_needs_abrasive").withStyle(ChatFormatting.DARK_GRAY));
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
                int temperature = Math.round(forge.heatProgressFraction(slot) * 100.0F);
                tooltip.add(Component.translatable("jade.mobstoolforging.workpiece", slot + 1, workpiece.getHoverName(), temperature).withStyle(ChatFormatting.GRAY));
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
