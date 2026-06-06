package moe.takochan.webnei.gtore;

import java.util.List;

public record GtSmallOreDetail(
        int amountPerChunk,
        int heightMin,
        int heightMax,
        GtItemRef smallOreItem,
        GtItemRef dustItem,
        List<GtItemRef> drops,
        List<GtDimensionRef> dimensions) {
}
