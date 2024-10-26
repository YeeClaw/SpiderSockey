package com.iliadstudios.spidersockey;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;

@Mod.EventBusSubscriber
public class CommandHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("mycommand")
            .then(Commands.argument("message", StringArgumentType.string())
            .executes(CommandHandler::executeCommand)));
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) {
        String message = StringArgumentType.getString(context, "message");
        context.getSource().sendSuccess(() -> Component.literal("You ran the command with message: " + message), false);
        // Add your command logic here
        return 1;
    }
}
