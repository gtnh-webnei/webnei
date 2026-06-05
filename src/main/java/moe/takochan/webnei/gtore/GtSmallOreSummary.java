package moe.takochan.webnei.gtore;

import java.util.List;

public record GtSmallOreSummary(
        String oreGenName,
        String materialName,
        int amountPerChunk,
        int heightMin,
        int heightMax,
        GtItemRef smallOreItem,
        List<GtDimensionRef> dimensions) {
}
