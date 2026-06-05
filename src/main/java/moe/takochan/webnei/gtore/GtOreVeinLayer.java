package moe.takochan.webnei.gtore;

import java.util.List;

public record GtOreVeinLayer(
        String layer,
        String materialName,
        int oreMeta,
        GtItemRef item,
        List<GtItemRef> variants) {
}
