package me.myogoo.myotus.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.myogoo.myotus.Myotus;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.io.IOException;
import java.nio.file.Path;

@EventBusSubscriber(modid = Myotus.MODID, value = Dist.CLIENT)
public final class MyotusIsorenderClientCommand {
    private static final int DEFAULT_SIZE = 512;
    private static final int MIN_SIZE = 16;
    private static final int MAX_SIZE = 4096;

    private MyotusIsorenderClientCommand() {
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        register(event.getDispatcher(), event.getBuildContext());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("myotus")
                .then(Commands.literal("isorender")
                        .executes(MyotusIsorenderClientCommand::showHelp)
                        .then(Commands.literal("block")
                                .then(Commands.argument("block", BlockStateArgument.block(buildContext))
                                        .executes(context -> renderBlock(context, DEFAULT_SIZE))
                                        .then(Commands.argument("size", IntegerArgumentType.integer(MIN_SIZE, MAX_SIZE))
                                                .executes(context -> renderBlock(context, IntegerArgumentType.getInteger(context, "size"))))))));
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Usage: /myotus isorender block <block_state> [size]"), false);
        context.getSource().sendSuccess(() -> Component.literal("Example: /myotus isorender block minecraft:dirt 512"), false);
        context.getSource().sendSuccess(() -> Component.literal("Example: /myotus isorender block minecraft:oak_log[axis=y] 512"), false);
        return 1;
    }

    private static int renderBlock(CommandContext<CommandSourceStack> context, int size) throws CommandSyntaxException {
        BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
        BlockState state = blockInput.getState();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        context.getSource().sendSuccess(() -> Component.literal(
                "Queued block isorender export: " + state + " at " + size + "px"), false);
        Minecraft.getInstance().execute(() -> {
            try {
                Path output = MyotusIsorenderExporter.exportBlock(state, blockId, size);
                context.getSource().sendSuccess(() -> Component.literal(
                        "Exported block isorender: " + output.toAbsolutePath()), false);
            } catch (IOException | RuntimeException e) {
                Myotus.LOGGER.error("Failed to export isometric block render for {}", blockId, e);
                context.getSource().sendFailure(Component.literal("Failed to export block isorender: " + e.getMessage()));
            }
        });
        return 1;
    }
}
