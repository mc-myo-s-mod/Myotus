package me.myogoo.myotus.api;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.myogoo.myotus.api.command.argument.MyoArgumentAdapter;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandsApiTest {
    @Test
    void registersCustomArgumentsWithoutSilentlyReplacingExistingAdapters() {
        MyoArgumentAdapter<CustomValue> adapter = new MyoArgumentAdapter<>() {
            @Override
            public ArgumentType<?> argumentType() {
                return StringArgumentType.word();
            }

            @Override
            public CustomValue value(CommandContext<CommandSourceStack> context, String name) {
                return new CustomValue(StringArgumentType.getString(context, name));
            }
        };

        assertDoesNotThrow(() -> MyotusAPI.commands().registerArgument(CustomValue.class, adapter));
        assertThrows(IllegalArgumentException.class,
                () -> MyotusAPI.commands().registerArgument(CustomValue.class, adapter));
    }

    @Test
    void concurrentRegistrationAcceptsExactlyOneAdapter() throws Exception {
        MyoArgumentAdapter<ConcurrentValue> adapter = new MyoArgumentAdapter<>() {
            @Override
            public ArgumentType<?> argumentType() {
                return StringArgumentType.word();
            }

            @Override
            public ConcurrentValue value(CommandContext<CommandSourceStack> context, String name) {
                return new ConcurrentValue(StringArgumentType.getString(context, name));
            }
        };
        int workerCount = 16;
        var ready = new CountDownLatch(workerCount);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(workerCount);
        var results = new ArrayList<Future<Boolean>>(workerCount);
        try {
            for (int i = 0; i < workerCount; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        MyotusAPI.commands().registerArgument(ConcurrentValue.class, adapter);
                        return true;
                    } catch (IllegalArgumentException duplicate) {
                        return false;
                    }
                }));
            }
            ready.await();
            start.countDown();

            int successes = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                }
            }
            assertEquals(1, successes);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private record CustomValue(String value) {
    }

    private record ConcurrentValue(String value) {
    }
}
