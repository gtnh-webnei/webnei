package moe.takochan.webnei.gtore;

import java.util.List;

public record GtUndergroundFluidSummary(
        String fluidId,
        GtFluidRef fluid,
        List<GtDimensionRef> dimensions,
        List<GtUndergroundFluidEntry> entries) {
}
