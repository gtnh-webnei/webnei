package moe.takochan.webnei.model.dto;

import moe.takochan.webnei.model.dto.ItemRef;

public record GtSmallOreVariant(
        int variantIndex,
        ItemRef smallOreItem,
        ItemRef dustItem) {
}
