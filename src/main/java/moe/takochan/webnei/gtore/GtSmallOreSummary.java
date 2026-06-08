package moe.takochan.webnei.gtore;

import java.util.List;

import moe.takochan.webnei.common.ItemRef;

public record GtSmallOreSummary(
        String oreGenName,
        ItemRef smallOreItem,
        List<GtDimensionRef> dimensions) {
}
