package moe.takochan.webnei.gtore;

public record GtUndergroundFluidEntry(
        String dimension,
        GtDimensionRef dimensionDisplay,
        int chance,
        int minAmount,
        int maxAmount) {
}
