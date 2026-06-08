package moe.takochan.webnei.fluid;

import java.util.List;

import moe.takochan.webnei.extras.FluidBlockEntry;
import moe.takochan.webnei.extras.FluidContainerEntry;

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
        String assetUrl,
        List<FluidUndergroundResource> undergroundResources,
        List<FluidContainerEntry> containers,
        List<FluidBlockEntry> blocks) {
}
