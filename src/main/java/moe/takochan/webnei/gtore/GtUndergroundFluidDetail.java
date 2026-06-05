package moe.takochan.webnei.gtore;

public record GtUndergroundFluidDetail(
        String fluidId,
        String dimension,
        GtFluidRef fluid,
        GtDimensionRef dimensionDisplay,
        int chance,
        int minAmount,
        int maxAmount) {
}
