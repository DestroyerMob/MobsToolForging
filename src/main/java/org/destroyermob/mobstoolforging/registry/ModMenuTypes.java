package org.destroyermob.mobstoolforging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.PatternCreationStationMenu;

public final class ModMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
            Registries.MENU,
            MobsToolForging.MOD_ID
    );

    public static final DeferredHolder<MenuType<?>, MenuType<PatternCreationStationMenu>> PATTERN_CREATION_STATION = MENU_TYPES.register(
            "pattern_creation_station",
            () -> IMenuTypeExtension.create(PatternCreationStationMenu::new)
    );

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
