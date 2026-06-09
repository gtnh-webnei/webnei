package moe.takochan.webnei.model.dto;

import moe.takochan.webnei.model.dto.ItemRef;

public record FluidContainerEntry(
        ItemRef container,
        int amount) {
}
