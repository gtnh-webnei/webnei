package moe.takochan.webnei.asset;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import moe.takochan.webnei.config.AssetsProperties;
import moe.takochan.webnei.model.dto.DatasetSummary;

import org.springframework.stereotype.Component;

@Component
public class AssetUrlBuilder {

    private final AssetsProperties assetsProperties;

    public AssetUrlBuilder(AssetsProperties assetsProperties) {
        this.assetsProperties = assetsProperties;
    }

    public String build(DatasetSummary dataset, String assetPath, String sha256) {
        if (dataset == null) {
            throw new IllegalArgumentException("dataset must not be null");
        }
        if (assetPath == null || assetPath.isBlank()) {
            return null;
        }
        String prefix = assetsProperties.publicUrl() + "/"
                + encodeSegment(dataset.packSlug()) + "/"
                + encodeSegment(dataset.packVersion()) + "/"
                + encodeSegment(dataset.variant()) + "/";
        String path = Arrays.stream(assetPath.split("/"))
                .map(AssetUrlBuilder::encodeSegment)
                .collect(Collectors.joining("/"));
        String url = prefix + path;
        if (sha256 != null && !sha256.isBlank()) {
            url = url + "?v=" + sha256;
        }
        return url;
    }

    private static String encodeSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return "";
        }
        return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
