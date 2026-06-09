package moe.takochan.webnei.model.dto;

public record AspectEntry(
        String aspectId,
        String name,
        String description,
        boolean primal,
        int amount,
        String iconItemVariantId,
        String iconAssetUrl) {
}
