package moe.takochan.webnei.gtore;

import java.util.List;

import moe.takochan.webnei.common.ItemRef;

public record GtOreVeinSummary(
        String veinName,
        String displayName,
        ItemRef primaryItem,
        List<GtDimensionRef> dimensions) {
}
