package moe.takochan.webnei.gtore;

import java.util.List;

public record GtOreVeinSummary(
        String veinName,
        String displayName,
        int heightMin,
        int heightMax,
        int weight,
        GtItemRef primaryItem,
        List<GtDimensionRef> dimensions) {
}
