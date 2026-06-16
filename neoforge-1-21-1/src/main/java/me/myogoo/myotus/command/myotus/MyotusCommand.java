package me.myogoo.myotus.command.myotus;

import com.mojang.brigadier.context.CommandContext;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.annotation.commands.MyoCommand;
import me.myogoo.myotus.api.annotation.commands.MyoExecute;
import me.myogoo.myotus.api.annotation.commands.MyoPermission;
import me.myogoo.myotus.api.command.permission.MyoPermissionLevel;
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
            ModIntegrationManager.getRegisteredIntegrations().forEach(x -> {
                String status = x.active() ? "active" : "inactive";
                context.getSource().sendSuccess(() -> Component.literal("%s [%s]".formatted(x.modId(), status)), true);
            });
            return 1;
        }
    }

    @MyoCommand(value = "dev", parent = MyotusCommand.class)
    @MyoPermission(permission = MyoPermissionLevel.ADMIN)
    public static class DevModeCommand {
        @MyoExecute
        public static int execute(CommandSourceStack source) {
            Myotus.DEV_MODE = !Myotus.DEV_MODE;
            source.sendSuccess(() -> Component.literal("Myotus dev mode is now %s. Run /reload to re-evaluate conditional recipes.".formatted(Myotus.DEV_MODE ? "enabled" : "disabled")), false);
            return 1;
        }
    }
}
