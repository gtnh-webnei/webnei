package moe.takochan.webnei.extras;

import java.util.List;

public record FluidExtras(
        List<FluidContainerEntry> containers,
        List<FluidBlockEntry> blocks) {
}
