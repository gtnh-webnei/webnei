package moe.takochan.webnei.model.dto;

import moe.takochan.webnei.model.dto.ItemRef;

public record GtBartWorksOreSummary(
        String entryId,
        String entryType,
        String dimension,
        String dimensionDisplayName,
        ItemRef resultItem) {
}
