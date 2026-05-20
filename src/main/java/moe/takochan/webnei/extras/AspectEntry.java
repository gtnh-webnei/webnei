package moe.takochan.webnei.extras;

public record AspectEntry(
        String aspectId,
        String name,
        String description,
        boolean primal,
        int amount,
        String iconItemVariantId,
        String iconAssetUrl) {
}
