package moe.takochan.webnei.catalog.api;

public record FluidListEntry(
        String id,
        String fluidId,
        String displayName,
        String modId,
        String modName,
        String registryName,
        String chemicalExpression,
        int temperature,
        boolean gaseous,
        IconAsset icon) {}
