package me.myogoo.myotus.util.mod;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

public class ModVersionHelper {

    /**
     * versionRange 문자열이 "*"이면 모든 버전 허용.
     * 그 외에는 Maven VersionRange 형식으로 파싱하여 actualVersion이 포함되는지 체크.
     * 예: "[1.5.0,)" → 1.5.0 이상, "[1.0,2.0)" → 1.0 이상 2.0 미만
     */
    public static boolean isVersionInRange(String versionRange, ArtifactVersion actualVersion) {
        if (versionRange == null || versionRange.equals("*") || versionRange.isEmpty()) {
            return true;
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
        } catch (InvalidVersionSpecificationException e) {
            // Ignore exception and return default
        }

        return new DefaultArtifactVersion("0.0.0");
    }
}
