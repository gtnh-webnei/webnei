package moe.takochan.webnei.gtore;

import java.util.List;

public record GtBartWorksOreDetail(
        String entryType,
        String dimensionDisplayName,
        GtItemRef resultItem,
        int heightMin,
        int heightMax,
        int weight,
        int density,
        int size,
        int amountPerChunk,
        List<GtBartWorksOreLayer> layers) {
}
