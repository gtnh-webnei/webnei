package moe.takochan.webnei.recipe.dto;

import moe.takochan.webnei.common.FluidRef;
import moe.takochan.webnei.common.ItemRef;

public record LookupTargetHeaderDto(
        String targetType,
        ItemRef item,
        FluidRef fluid) {
}
