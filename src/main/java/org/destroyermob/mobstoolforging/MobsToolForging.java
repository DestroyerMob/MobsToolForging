package org.destroyermob.mobstoolforging;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.destroyermob.mobstoolforging.client.MobsToolForgingClient;
import org.destroyermob.mobstoolforging.data.ModDataGenerators;
import org.destroyermob.mobstoolforging.network.ModNetworking;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModRecipeSerializers;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.slf4j.Logger;

@Mod(MobsToolForging.MOD_ID)
public class MobsToolForging {
    public static final String MOD_ID = "mobstoolforging";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobsToolForging(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModNetworking.register(modEventBus);
        ModDataGenerators.register(modEventBus);

        modEventBus.addListener(this::addCreativeTabContents);

        if (FMLEnvironment.dist.isClient()) {
            MobsToolForgingClient.register(modEventBus);
        }
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.TOOL_FORGE);
            event.accept(ModBlocks.LAPIDARY_TABLE);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            for (ToolKind toolKind : ToolKind.values()) {
                event.accept(toolKind.createPart(MaterialCatalog.DIAMOND));
            }
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.SMITHING_HAMMER);
            for (ToolKind toolKind : ToolKind.values()) {
                event.accept(toolKind.createTool(MaterialCatalog.DIAMOND, net.minecraft.world.item.Items.STICK.getDefaultInstance()));
            }
        }
    }
}
