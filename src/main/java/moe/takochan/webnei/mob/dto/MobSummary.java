package moe.takochan.webnei.mob.dto;

public record MobSummary(
        String mobVariantId,
        String mobId,
        String modId,
        String entityName,
        String displayName,
        double width,
        double height,
        double maxHealth,
        int armor,
        boolean immuneToFire,
        boolean leashable,
        String assetUrl) {
}
