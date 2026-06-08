package moe.takochan.webnei.gtore;

import java.util.List;

import moe.takochan.webnei.common.ItemRef;

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
