package moe.takochan.webnei.model.dto;

import java.util.List;

import moe.takochan.webnei.model.dto.ItemRef;

public record GtOreVeinSummary(
        String veinName,
        String displayName,
        ItemRef primaryItem,
        List<GtDimensionRef> dimensions) {
}
