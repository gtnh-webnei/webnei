import type { FluidRef, ItemRef } from './entityRefs.types';

export interface ItemRelatedFluidEntry {
  fluid: FluidRef;
}

export interface FluidContainerEntry {
  container: ItemRef;
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
  item: ItemRef;
}

export interface ItemExtras {
  oreDictNames: string[];
  relatedFluids: ItemRelatedFluidEntry[];
  aspects: AspectEntry[];
}

export interface FluidExtras {
  containers: FluidContainerEntry[];
  blocks: FluidBlockEntry[];
}
