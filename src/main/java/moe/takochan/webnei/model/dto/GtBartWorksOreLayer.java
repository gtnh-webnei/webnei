package moe.takochan.webnei.model.dto;

import moe.takochan.webnei.model.dto.ItemRef;

public record GtBartWorksOreLayer(
        String layer,
        int layerIndex,
        int oreMeta,
        boolean bartworksOre,
        ItemRef item) {
}
