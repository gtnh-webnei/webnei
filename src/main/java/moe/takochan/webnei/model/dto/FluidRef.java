package moe.takochan.webnei.model.dto;

public record FluidRef(
        String fluidVariantId,
        String fluidId,
        String modId,
        String modName,
        String displayName,
        Boolean gaseous,
        Integer temperature,
        String assetUrl) {
}
