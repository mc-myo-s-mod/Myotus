package me.myogoo.myotus.dto;

import org.apache.maven.artifact.versioning.ArtifactVersion;

public record MyoModInfoDto(String modId, String namespace, String displayName, ArtifactVersion version) {
    public MyoModInfoDto(String modId, String namespace, ArtifactVersion version) {
        this(modId, namespace, modId, version);
    }
}
