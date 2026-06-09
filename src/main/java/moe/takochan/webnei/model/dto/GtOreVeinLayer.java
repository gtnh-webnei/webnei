package moe.takochan.webnei.model.dto;

import java.util.List;

import moe.takochan.webnei.model.dto.ItemRef;

public record GtOreVeinLayer(
        String layer,
        String materialName,
        int oreMeta,
        ItemRef item,
        List<ItemRef> variants) {
}
