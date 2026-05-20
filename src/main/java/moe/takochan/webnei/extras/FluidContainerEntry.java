package moe.takochan.webnei.extras;

public record FluidContainerEntry(
        String fluidVariantId,
        String fluidDisplayName,
        String containerItemVariantId,
        String containerDisplayName,
        String containerAssetUrl,
        String emptyContainerItemVariantId,
        String emptyContainerDisplayName,
        String emptyContainerAssetUrl,
        int amount) {
}
