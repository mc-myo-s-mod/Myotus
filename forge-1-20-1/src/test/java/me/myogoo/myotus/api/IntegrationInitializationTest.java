package me.myogoo.myotus.api;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationInitializationTest {
    @Test
    void concurrentAccessWaitsForOneInitialization() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var runs = new AtomicInteger();
        var initialization = new MyotusAPI.InitializationTask(() -> {
            runs.incrementAndGet();
            started.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(initialization::run);
            assertTrue(started.await(1, TimeUnit.SECONDS));
            var second = executor.submit(initialization::run);

            assertThrows(TimeoutException.class, () -> second.get(100, TimeUnit.MILLISECONDS));
            release.countDown();

            first.get(1, TimeUnit.SECONDS);
            second.get(1, TimeUnit.SECONDS);
            initialization.run();
            assertEquals(1, runs.get());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }
}
