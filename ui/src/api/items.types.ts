import type { AspectEntry, ItemRelatedFluidEntry } from './extras.types';
import type { GtDimensionRef } from './gt.types';

export interface NeiPanelEntry {
  itemVariantId: string;
  itemId: string;
  modId: string;
  modName: string;
  registryName: string;
  damage: number;
  displayName: string;
  assetUrl: string | null;
  panelIndex: number;
}

export interface ItemWorldGenerationRef {
  section: string;
  key: string;
  title: string;
  type: string;
  dimensions: GtDimensionRef[];
  statText: string;
}

export interface ItemDetail {
  itemId: string;
  modId: string;
  modName: string;
  registryName: string;
  unlocalizedName: string;
  maxStackSize: number;
  maxDamage: number;
  damage: number;
  nbtText: string | null;
  displayName: string;
  tooltipText: string;
  chemicalExpression: string | null;
  worldGeneration: ItemWorldGenerationRef[];
  oreDictNames: string[];
  relatedFluids: ItemRelatedFluidEntry[];
  aspects: AspectEntry[];
}

export interface ItemListParams {
  q?: string;
  modId?: string;
  page?: number;
  size?: number;
}
