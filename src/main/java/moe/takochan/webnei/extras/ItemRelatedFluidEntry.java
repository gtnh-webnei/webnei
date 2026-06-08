package moe.takochan.webnei.extras;

public record ItemRelatedFluidEntry(
        String fluidVariantId,
        String fluidId,
        String fluidModId,
        String fluidModName,
        String fluidDisplayName,
        Boolean fluidGaseous,
        Integer fluidTemperature,
        String fluidAssetUrl) {
}
