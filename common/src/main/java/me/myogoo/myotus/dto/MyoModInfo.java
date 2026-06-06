package me.myogoo.myotus.dto;

import org.apache.maven.artifact.versioning.ArtifactVersion;

public record MyoModInfo(String modId, String namespace, String displayName, ArtifactVersion version) {
    public MyoModInfo(String modId, String namespace, ArtifactVersion version) {
        this(modId, namespace, modId, version);
    }
}
