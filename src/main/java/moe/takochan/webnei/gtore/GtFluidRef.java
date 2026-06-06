package moe.takochan.webnei.gtore;

public record GtFluidRef(
        String fluidVariantId,
        String fluidId,
        String modId,
        String modName,
        String displayName,
        Boolean gaseous,
        Integer temperature,
        String assetUrl) {
}
