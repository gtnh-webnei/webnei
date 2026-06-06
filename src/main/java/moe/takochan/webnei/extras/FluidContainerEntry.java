package moe.takochan.webnei.extras;

public record FluidContainerEntry(
        String fluidVariantId,
        String fluidId,
        String fluidModId,
        String fluidModName,
        String fluidDisplayName,
        Boolean fluidGaseous,
        Integer fluidTemperature,
        String fluidAssetUrl,
        String containerItemVariantId,
        String containerDisplayName,
        String containerAssetUrl,
        String emptyContainerItemVariantId,
        String emptyContainerDisplayName,
        String emptyContainerAssetUrl,
        int amount) {
}
