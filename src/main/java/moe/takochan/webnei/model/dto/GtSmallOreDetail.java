package moe.takochan.webnei.model.dto;

import java.util.List;

import moe.takochan.webnei.model.dto.ItemRef;

public record GtSmallOreDetail(
        int amountPerChunk,
        int heightMin,
        int heightMax,
        ItemRef smallOreItem,
        ItemRef dustItem,
        List<ItemRef> drops,
        List<GtDimensionRef> dimensions) {
}
