package org.destroyermob.mobstoolforging.client;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControllerInputs;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.RadialIcons;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public final class BeyondControlsIntegration implements ControlifyEntrypoint {
    private static final Component CATEGORY = Component.translatable("key.categories.mobstoolforging");
    private static final int KNAPPING_PRIORITY = 400;

    private static InputBindingSupplier previous;
    private static InputBindingSupplier next;

    @Override
    public void onControlifyPreInit(PreInitContext context) {
        previous = register(context, "knapping_previous", "left_shoulder",
                MobsToolForgingClient.previousKnappingKey());
        next = register(context, "knapping_next", "right_shoulder",
                MobsToolForgingClient.nextKnappingKey());
    }

    private static InputBindingSupplier register(PreInitContext context, String path, String button,
                                                 KeyMapping keyMapping) {
        return context.bindings().registerBinding(builder -> builder
                .id("fabric-key-binding-api-v1", "key.mobstoolforging." + path)
                .name(Component.translatable("key.mobstoolforging." + path))
                .category(CATEGORY)
                .defaultInput(ControllerInputs.button(button))
                .allowedContexts(BindContext.IN_GAME)
                .activeWhen(activation -> MobsToolForgingClient.hasKnappingTarget())
                .priority(KNAPPING_PRIORITY)
                .radialCandidate(RadialIcons.getItem(Items.FLINT))
                .addKeyCorrelation(keyMapping));
    }

    @Override
    public void onControlifyInit(InitContext context) {
        ControlifyEvents.ACTIVE_CONTROLLER_TICKED.register(event -> {
            var controller = event.controller();
            if (previous.on(controller).justPressed()) {
                MobsToolForgingClient.cycleKnappingTargetFromController(-1);
            }
            if (next.on(controller).justPressed()) {
                MobsToolForgingClient.cycleKnappingTargetFromController(1);
            }
        });
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {
    }
}
