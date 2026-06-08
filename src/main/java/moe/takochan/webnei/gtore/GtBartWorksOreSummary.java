package moe.takochan.webnei.gtore;

import moe.takochan.webnei.common.ItemRef;

public record GtBartWorksOreSummary(
        String entryId,
        String entryType,
        String dimension,
        String dimensionDisplayName,
        ItemRef resultItem) {
}
