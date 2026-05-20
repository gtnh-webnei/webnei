package moe.takochan.webnei.mob.dto;

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
