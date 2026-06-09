package moe.takochan.webnei.model.dto;

public record MobDropRow(
        String dropType,
        int listIndex,
        String itemVariantId,
        String displayName,
        String assetUrl,
        int stackSize,
        double probability,
        boolean lootable,
        boolean playerOnly) {
}
