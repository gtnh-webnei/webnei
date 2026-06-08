package moe.takochan.webnei.extras;

import java.util.List;

public record ItemExtras(
        List<String> oreDictNames,
        List<ItemRelatedFluidEntry> relatedFluids,
        List<AspectEntry> aspects) {
}
