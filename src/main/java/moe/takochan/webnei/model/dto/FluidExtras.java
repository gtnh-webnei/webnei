package moe.takochan.webnei.model.dto;

import java.util.List;

public record FluidExtras(
        List<FluidContainerEntry> containers,
        List<FluidBlockEntry> blocks) {
}
