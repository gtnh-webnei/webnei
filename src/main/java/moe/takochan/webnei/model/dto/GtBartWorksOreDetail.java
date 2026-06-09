package moe.takochan.webnei.model.dto;

import java.util.List;

import moe.takochan.webnei.model.dto.ItemRef;

public record GtBartWorksOreDetail(
        String entryType,
        String dimensionDisplayName,
        ItemRef resultItem,
        int heightMin,
        int heightMax,
        int weight,
        int density,
        int size,
        int amountPerChunk,
        List<GtBartWorksOreLayer> layers) {
}
