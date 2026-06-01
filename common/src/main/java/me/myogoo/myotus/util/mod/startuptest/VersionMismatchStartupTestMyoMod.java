package me.myogoo.myotus.util.mod.startuptest;

import me.myogoo.myotus.api.annotation.MyoMod;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@MyoMod(value = "myotus", versionRange = "[999.0.0,)")
@Retention(RetentionPolicy.RUNTIME)
public @interface VersionMismatchStartupTestMyoMod {
}
