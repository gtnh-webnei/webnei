package moe.takochan.webnei.fluid;

import moe.takochan.webnei.gtore.GtDimensionRef;

public record FluidUndergroundResource(
        String fluidId,
        String dimension,
        GtDimensionRef dimensionDisplay,
        int chance,
        int minAmount,
        int maxAmount) {
}
