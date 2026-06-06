package moe.takochan.webnei.gtore;

import java.util.List;

public record GtOreVeinSummary(
        String veinName,
        String displayName,
        GtItemRef primaryItem,
        List<GtDimensionRef> dimensions) {
}
