package moe.takochan.webnei.model.dto;

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
        String assetUrl) {
}
