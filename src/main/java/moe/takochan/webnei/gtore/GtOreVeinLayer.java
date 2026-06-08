package moe.takochan.webnei.gtore;

import java.util.List;

import moe.takochan.webnei.common.ItemRef;

public record GtOreVeinLayer(
        String layer,
        String materialName,
        int oreMeta,
        ItemRef item,
        List<ItemRef> variants) {
}
