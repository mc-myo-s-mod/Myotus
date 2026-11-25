package me.myogoo.myotus.client;

import java.util.Optional;

/** Maps an AE2 style document to its dedicated wide sibling. */
public final class TerminalWidthPaths {
    private TerminalWidthPaths() {
    }

    public static Optional<String> widePath(String nativePath) {
        if (nativePath == null || !nativePath.startsWith("/") || !nativePath.endsWith(".json")
                || nativePath.endsWith("/.json") || nativePath.endsWith("_wide.json")) {
            return Optional.empty();
        }
        return Optional.of(nativePath.substring(0, nativePath.length() - ".json".length()) + "_wide.json");
    }
}
