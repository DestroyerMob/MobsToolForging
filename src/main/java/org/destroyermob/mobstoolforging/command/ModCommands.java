package org.destroyermob.mobstoolforging.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ArmorStatsCatalog;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ForgingQuality;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
import org.destroyermob.mobstoolforging.world.ToolPartData;
import org.destroyermob.mobstoolforging.world.ToolTypeDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.VanillaToolConverter;

public final class ModCommands {
    private static final ResourceLocation NONE = ResourceLocation.withDefaultNamespace("none");

    private ModCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(MobsToolForging.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("convert_tool")
                        .executes(ModCommands::convertVanillaTool))
                .then(Commands.literal("convert_vanilla_tool")
                        .executes(ModCommands::convertVanillaTool))
                .then(giveDebug(false))
                .then(giveDebug(true))
                .then(givePartCommand(false))
                .then(givePartCommand(true)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> giveDebug(boolean targetBranch) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(targetBranch ? "give_debug_to" : "give_debug");
        if (targetBranch) {
            return root.then(Commands.argument("targets", EntityArgument.players())
                    .then(armorRoot(true))
                    .then(toolRoot(true))
                    .then(partRoot(true)));
        }
        return root.then(armorRoot(false))
                .then(toolRoot(false))
                .then(partRoot(false));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> givePartCommand(boolean targetBranch) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(targetBranch ? "give_part_to" : "give_part");
        if (targetBranch) {
            return root.then(Commands.argument("targets", EntityArgument.players())
                    .then(partArguments(true)));
        }
        return root.then(partArguments(false));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> armorRoot(boolean targetBranch) {
        return Commands.literal("armor")
                .then(helmetCommand(targetBranch))
                .then(chestplateCommand(targetBranch))
                .then(leggingsCommand(targetBranch))
                .then(bootsCommand(targetBranch));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> helmetCommand(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> plate = materialArgument("plate", true);
        plate.executes(context -> giveHelmet(context, targetBranch, Optional.of(material(context, "plate"))));
        return Commands.literal("helmet")
                .executes(context -> giveHelmet(context, targetBranch, Optional.empty()))
                .then(plate);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> chestplateCommand(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> plate = materialArgument("plate", true);
        plate.executes(context -> giveChestplate(context, targetBranch, Optional.of(material(context, "plate"))));
        return Commands.literal("chestplate")
                .executes(context -> giveChestplate(context, targetBranch, Optional.empty()))
                .then(plate);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leggingsCommand(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> plate = materialArgument("plate", true);
        plate.executes(context -> giveLeggings(context, targetBranch, Optional.of(material(context, "plate"))));
        return Commands.literal("leggings")
                .executes(context -> giveLeggings(context, targetBranch, Optional.empty()))
                .then(plate);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bootsCommand(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> plate = materialArgument("plate", true);
        plate.executes(context -> giveBoots(context, targetBranch, Optional.of(material(context, "plate"))));
        return Commands.literal("boots")
                .executes(context -> giveBoots(context, targetBranch, Optional.empty()))
                .then(plate);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> toolRoot(boolean targetBranch) {
        LiteralArgumentBuilder<CommandSourceStack> tool = Commands.literal("tool");
        tool.then(toolCommand(ToolKind.SWORD, targetBranch, true));
        tool.then(toolCommand(ToolKind.PICKAXE, targetBranch, false));
        tool.then(toolCommand(ToolKind.AXE, targetBranch, false));
        tool.then(toolCommand(ToolKind.SHOVEL, targetBranch, false));
        tool.then(toolCommand(ToolKind.HOE, targetBranch, false));
        tool.then(toolCommand(ToolKind.MATTOCK, targetBranch, true));
        return tool;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> toolCommand(ToolKind toolKind, boolean targetBranch, boolean requiresGuard) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> head = materialArgument("head", false);
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> handle = materialArgument("handle", false);
        head.then(handle);
        if (requiresGuard) {
            handle.then(requiredToolTail(toolKind, targetBranch));
        } else {
            handle.executes(context -> giveTool(context, targetBranch, toolKind, Optional.empty()));
        }
        return Commands.literal(toolKind.id()).then(head);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> requiredToolTail(ToolKind toolKind, boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> guard = materialArgument("guard", false);
        guard.executes(context -> giveTool(context, targetBranch, toolKind, Optional.of(material(context, "guard"))));
        return guard;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> partRoot(boolean targetBranch) {
        return Commands.literal("part").then(partArguments(targetBranch));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> partArguments(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, String> partType = Commands.argument("part_type", StringArgumentType.word())
                .suggests((context, builder) -> suggestPartTypes(builder));
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> materialArg = Commands.argument("material", ResourceLocationArgument.id())
                .suggests(ModCommands::suggestPartMaterials);
        materialArg.executes(context -> givePart(
                context,
                targetBranch,
                StringArgumentType.getString(context, "part_type"),
                material(context, "material"),
                ToolPartData.DEFAULT_QUALITY
        ));
        materialArg.then(Commands.argument("quality", IntegerArgumentType.integer(ForgingQuality.MIN_SCORE, ForgingQuality.MAX_SCORE))
                .executes(context -> givePart(
                        context,
                        targetBranch,
                        StringArgumentType.getString(context, "part_type"),
                        material(context, "material"),
                        IntegerArgumentType.getInteger(context, "quality")
                )));
        return partType.then(materialArg);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> materialArgument(String name, boolean armor) {
        return Commands.argument(name, ResourceLocationArgument.id())
                .suggests((context, builder) -> suggestMaterials(builder, armor, false));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> optionalMaterialArgument(String name, boolean armor) {
        return Commands.argument(name, ResourceLocationArgument.id())
                .suggests((context, builder) -> suggestMaterials(builder, armor, true));
    }

    private static CompletableFuture<Suggestions> suggestMaterials(SuggestionsBuilder builder, boolean armor, boolean optional) {
        List<String> suggestions = new ArrayList<>();
        if (optional) {
            suggestions.add("none");
        }
        (armor ? armorMaterials() : toolMaterials()).forEach(material -> suggestions.add(material.toString()));
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }

    private static CompletableFuture<Suggestions> suggestPartTypes(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(partTypes(), builder);
    }

    private static CompletableFuture<Suggestions> suggestPartMaterials(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String partType = "";
        try {
            partType = StringArgumentType.getString(context, "part_type");
        } catch (IllegalArgumentException ignored) {
        }
        List<ResourceLocation> materials;
        if (isHandlePart(partType)) {
            materials = handleInputMaterials();
        } else {
            ForgeTemplateDefinition template = partTemplate(partType).orElse(null);
            materials = template != null && !template.materialWhitelist().isEmpty()
                    ? template.materialWhitelist().stream().toList()
                    : forgedPartMaterials();
        }
        return SharedSuggestionProvider.suggest(materials.stream().map(ResourceLocation::toString).toList(), builder);
    }

    private static List<ResourceLocation> armorMaterials() {
        return MaterialCatalog.starterMaterialIds().stream()
                .filter(ArmorStatsCatalog::isSupportedArmorMaterial)
                .toList();
    }

    private static List<ResourceLocation> toolMaterials() {
        List<ResourceLocation> values = new ArrayList<>();
        addAll(values, MaterialCatalog.starterMaterialIds());
        addAll(values, MaterialCatalog.handleMaterialIds());
        return List.copyOf(values);
    }

    private static void addAll(List<ResourceLocation> values, List<ResourceLocation> additions) {
        additions.forEach(value -> {
            if (!values.contains(value)) {
                values.add(value);
            }
        });
    }

    private static List<String> partTypes() {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        values.add("handle");
        for (ToolTypeDefinition definition : ToolTypeRegistry.toolTypes()) {
            values.add(definition.primaryPartType());
            values.addAll(definition.requiredAssemblyParts());
            values.addAll(definition.partTypes());
        }
        ToolTypeRegistry.templates().stream()
                .filter(ModCommands::isToolPartTemplate)
                .map(ForgeTemplateDefinition::partType)
                .forEach(values::add);
        return List.copyOf(values);
    }

    private static List<ResourceLocation> forgedPartMaterials() {
        List<ResourceLocation> values = new ArrayList<>();
        addAll(values, MaterialCatalog.starterMaterialIds());
        if (!values.contains(MaterialCatalog.FLINT)) {
            values.add(MaterialCatalog.FLINT);
        }
        return List.copyOf(values);
    }

    private static List<ResourceLocation> handleInputMaterials() {
        return List.of(MaterialCatalog.OAK, MaterialCatalog.BLAZE, MaterialCatalog.BREEZE);
    }

    private static int giveHelmet(CommandContext<CommandSourceStack> context, boolean targetBranch, Optional<ResourceLocation> plate) throws CommandSyntaxException {
        if (plate.isPresent() && !ArmorStatsCatalog.isSupportedArmorMaterial(plate.get())) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(plate.get())));
            return 0;
        }
        ItemStack stack = plate
                .map(material -> ModItems.MODULAR_HELMET.get().create(material))
                .orElseGet(() -> ModItems.MODULAR_HELMET.get().createChainmail());
        return giveStack(context, targetBranch, stack);
    }

    private static int giveChestplate(CommandContext<CommandSourceStack> context, boolean targetBranch, Optional<ResourceLocation> plate) throws CommandSyntaxException {
        if (plate.isPresent() && !ArmorStatsCatalog.isSupportedArmorMaterial(plate.get())) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(plate.get())));
            return 0;
        }
        ItemStack stack = plate
                .map(material -> ModItems.MODULAR_CHESTPLATE.get().create(material))
                .orElseGet(() -> ModItems.MODULAR_CHESTPLATE.get().createChainmail());
        return giveStack(context, targetBranch, stack);
    }

    private static int giveLeggings(CommandContext<CommandSourceStack> context, boolean targetBranch, Optional<ResourceLocation> plate) throws CommandSyntaxException {
        if (plate.isPresent() && !ArmorStatsCatalog.isSupportedArmorMaterial(plate.get())) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(plate.get())));
            return 0;
        }
        ItemStack stack = plate
                .map(material -> ModItems.MODULAR_LEGGINGS.get().create(material))
                .orElseGet(() -> ModItems.MODULAR_LEGGINGS.get().createChainmail());
        return giveStack(context, targetBranch, stack);
    }

    private static int giveBoots(CommandContext<CommandSourceStack> context, boolean targetBranch, Optional<ResourceLocation> plate) throws CommandSyntaxException {
        if (plate.isPresent() && !ArmorStatsCatalog.isSupportedArmorMaterial(plate.get())) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(plate.get())));
            return 0;
        }
        ItemStack stack = plate
                .map(material -> ModItems.MODULAR_BOOTS.get().create(material))
                .orElseGet(() -> ModItems.MODULAR_BOOTS.get().createChainmail());
        return giveStack(context, targetBranch, stack);
    }

    private static int giveTool(CommandContext<CommandSourceStack> context, boolean targetBranch, ToolKind toolKind, Optional<ResourceLocation> guard) throws CommandSyntaxException {
        ResourceLocation head = material(context, "head");
        ResourceLocation handle = material(context, "handle");
        if (toolKind == ToolKind.SWORD && guard.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.sword_needs_guard"));
            return 0;
        }
        ToolTypeDefinition definition = ToolTypeRegistry.toolType(toolKind).orElse(null);
        if (definition == null) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.unknown_tool", toolKind.id()));
            return 0;
        }
        ToolConstructionData construction = new ToolConstructionData(
                ToolConstructionData.toolType(toolKind),
                head,
                handle,
                guard,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                ToolConstructionData.DEFAULT_QUALITY
        );
        ItemStack stack = definition.createTool(construction);
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.tool_failed", toolKind.id()));
            return 0;
        }
        return giveStack(context, targetBranch, stack);
    }

    private static int givePart(CommandContext<CommandSourceStack> context, boolean targetBranch, String partType, ResourceLocation material, int quality) throws CommandSyntaxException {
        if (isHandlePart(partType)) {
            ItemStack handle = handleStack(material);
            if (handle.isEmpty()) {
                context.getSource().sendFailure(Component.literal("No handle item is registered for material " + material + "."));
                return 0;
            }
            return giveStack(context, targetBranch, handle);
        }
        if (!MaterialCatalog.isNormalForgingMaterial(material)) {
            context.getSource().sendFailure(Component.literal(MaterialCatalog.displayNameText(material) + " is a treatment material, not a tool part material."));
            return 0;
        }
        ForgeTemplateDefinition template = partTemplate(partType).orElse(null);
        if (template != null) {
            if (!template.allowsMaterial(material)) {
                context.getSource().sendFailure(Component.literal(MaterialCatalog.displayNameText(material) + " cannot be used for " + partType + "."));
                return 0;
            }
            ItemStack stack = template.outputStack(material, ForgingQuality.clampScore(quality));
            if (stack.isEmpty()) {
                context.getSource().sendFailure(Component.literal("Could not create modular tool part " + partType + "."));
                return 0;
            }
            return giveStack(context, targetBranch, stack);
        }
        ToolTypeDefinition definition = partDefinition(partType).orElse(null);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("Unknown modular tool part type: " + partType + "."));
            return 0;
        }
        ItemStack stack = definition.createPart(partType, material, ForgingQuality.clampScore(quality));
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Could not create modular tool part " + partType + "."));
            return 0;
        }
        return giveStack(context, targetBranch, stack);
    }

    private static Optional<ToolTypeDefinition> partDefinition(String partType) {
        return ToolTypeRegistry.toolTypes().stream()
                .filter(definition -> definition.primaryPartType().equals(partType)
                        || definition.requiredAssemblyParts().contains(partType)
                        || definition.partTypes().contains(partType))
                .findFirst();
    }

    private static Optional<ForgeTemplateDefinition> partTemplate(String partType) {
        return ToolTypeRegistry.templates().stream()
                .filter(ModCommands::isToolPartTemplate)
                .filter(template -> template.partType().equals(partType))
                .findFirst();
    }

    private static boolean isToolPartTemplate(ForgeTemplateDefinition template) {
        String partType = template.partType();
        return template.outputItem() != null
                || partType.endsWith("_head")
                || partType.endsWith("_blade")
                || partType.endsWith("_guard");
    }

    private static boolean isHandlePart(String partType) {
        return "handle".equals(partType);
    }

    private static ItemStack handleStack(ResourceLocation material) {
        if (MaterialCatalog.OAK.equals(material)) {
            return new ItemStack(Items.STICK);
        }
        if (MaterialCatalog.BLAZE.equals(material)) {
            return new ItemStack(Items.BLAZE_ROD);
        }
        if (MaterialCatalog.BREEZE.equals(material)) {
            return new ItemStack(Items.BREEZE_ROD);
        }
        return ItemStack.EMPTY;
    }

    private static int giveStack(CommandContext<CommandSourceStack> context, boolean targetBranch, ItemStack stack) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = targetBranch ? EntityArgument.getPlayers(context, "targets") : List.of(context.getSource().getPlayerOrException());
        for (ServerPlayer player : targets) {
            ItemStack copy = stack.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
            player.containerMenu.broadcastChanges();
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.mobstoolforging.give_debug.success", stack.getHoverName(), targets.size()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static ResourceLocation material(CommandContext<CommandSourceStack> context, String name) {
        return ResourceLocationArgument.getId(context, name);
    }

    private static Optional<ResourceLocation> optionalMaterial(CommandContext<CommandSourceStack> context, String name) {
        ResourceLocation value = material(context, name);
        return NONE.equals(value) ? Optional.empty() : Optional.of(value);
    }

    private static int convertVanillaTool(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ItemStack original = player.getMainHandItem();
        Component originalName = original.getHoverName().copy();
        ItemStack converted = VanillaToolConverter.convertLootOrEquipment(original, MaterialCatalog.OAK);
        if (converted.isEmpty()) {
            source.sendFailure(Component.translatable("commands.mobstoolforging.convert_vanilla_tool.not_supported"));
            return 0;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, converted);
        player.containerMenu.broadcastChanges();
        source.sendSuccess(() -> Component.translatable(
                "commands.mobstoolforging.convert_vanilla_tool.success",
                originalName,
                converted.getHoverName()
        ), true);
        return Command.SINGLE_SUCCESS;
    }
}
