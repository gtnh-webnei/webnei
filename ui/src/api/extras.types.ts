export interface FluidContainerEntry {
  fluidVariantId: string;
  fluidId: string;
  fluidModId: string | null;
  fluidModName: string | null;
  fluidDisplayName: string | null;
  fluidGaseous: boolean | null;
  fluidTemperature: number | null;
  fluidAssetUrl: string | null;
  containerItemVariantId: string;
  containerDisplayName: string | null;
  containerAssetUrl: string | null;
  emptyContainerItemVariantId: string;
  emptyContainerDisplayName: string | null;
  emptyContainerAssetUrl: string | null;
  amount: number;
}

export interface AspectEntry {
  aspectId: string;
  name: string;
  description: string;
  primal: boolean;
  amount: number;
  iconItemVariantId: string;
  iconAssetUrl: string | null;
}

export interface FluidBlockEntry {
  blockItemVariantId: string;
  blockDisplayName: string | null;
  blockAssetUrl: string | null;
}

export interface ItemExtras {
  oreDictNames: string[];
  fluidContainers: FluidContainerEntry[];
  fluidContainersTotal: number;
  aspects: AspectEntry[];
}

export interface FluidExtras {
  containers: FluidContainerEntry[];
  blocks: FluidBlockEntry[];
}
