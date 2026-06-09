package moe.takochan.webnei.model.dto;

import java.util.List;

import moe.takochan.webnei.model.dto.ItemRef;

public record GtSmallOreSummary(
        String oreGenName,
        ItemRef smallOreItem,
        List<GtDimensionRef> dimensions) {
}
