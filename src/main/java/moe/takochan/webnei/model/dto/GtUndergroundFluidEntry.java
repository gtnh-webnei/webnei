package moe.takochan.webnei.model.dto;

public record GtUndergroundFluidEntry(
        String dimension,
        GtDimensionRef dimensionDisplay,
        int chance,
        int minAmount,
        int maxAmount) {
}
