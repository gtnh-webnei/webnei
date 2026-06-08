package moe.takochan.webnei.gtore;

import java.util.List;

import moe.takochan.webnei.common.FluidRef;

public record GtUndergroundFluidDetail(
        FluidRef fluid,
        List<GtDimensionRef> dimensions,
        List<GtUndergroundFluidEntry> entries) {
}
