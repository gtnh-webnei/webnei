package moe.takochan.webnei.asset;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import moe.takochan.webnei.dataset.entity.DatasetEntity;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

@Service
public class AssetUrlService {

    private static final String PATH_SEPARATOR = "/";
    private static final char PATH_SEPARATOR_CHAR = '/';

    private final AssetProperties properties;

    public AssetUrlService(AssetProperties properties) {
        this.properties = properties;
    }

    public String assetUrl(DatasetEntity dataset, String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            return null;
        }

        List<String> segments = new ArrayList<>();
        segments.add(dataset.getPackSlug());
        segments.add(dataset.getPackVersion());
        segments.add(dataset.getVariant());
        for (String segment : assetPath.split(PATH_SEPARATOR)) {
            if (!segment.isBlank()) {
                segments.add(segment);
            }
        }
        String path = segments.stream()
                .map(AssetUrlService::encodePathSegment)
                .collect(Collectors.joining(PATH_SEPARATOR));
        return joinUrl(properties.getPublicUrl(), path);
    }

    private static String joinUrl(String prefix, String path) {
        if (prefix.isBlank()) {
            return path;
        }
        String normalizedPrefix = trimTrailingSeparators(prefix);
        if (normalizedPrefix.isEmpty()) {
            return PATH_SEPARATOR + path;
        }
        return normalizedPrefix + PATH_SEPARATOR + path;
    }

    private static String trimTrailingSeparators(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == PATH_SEPARATOR_CHAR) {
            end--;
        }
        return value.substring(0, end);
    }

    private static String encodePathSegment(String segment) {
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }
}
