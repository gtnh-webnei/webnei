package moe.takochan.webnei.model.dto;

import java.util.List;

public record GtOreVeinDetail(
        String displayName,
        int weight,
        int size,
        int density,
        int heightMin,
        int heightMax,
        List<GtOreVeinLayer> layers,
        List<GtDimensionRef> dimensions) {
}
