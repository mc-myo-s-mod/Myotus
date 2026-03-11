package me.myogoo.myotus.commands.myotus;

import com.mojang.brigadier.context.CommandContext;
import me.myogoo.myotus.api.command.MyoCommand;
import me.myogoo.myotus.api.command.MyoExecute;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

@MyoCommand(value = "mods", parent = MyoBaseCommand.class)
public class LoadTestCommand {
    @MyoExecute
    public static int execute(CommandContext<CommandSourceStack> context) {
        ModIntegrationManager.getActiveIntegrations().keySet().stream().forEach(x -> {
            context.getSource().sendSuccess(() -> Component.literal(String.format("Active Integrations: %s", x.getDisplayModName())), true);
        });
        return 1;
    }
}
