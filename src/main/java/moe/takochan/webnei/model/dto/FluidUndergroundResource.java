package moe.takochan.webnei.model.dto;

import moe.takochan.webnei.model.dto.GtDimensionRef;

public record FluidUndergroundResource(
        String fluidId,
        String dimension,
        GtDimensionRef dimensionDisplay,
        int chance,
        int minAmount,
        int maxAmount) {
}
