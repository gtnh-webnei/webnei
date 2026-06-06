package moe.takochan.webnei.gtore;

import java.util.List;

public record GtSmallOreSummary(
        String oreGenName,
        GtItemRef smallOreItem,
        List<GtDimensionRef> dimensions) {
}
