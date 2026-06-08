package moe.takochan.webnei.extras;

import moe.takochan.webnei.common.ItemRef;

public record FluidContainerEntry(
        ItemRef container,
        int amount) {
}
