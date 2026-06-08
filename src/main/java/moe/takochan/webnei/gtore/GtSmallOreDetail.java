package moe.takochan.webnei.gtore;

import java.util.List;

import moe.takochan.webnei.common.ItemRef;

public record GtSmallOreDetail(
        int amountPerChunk,
        int heightMin,
        int heightMax,
        ItemRef smallOreItem,
        ItemRef dustItem,
        List<ItemRef> drops,
        List<GtDimensionRef> dimensions) {
}
