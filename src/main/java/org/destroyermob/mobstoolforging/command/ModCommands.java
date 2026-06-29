package org.destroyermob.mobstoolforging.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
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
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ArmorStatsCatalog;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.ToolConstructionData;
import org.destroyermob.mobstoolforging.world.ToolKind;
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
                .then(giveDebug(true)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> giveDebug(boolean targetBranch) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(targetBranch ? "give_debug_to" : "give_debug");
        if (targetBranch) {
            return root.then(Commands.argument("targets", EntityArgument.players())
                    .then(armorRoot(true))
                    .then(toolRoot(true)));
        }
        return root.then(armorRoot(false))
                .then(toolRoot(false));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> armorRoot(boolean targetBranch) {
        return Commands.literal("armor")
                .then(helmetCommand(targetBranch))
                .then(chestplateCommand(targetBranch))
                .then(leggingsCommand(targetBranch))
                .then(bootsCommand(targetBranch));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> helmetCommand(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> skull = materialArgument("skull", true);
        skull.executes(context -> giveHelmet(context, targetBranch, material(context, "skull"), Optional.empty(), Optional.empty()));
        skull.then(optionalMaterialArgument("comb", true)
                .executes(context -> giveHelmet(context, targetBranch, material(context, "skull"), optionalMaterial(context, "comb"), Optional.empty()))
                .then(optionalMaterialArgument("visor", true)
                        .executes(context -> giveHelmet(context, targetBranch, material(context, "skull"), optionalMaterial(context, "comb"), optionalMaterial(context, "visor")))));
        return Commands.literal("helmet").then(skull);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> chestplateCommand(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> body = materialArgument("body", true);
        body.executes(context -> giveChestplate(context, targetBranch, material(context, "body")));
        return Commands.literal("chestplate").then(body);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leggingsCommand(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> legs = materialArgument("legs", true);
        legs.executes(context -> giveLeggings(context, targetBranch, material(context, "legs")));
        return Commands.literal("leggings").then(legs);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bootsCommand(boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> feet = materialArgument("feet", true);
        feet.executes(context -> giveBoots(context, targetBranch, material(context, "feet")));
        return Commands.literal("boots").then(feet);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> toolRoot(boolean targetBranch) {
        LiteralArgumentBuilder<CommandSourceStack> tool = Commands.literal("tool");
        tool.then(toolCommand(ToolKind.SWORD, targetBranch, true));
        tool.then(toolCommand(ToolKind.PICKAXE, targetBranch, false));
        tool.then(toolCommand(ToolKind.AXE, targetBranch, false));
        tool.then(toolCommand(ToolKind.SHOVEL, targetBranch, false));
        tool.then(toolCommand(ToolKind.HOE, targetBranch, false));
        return tool;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> toolCommand(ToolKind toolKind, boolean targetBranch, boolean requiresGuard) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> head = materialArgument("head", false);
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> handle = materialArgument("handle", false);
        head.then(handle);
        if (requiresGuard) {
            handle.then(requiredToolTail(toolKind, targetBranch));
        } else {
            handle.executes(context -> giveTool(context, targetBranch, toolKind, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
            handle.then(optionalToolTail(toolKind, targetBranch, "binding", 0));
        }
        return Commands.literal(toolKind.id()).then(head);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> requiredToolTail(ToolKind toolKind, boolean targetBranch) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> guard = materialArgument("guard", false);
        guard.executes(context -> giveTool(context, targetBranch, toolKind, Optional.of(material(context, "guard")), Optional.empty(), Optional.empty(), Optional.empty()));
        guard.then(optionalToolTail(toolKind, targetBranch, "binding", 0));
        return guard;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> optionalToolTail(ToolKind toolKind, boolean targetBranch, String name, int index) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> argument = optionalMaterialArgument(name, false);
        argument.executes(context -> giveToolFromContext(context, targetBranch, toolKind));
        if (index == 0) {
            argument.then(optionalToolTail(toolKind, targetBranch, "wrap", 1));
        } else if (index == 1) {
            argument.then(optionalToolTail(toolKind, targetBranch, "focus", 2));
        } else if (index == 2) {
            argument.then(optionalToolTail(toolKind, targetBranch, "treatment", 3));
        }
        return argument;
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

    private static List<ResourceLocation> armorMaterials() {
        return MaterialCatalog.starterMaterialIds().stream()
                .filter(ArmorStatsCatalog::isSupportedArmorMaterial)
                .toList();
    }

    private static List<ResourceLocation> toolMaterials() {
        List<ResourceLocation> values = new ArrayList<>();
        addAll(values, MaterialCatalog.starterMaterialIds());
        addAll(values, MaterialCatalog.handleMaterialIds());
        addAll(values, MaterialCatalog.visualMaterialIds("bindingMaterial"));
        addAll(values, MaterialCatalog.visualMaterialIds("wrapMaterial"));
        addAll(values, MaterialCatalog.visualMaterialIds("focusMaterial"));
        addAll(values, MaterialCatalog.visualMaterialIds("treatment"));
        return List.copyOf(values);
    }

    private static void addAll(List<ResourceLocation> values, List<ResourceLocation> additions) {
        additions.forEach(value -> {
            if (!values.contains(value)) {
                values.add(value);
            }
        });
    }

    private static int giveHelmet(CommandContext<CommandSourceStack> context, boolean targetBranch, ResourceLocation skull, Optional<ResourceLocation> comb, Optional<ResourceLocation> visor) throws CommandSyntaxException {
        if (!ArmorStatsCatalog.isSupportedArmorMaterial(skull)) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(skull)));
            return 0;
        }
        if (comb.isPresent() && !ArmorStatsCatalog.isSupportedArmorMaterial(comb.get())) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(comb.get())));
            return 0;
        }
        if (visor.isPresent() && !ArmorStatsCatalog.isSupportedArmorMaterial(visor.get())) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(visor.get())));
            return 0;
        }
        ItemStack stack = ModItems.MODULAR_HELMET.get().create(skull, comb, visor);
        return giveStack(context, targetBranch, stack);
    }

    private static int giveChestplate(CommandContext<CommandSourceStack> context, boolean targetBranch, ResourceLocation body) throws CommandSyntaxException {
        if (!ArmorStatsCatalog.isSupportedArmorMaterial(body)) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(body)));
            return 0;
        }
        ItemStack stack = ModItems.MODULAR_CHESTPLATE.get().create(body);
        return giveStack(context, targetBranch, stack);
    }

    private static int giveLeggings(CommandContext<CommandSourceStack> context, boolean targetBranch, ResourceLocation legs) throws CommandSyntaxException {
        if (!ArmorStatsCatalog.isSupportedArmorMaterial(legs)) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(legs)));
            return 0;
        }
        ItemStack stack = ModItems.MODULAR_LEGGINGS.get().create(legs);
        return giveStack(context, targetBranch, stack);
    }

    private static int giveBoots(CommandContext<CommandSourceStack> context, boolean targetBranch, ResourceLocation feet) throws CommandSyntaxException {
        if (!ArmorStatsCatalog.isSupportedArmorMaterial(feet)) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.invalid_armor_material", MaterialCatalog.displayName(feet)));
            return 0;
        }
        ItemStack stack = ModItems.MODULAR_BOOTS.get().create(feet);
        return giveStack(context, targetBranch, stack);
    }

    private static int giveToolFromContext(CommandContext<CommandSourceStack> context, boolean targetBranch, ToolKind toolKind) throws CommandSyntaxException {
        Optional<ResourceLocation> guard = toolKind == ToolKind.SWORD ? Optional.of(material(context, "guard")) : Optional.empty();
        return giveTool(
                context,
                targetBranch,
                toolKind,
                guard,
                optionalArgument(context, "binding"),
                optionalArgument(context, "wrap"),
                optionalArgument(context, "focus"),
                optionalArgument(context, "treatment")
        );
    }

    private static int giveTool(CommandContext<CommandSourceStack> context, boolean targetBranch, ToolKind toolKind, Optional<ResourceLocation> guard, Optional<ResourceLocation> binding, Optional<ResourceLocation> wrap, Optional<ResourceLocation> focus) throws CommandSyntaxException {
        return giveTool(context, targetBranch, toolKind, guard, binding, wrap, focus, Optional.empty());
    }

    private static int giveTool(
            CommandContext<CommandSourceStack> context,
            boolean targetBranch,
            ToolKind toolKind,
            Optional<ResourceLocation> guard,
            Optional<ResourceLocation> binding,
            Optional<ResourceLocation> wrap,
            Optional<ResourceLocation> focus,
            Optional<ResourceLocation> treatment
    ) throws CommandSyntaxException {
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
                binding,
                wrap,
                focus,
                treatment,
                ToolConstructionData.DEFAULT_QUALITY
        );
        ItemStack stack = definition.createTool(construction);
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.mobstoolforging.give_debug.tool_failed", toolKind.id()));
            return 0;
        }
        return giveStack(context, targetBranch, stack);
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

    private static Optional<ResourceLocation> optionalArgument(CommandContext<CommandSourceStack> context, String name) {
        try {
            return optionalMaterial(context, name);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static int convertVanillaTool(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ItemStack original = player.getMainHandItem();
        Component originalName = original.getHoverName().copy();
        ItemStack converted = VanillaToolConverter.convert(original, MaterialCatalog.OAK);
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
