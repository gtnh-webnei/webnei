package moe.takochan.webnei.gtore;

import java.util.List;

public record GtBartWorksOreDetail(
        String entryId,
        String entryType,
        String worldgenName,
        String dimension,
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
