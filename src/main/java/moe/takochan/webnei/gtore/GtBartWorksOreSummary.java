package moe.takochan.webnei.gtore;

public record GtBartWorksOreSummary(
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
        int amountPerChunk) {
}
