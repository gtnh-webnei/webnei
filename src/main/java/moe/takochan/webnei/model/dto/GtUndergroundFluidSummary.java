package moe.takochan.webnei.model.dto;

import java.util.List;

import moe.takochan.webnei.model.dto.FluidRef;

public record GtUndergroundFluidSummary(
        String fluidId,
        FluidRef fluid,
        List<GtDimensionRef> dimensions,
        List<GtUndergroundFluidEntry> entries) {
}
