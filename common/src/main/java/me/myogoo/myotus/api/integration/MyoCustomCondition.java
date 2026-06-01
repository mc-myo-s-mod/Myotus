package me.myogoo.myotus.api.integration;

import me.myogoo.myotus.dto.MyoModInfoDto;

@FunctionalInterface
public interface MyoCustomCondition {
    boolean test(MyoModInfoDto modInfo);
}
