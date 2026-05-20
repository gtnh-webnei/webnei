package moe.takochan.webnei.extras;

import java.util.List;

public record ItemExtras(
        List<String> oreDictNames,
        List<FluidContainerEntry> fluidContainers,
        long fluidContainersTotal,
        List<AspectEntry> aspects,
        long asInputRecipeCount,
        long asOutputRecipeCount) {
}
