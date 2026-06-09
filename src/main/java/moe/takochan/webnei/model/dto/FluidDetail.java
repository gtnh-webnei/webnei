package moe.takochan.webnei.model.dto;

import java.util.List;

import moe.takochan.webnei.model.dto.FluidBlockEntry;
import moe.takochan.webnei.model.dto.FluidContainerEntry;

public record FluidDetail(
        String fluidId,
        String modId,
        String modName,
        String registryName,
        String unlocalizedName,
        String displayName,
        boolean gaseous,
        int density,
        int temperature,
        int viscosity,
        int luminosity,
        int runtimeFluidId,
        String nbtText,
        String chemicalExpression,
        List<FluidUndergroundResource> undergroundResources,
        List<FluidContainerEntry> containers,
        List<FluidBlockEntry> blocks) {
}
