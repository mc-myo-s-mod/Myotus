package me.myogoo.myotus.api.integration;

import me.myogoo.myotus.dto.MyoModInfo;

@FunctionalInterface
public interface MyoCustomCondition {
    boolean test(MyoModInfo modInfo);
}
