package org.destroyermob.mobstoolforging.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.VanillaToolConverter;

public final class ModCommands {
    private ModCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(MobsToolForging.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("convert_vanilla_tool")
                        .executes(ModCommands::convertVanillaTool)));
    }

    private static int convertVanillaTool(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ItemStack original = player.getMainHandItem();
        Component originalName = original.getHoverName().copy();
        ItemStack converted = VanillaToolConverter.convert(original, MaterialCatalog.OAK);
        if (converted.isEmpty()) {
            source.sendFailure(Component.translatable("commands.mobstoolforging.convert_vanilla_tool.not_vanilla"));
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
