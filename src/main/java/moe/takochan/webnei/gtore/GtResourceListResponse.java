package moe.takochan.webnei.gtore;

import java.util.List;

public record GtResourceListResponse<T>(
        List<T> items,
        List<GtDimensionRef> dimensions,
        List<String> types) {
}
