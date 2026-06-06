package moe.takochan.webnei.item;

import java.util.List;

import moe.takochan.webnei.gtore.GtDimensionRef;

public record ItemWorldGenerationRef(
        String section,
        String key,
        String title,
        String type,
        List<GtDimensionRef> dimensions,
        String statText) {
}
