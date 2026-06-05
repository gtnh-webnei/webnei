package moe.takochan.webnei.fluid;

public record FluidDetail(
        String fluidVariantId,
        String fluidId,
        String modId,
        String registryName,
        String unlocalizedName,
        String displayName,
        boolean gaseous,
        int density,
        int temperature,
        int viscosity,
        int luminosity,
        int runtimeFluidId,
        String nbtHash,
        String nbtText,
        String chemicalExpression,
        String assetUrl) {
}
