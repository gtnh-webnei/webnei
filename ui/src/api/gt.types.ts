import type { FluidRef, ItemRef } from './entityRefs.types';

export interface GtDimensionRef {
  dimension: string;
  fullName: string;
  displayName: string;
  displayAbbr: string;
  iconItemVariantId: string;
  iconAssetUrl: string | null;
  sortOrder: number;
}

export interface GtOreVeinSummary {
  veinName: string;
  displayName: string;
  primaryItem: ItemRef | null;
  dimensions: GtDimensionRef[];
}

export interface GtOreVeinLayer {
  layer: string;
  materialName: string;
  oreMeta: number;
  item: ItemRef;
  variants: ItemRef[];
}

export interface GtOreVeinDetail {
  displayName: string;
  weight: number;
  size: number;
  density: number;
  heightMin: number;
  heightMax: number;
  layers: GtOreVeinLayer[];
  dimensions: GtDimensionRef[];
}

export interface GtSmallOreSummary {
  oreGenName: string;
  smallOreItem: ItemRef;
  dimensions: GtDimensionRef[];
}

export interface GtSmallOreVariant {
  variantIndex: number;
  smallOreItem: ItemRef;
  dustItem: ItemRef;
}

export interface GtSmallOreDetail {
  amountPerChunk: number;
  heightMin: number;
  heightMax: number;
  smallOreItem: ItemRef;
  dustItem: ItemRef;
  drops: ItemRef[];
  dimensions: GtDimensionRef[];
}

export interface GtUndergroundFluidEntry {
  dimension: string;
  dimensionDisplay: GtDimensionRef;
  chance: number;
  minAmount: number;
  maxAmount: number;
}

export interface GtUndergroundFluidSummary {
  fluidId: string;
  fluid: FluidRef;
  dimensions: GtDimensionRef[];
  entries: GtUndergroundFluidEntry[];
}

export interface GtUndergroundFluidDetail {
  fluid: FluidRef;
  dimensions: GtDimensionRef[];
  entries: GtUndergroundFluidEntry[];
}

export interface GtBartWorksOreSummary {
  entryId: string;
  entryType: string;
  dimension: string;
  dimensionDisplayName: string;
  resultItem: ItemRef;
}

export interface GtBartWorksOreLayer {
  layer: string;
  layerIndex: number;
  oreMeta: number;
  bartworksOre: boolean;
  item: ItemRef;
}

export interface GtBartWorksOreDetail {
  entryType: string;
  dimensionDisplayName: string;
  resultItem: ItemRef;
  heightMin: number;
  heightMax: number;
  weight: number;
  density: number;
  size: number;
  amountPerChunk: number;
  layers: GtBartWorksOreLayer[];
}

export interface GtResourceListResponse<T> {
  items: T[];
  dimensions: GtDimensionRef[];
  types: string[];
}

export type GtSection = 'ore-veins' | 'small-ores' | 'underground-fluids' | 'bartworks-ores';
