package moe.takochan.webnei.gtore;

import moe.takochan.webnei.common.ItemRef;

public record GtBartWorksOreLayer(
        String layer,
        int layerIndex,
        int oreMeta,
        boolean bartworksOre,
        ItemRef item) {
}
