package me.myogoo.myotus.command.myotus;

import com.mojang.brigadier.context.CommandContext;
import me.myogoo.myotus.api.annotation.commands.MyoCommand;
import me.myogoo.myotus.api.annotation.commands.MyoExecute;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

@MyoCommand("myotus")
public class MyotusCommand {
    @MyoCommand(value = "active_mod", parent = MyotusCommand.class)
    public static class LoadTestCommand {
        @MyoExecute
        public static int execute(CommandContext<CommandSourceStack> context) {
            context.getSource().sendSuccess(() -> Component.literal("Active Mod Integrations:"), false);
            ModIntegrationManager.getActiveIntegrations().keySet().forEach(x -> {
                context.getSource().sendSuccess(() -> Component.literal(x.getDisplayModName()), true);
            });
            return 1;
        }
    }

    @MyoCommand(value = "loaded_mod", parent = MyotusCommand.class)
    public static class RegisterModCommand {
        @MyoExecute
        public static int execute(CommandContext<CommandSourceStack> context) {
            context.getSource().sendSuccess(() -> Component.literal("Registered Mod Integrations:"), false);
            ModIntegrationManager.getActiveIntegrations().keySet().forEach(x -> {
                if(ModIntegrationManager.isLoaded(x)) {
                    context.getSource().sendSuccess(() -> Component.literal(x.getDisplayModName()), true);
                }
            });
            return 1;
        }
    }
}
