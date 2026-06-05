package moe.takochan.webnei.fluid;

public record FluidSummary(
        String fluidVariantId,
        String fluidId,
        String modId,
        String modName,
        String registryName,
        String displayName,
        boolean gaseous,
        int density,
        int temperature,
        int viscosity,
        int luminosity,
        String nbtHash,
        String assetUrl) {
}
