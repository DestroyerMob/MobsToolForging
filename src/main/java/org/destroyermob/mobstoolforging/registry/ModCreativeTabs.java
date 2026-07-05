package org.destroyermob.mobstoolforging.registry;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.item.ModularToolItem;
import org.destroyermob.mobstoolforging.world.FlintToolStacks;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MobsToolForging.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOBS_TOOL_FORGING =
            CREATIVE_TABS.register("mobs_tool_forging", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mobstoolforging"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> ModItems.TOOLMAKERS_BENCH.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        acceptWorkstations(output);
                        acceptStationTools(output);
                        acceptPatterns(output);
                        acceptParts(output);
                        acceptMaterials(output);
                        acceptStarterTools(output);
                        acceptStarterArmor(output);
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }

    private static void acceptWorkstations(CreativeModeTab.Output output) {
        output.accept(ModItems.CRUDE_ANVIL.get());
        output.accept(ModItems.TOOL_FORGE.get());
        output.accept(ModItems.LAPIDARY_TABLE.get());
        output.accept(ModItems.PATTERN_CREATION_STATION.get());
        ModItems.PATTERN_RACK_ITEMS.forEach(item -> output.accept(item.get()));
        ModItems.TOOLMAKER_STATION_ITEMS.forEach(item -> output.accept(item.get()));
        output.accept(ModItems.HEATING_FORGE.get());
        output.accept(ModItems.CRUCIBLE.get());
        output.accept(ModItems.FOUNDRY_FORGE.get());
    }

    private static void acceptStationTools(CreativeModeTab.Output output) {
        output.accept(ModItems.SMITHING_HAMMER.get());
        output.accept(ModItems.IRON_SMITHING_HAMMER.get());
        output.accept(ModItems.GEM_CUTTERS_KNIFE.get());
        output.accept(ModItems.SCREWDRIVER.get());
        output.accept(ModItems.FIRE_STICK.get());
    }

    private static void acceptPatterns(CreativeModeTab.Output output) {
        output.accept(ModItems.PICKAXE_HEAD_PATTERN.get());
        output.accept(ModItems.AXE_HEAD_PATTERN.get());
        output.accept(ModItems.SHOVEL_HEAD_PATTERN.get());
        output.accept(ModItems.HOE_HEAD_PATTERN.get());
        output.accept(ModItems.SWORD_BLADE_PATTERN.get());
        output.accept(ModItems.SWORD_GUARD_PATTERN.get());
        output.accept(ModItems.SMITHING_HAMMER_HEAD_PATTERN.get());
        output.accept(ModItems.SCREWDRIVER_HEAD_PATTERN.get());
        output.accept(ModItems.GEM_CUTTERS_BLADE_PATTERN.get());
        output.accept(ModItems.HELMET_SKULL_PATTERN.get());
        output.accept(ModItems.HELMET_COMB_PATTERN.get());
        output.accept(ModItems.HELMET_VISOR_PATTERN.get());
        output.accept(ModItems.CHESTPLATE_CHAINMAIL_PATTERN.get());
        output.accept(ModItems.CHESTPLATE_BODY_PATTERN.get());
        output.accept(ModItems.LEGGINGS_LEGS_PATTERN.get());
        output.accept(ModItems.LEGGINGS_KNEES_PATTERN.get());
        output.accept(ModItems.LEGGINGS_TASSETS_PATTERN.get());
        output.accept(ModItems.BOOTS_FEET_PATTERN.get());
        ToolTypeRegistry.templates().stream()
                .filter(template -> !template.id().getNamespace().equals(MobsToolForging.MOD_ID))
                .map(ModCreativeTabs::templatePattern)
                .forEach(output::accept);
        output.accept(ModItems.TEMPLATE_PATTERN.get());
    }

    private static void acceptParts(CreativeModeTab.Output output) {
        output.accept(ModItems.SWORD_BLADE.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.SWORD_GUARD.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.SHOVEL_HEAD.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.PICKAXE_HEAD.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.AXE_HEAD.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.HOE_HEAD.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.HELMET_SKULL.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.CHESTPLATE_CHAINMAIL.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.CHESTPLATE_BODY.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.LEGGINGS_LEGS.get().createPart(MaterialCatalog.IRON));
        output.accept(ModItems.BOOTS_FEET.get().createPart(MaterialCatalog.IRON));
    }

    private static void acceptMaterials(CreativeModeTab.Output output) {
        output.accept(ModItems.PATTERN_BOARD.get());
        output.accept(ModItems.FLINT_SHARD.get());
        output.accept(ModItems.PLANT_FIBER.get());
        output.accept(ModItems.SMITHING_HAMMER_HEAD.get());
        output.accept(ModItems.SCREWDRIVER_HEAD.get());
        output.accept(ModItems.GEM_CUTTERS_BLADE.get());
        output.accept(ModItems.DIAMOND_POWDER.get());
    }

    private static void acceptStarterTools(CreativeModeTab.Output output) {
        if (MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get()) {
            flintToolKinds().forEach(toolKind -> output.accept(FlintToolStacks.create(toolKind)));
        }
        output.accept(modularTool(ToolKind.SWORD, Optional.of(MaterialCatalog.IRON)));
        output.accept(modularTool(ToolKind.SHOVEL, Optional.empty()));
        output.accept(modularTool(ToolKind.PICKAXE, Optional.empty()));
        output.accept(modularTool(ToolKind.AXE, Optional.empty()));
        output.accept(modularTool(ToolKind.HOE, Optional.empty()));
        output.accept(modularTool(ToolKind.MATTOCK, Optional.of(MaterialCatalog.IRON)));
    }

    private static void acceptStarterArmor(CreativeModeTab.Output output) {
        output.accept(ModItems.MODULAR_HELMET.get().create(MaterialCatalog.IRON, Optional.empty(), Optional.empty()));
        output.accept(ModItems.MODULAR_CHESTPLATE.get().createChainmail());
        output.accept(ModItems.MODULAR_LEGGINGS.get().create(MaterialCatalog.IRON));
        output.accept(ModItems.MODULAR_BOOTS.get().create(MaterialCatalog.IRON));
    }

    private static ItemStack modularTool(ToolKind toolKind, Optional<ResourceLocation> secondaryPartMaterial) {
        if (!(toolKind.toolItem().get() instanceof ModularToolItem modularTool)) {
            return ItemStack.EMPTY;
        }
        return modularTool.create(new ToolConstructionData(
                ToolConstructionData.toolType(toolKind),
                MaterialCatalog.IRON,
                MaterialCatalog.OAK,
                secondaryPartMaterial,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                ToolConstructionData.DEFAULT_QUALITY
        ));
    }

    private static ItemStack templatePattern(ForgeTemplateDefinition template) {
        ItemStack stack = new ItemStack(ModItems.TEMPLATE_PATTERN.get());
        stack.set(ModDataComponents.FORGE_TEMPLATE.get(), template.id());
        return stack;
    }

    private static java.util.List<ToolKind> flintToolKinds() {
        return java.util.List.of(ToolKind.SWORD, ToolKind.PICKAXE, ToolKind.AXE, ToolKind.SHOVEL, ToolKind.HOE);
    }
}
