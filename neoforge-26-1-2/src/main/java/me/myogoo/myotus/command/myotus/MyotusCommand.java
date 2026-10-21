package me.myogoo.myotus.command.myotus;

import com.mojang.brigadier.context.CommandContext;
import me.myogoo.myotus.Myotus;
import me.myogoo.ssec.api.command.SSCommand;
import me.myogoo.ssec.api.command.SSCExecute;
import me.myogoo.ssec.api.command.SSCPermission;
import me.myogoo.ssec.api.command.permission.PermissionLevel;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

@SSCommand("myotus")
public class MyotusCommand {
    @SSCommand(value = "active_mod", parent = MyotusCommand.class)
    public static class LoadTestCommand {
        @SSCExecute
        public static int execute(CommandContext<CommandSourceStack> context) {
            context.getSource().sendSuccess(() -> Component.literal("Active Mod Integrations:"), false);
            ModIntegrationManager.getActiveIntegrations().keySet().forEach(x -> {
                context.getSource().sendSuccess(() -> Component.literal(x.getDisplayModName()), true);
            });
            return 1;
        }
    }

    @SSCommand(value = "loaded_mod", parent = MyotusCommand.class)
    public static class RegisterModCommand {
        @SSCExecute
        public static int execute(CommandContext<CommandSourceStack> context) {
            context.getSource().sendSuccess(() -> Component.literal("Registered Mod Integrations:"), false);
            ModIntegrationManager.getRegisteredIntegrations().forEach(x -> {
                String status = x.active() ? "active" : "inactive";
                context.getSource().sendSuccess(() -> Component.literal("%s [%s]".formatted(x.modId(), status)), true);
            });
            return 1;
        }
    }

    @SSCommand(value = "dev", parent = MyotusCommand.class)
    @SSCPermission(permission = PermissionLevel.ADMIN)
    public static class DevModeCommand {
        @SSCExecute
        public static int execute(CommandSourceStack source) {
            Myotus.DEV_MODE = !Myotus.DEV_MODE;
            source.sendSuccess(() -> Component.literal("Myotus dev mode is now %s. Run /reload to re-evaluate conditional recipes.".formatted(Myotus.DEV_MODE ? "enabled" : "disabled")), false);
            return 1;
        }
    }
}
