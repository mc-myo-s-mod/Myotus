package me.myogoo.myotus.util.mod;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

public class ModVersionHelper {
    public static boolean isVersionInRange(String versionRange, ArtifactVersion actualVersion) {
        if (versionRange == null || versionRange.equals("*") || versionRange.isEmpty()) {
            return true;
        }
        if (actualVersion == null) {
            return false;
        }

        try {
            VersionRange range = VersionRange.createFromVersionSpec(versionRange);
            return range.containsVersion(actualVersion);
        } catch (InvalidVersionSpecificationException e) {
            return false;
        }
    }

    public static ArtifactVersion getMinimumVersion(String versionRange) {
        if (versionRange == null || versionRange.equals("*") || versionRange.isEmpty()) {
            return new DefaultArtifactVersion("0.0.0");
        }

        try {
            VersionRange range = VersionRange.createFromVersionSpec(versionRange);
            if (range.getRecommendedVersion() != null) {
                return range.getRecommendedVersion();
            }
            if (!range.getRestrictions().isEmpty()) {
                ArtifactVersion lowerBound = range.getRestrictions().get(0).getLowerBound();
                if (lowerBound != null) {
                    return lowerBound;
                }
            }
        } catch (InvalidVersionSpecificationException ignored) {
        }

        return new DefaultArtifactVersion("0.0.0");
    }
}
