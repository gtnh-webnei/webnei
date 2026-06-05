package moe.takochan.webnei.gtore;

import java.util.List;

public record GtSmallOreDetail(
        String oreGenName,
        int oreMeta,
        String materialName,
        int amountPerChunk,
        int heightMin,
        int heightMax,
        GtItemRef smallOreItem,
        GtItemRef dustItem,
        List<GtSmallOreVariant> variants,
        List<GtItemRef> drops,
        List<GtDimensionRef> dimensions) {
}
