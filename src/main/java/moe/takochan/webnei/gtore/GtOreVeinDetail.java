package moe.takochan.webnei.gtore;

import java.util.List;

public record GtOreVeinDetail(
        String veinName,
        String displayName,
        int weight,
        int size,
        int density,
        int heightMin,
        int heightMax,
        List<GtOreVeinLayer> layers,
        List<GtDimensionRef> dimensions) {
}
