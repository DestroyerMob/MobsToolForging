package org.destroyermob.mobstoolforging.world;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Shared, translated foundry state and construction feedback for interactions and HUD integrations. */
public final class FoundryFeedback {
    private FoundryFeedback() {
    }

    public static Component structureDiagnostic(FoundryForgeBlockEntity foundry) {
        FoundryStructure.Diagnosis diagnosis = foundry.structureDiagnosis();
        if (diagnosis.formed()) {
            return Component.translatable("message.mobstoolforging.foundry_structure.complete");
        }
        if (diagnosis.failure() == FoundryStructure.Failure.CONTENTS_EXCEED_CAPACITY) {
            return Component.translatable(
                    "message.mobstoolforging.foundry_structure.contents_exceed_capacity",
                    foundry.moltenAmountMb() + foundry.solidReservedMb(),
                    diagnosis.structure().interiorVolume() * 1000
            );
        }
        String key = "message.mobstoolforging.foundry_structure." + diagnosis.failure().name().toLowerCase(Locale.ROOT);
        return diagnosis.problemPos()
                .<Component>map(pos -> Component.translatable(key, pos.getX(), pos.getY(), pos.getZ()))
                .orElseGet(() -> Component.translatable(key));
    }

    public static Component operatingStatus(FoundryForgeBlockEntity foundry) {
        return operatingStatus(foundry, foundry.connectedFuelStatus());
    }

    public static Component operatingStatus(
            FoundryForgeBlockEntity foundry,
            FoundryForgeBlockEntity.ConnectedFuelStatus fuel
    ) {
        if (!foundry.isFormed()) {
            return structureDiagnostic(foundry).copy().withStyle(ChatFormatting.RED);
        }
        if (foundry.solidItemCount() <= 0) {
            if (foundry.isStoked()) {
                return Component.translatable(foundry.moltenAmountMb() > 0
                                ? "message.mobstoolforging.foundry_operation.stoked_ready_to_pour"
                                : "message.mobstoolforging.foundry_operation.stoked_idle",
                        foundry.stokeTicksRemaining() / 20).withStyle(ChatFormatting.GOLD);
            }
            return Component.translatable(foundry.moltenAmountMb() > 0
                    ? "message.mobstoolforging.foundry_operation.ready_to_pour"
                    : "message.mobstoolforging.foundry_operation.add_metal").withStyle(ChatFormatting.GRAY);
        }
        if (fuel.tankCount() <= 0) {
            return Component.translatable("message.mobstoolforging.foundry_operation.add_tank").withStyle(ChatFormatting.RED);
        }
        if (!foundry.isLit() && fuel.amountMb() <= 0) {
            return Component.translatable("message.mobstoolforging.foundry_operation.add_fuel").withStyle(ChatFormatting.RED);
        }
        if (!foundry.hasSufficientTemperature()
                || (!foundry.isLit() && fuel.hottestTemperatureC() < foundry.currentMeltingPointC())) {
            float availableTemperature = foundry.isLit()
                    ? foundry.activeFuelTemperatureC()
                    : fuel.hottestTemperatureC();
            return Component.translatable(
                    "message.mobstoolforging.foundry_operation.too_cool",
                    temperature(availableTemperature),
                    temperature(foundry.currentMeltingPointC())
            ).withStyle(ChatFormatting.RED);
        }
        if (!foundry.isLit()) {
            return Component.translatable("message.mobstoolforging.foundry_operation.starting").withStyle(ChatFormatting.GOLD);
        }
        return foundry.isStoked()
                ? Component.translatable(
                        "message.mobstoolforging.foundry_operation.melting_stoked",
                        Math.round(foundry.meltProgressFraction() * 100.0F),
                        foundry.stokeTicksRemaining() / 20
                ).withStyle(ChatFormatting.GOLD)
                : Component.translatable(
                        "message.mobstoolforging.foundry_operation.melting",
                        Math.round(foundry.meltProgressFraction() * 100.0F)
                ).withStyle(ChatFormatting.GOLD);
    }

    public static String temperature(float celsius) {
        return String.format(Locale.ROOT, "%.0f°C", celsius);
    }

}
