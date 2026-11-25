package me.myogoo.myotus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.myogoo.ssec.api.command.SSCArgument;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import me.myogoo.ssec.api.command.SSCExecute;
import me.myogoo.ssec.api.command.SSCommand;
import me.myogoo.ssec.api.command.argument.SSCArgumentAdapter;
import me.myogoo.ssec.command.CommandRegistrar;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandDiscoveryTest {
    @Test
    void discoversSsecCommandsAndPassesBuildContextWithoutInitializingCommands() throws Exception {
        var buildContext = mock(CommandBuildContext.class);
        var builds = new AtomicInteger();
        CommandRegistrar.registerAdapter(ContextValue.class, new SSCArgumentAdapter<ContextValue>() {
            @Override
            public ArgumentType<?> argumentType(CommandBuildContext context) {
                assertSame(buildContext, context);
                builds.incrementAndGet();
                return StringArgumentType.word();
            }

            @Override
            public ContextValue value(CommandContext<CommandSourceStack> context, String name) {
                return new ContextValue(StringArgumentType.getString(context, name));
            }
        });
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        var event = mock(RegisterCommandsEvent.class);
        when(event.getDispatcher()).thenReturn(dispatcher);
        when(event.getBuildContext()).thenReturn(buildContext);
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                new ScannedAnnotation(Type.getType(SSCommand.class), Type.getType(ContextCommand.class)),
                new ScannedAnnotation(Type.getType(SSCommand.class), Type.getType(ContextCommand.class)),
                new ScannedAnnotation(Type.getType(SSCommand.class), Type.getType(NativeCommand.class)),
                new ScannedAnnotation(Type.getType(Deprecated.class), Type.getObjectType("missing/UnrelatedClass"))));
        try {
            me.myogoo.myotus.init.MyotusCommand.onRegisterCommands(event);
            assertEquals(1, builds.get());
            assertEquals(0, Initialization.count);
            assertNotNull(dispatcher.getRoot().getChild("discoveredNative"));
            assertEquals(6, dispatcher.execute("discoveredContext linked", mock(CommandSourceStack.class)));
            assertEquals(1, Initialization.count);
        } finally {
            AnnotationScanner.setAnnotationProvider(Stream::empty);
        }
    }

    private record ContextValue(String value) {
    }

    private static final class Initialization {
        private static int count;
    }

    @SSCommand("discoveredContext")
    static final class ContextCommand {
        static { Initialization.count++; }

        @SSCExecute
        static int execute(@SSCArgument("value") ContextValue value) { return value.value().length(); }
    }

    @SSCommand("discoveredNative")
    static final class NativeCommand {
        @SSCExecute static int execute() { return 1; }
    }
}
