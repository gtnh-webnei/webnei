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

    public String assetUrl(DatasetEntity dataset, String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            return null;
        }

        List<String> segments = new ArrayList<>();
        segments.add("assets");
        segments.add(dataset.getPackSlug());
        segments.add(dataset.getPackVersion());
        segments.add(dataset.getVariant());
        for (String segment : assetPath.split("/")) {
            if (!segment.isBlank()) {
                segments.add(segment);
            }
        }
        return segments.stream()
                .map(AssetUrlService::encodePathSegment)
                .collect(Collectors.joining("/", "/", ""));
    }

    private static String encodePathSegment(String segment) {
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }
}
