package me.myogoo.myotus.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MyoLogger {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final Logger LOGGER = LoggerFactory.getLogger(MyoLogger.class);

    private MyoLogger() {
    }

    public static Logger unwrap() {
        return LOGGER;
    }

    public static void trace(String message, Object... arguments) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(format("TRACE", message), arguments);
        }
    }

    public static void debug(String message, Object... arguments) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(format("DEBUG", message), arguments);
        }
    }

    public static void info(String message, Object... arguments) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(format("INFO", message), arguments);
        }
    }

    public static void warn(String message, Object... arguments) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(format("WARN", message), arguments);
        }
    }

    public static void error(String message, Object... arguments) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(format("ERROR", message), arguments);
        }
    }

    private static String format(String level, String message) {
        return "[" + level + "][" + LocalTime.now().format(TIME_FORMATTER) + "][" + caller() + "] " + message;
    }

    private static String caller() {
        return STACK_WALKER.walk(frames -> frames
                .dropWhile(frame -> frame.getDeclaringClass() == MyoLogger.class)
                .findFirst()
                .map(MyoLogger::formatCaller)
                .orElse("unknown#unknown"));
    }

    private static String formatCaller(StackWalker.StackFrame frame) {
        return frame.getDeclaringClass().getSimpleName() + "#" + frame.getMethodName();
    }
}
