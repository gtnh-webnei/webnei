package moe.takochan.webnei.gtore;

import moe.takochan.webnei.common.ItemRef;

public record GtSmallOreVariant(
        int variantIndex,
        ItemRef smallOreItem,
        ItemRef dustItem) {
}
