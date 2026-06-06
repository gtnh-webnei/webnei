package moe.takochan.webnei.gtore;

import java.util.List;

public record GtUndergroundFluidDetail(
        GtFluidRef fluid,
        List<GtDimensionRef> dimensions,
        List<GtUndergroundFluidEntry> entries) {
}
