import type { GtDimensionRef } from './gt.types';

export interface NeiPanelEntry {
  itemVariantId: string;
  itemId: string;
  modId: string;
  modName: string;
  registryName: string;
  damage: number;
  nbtHash: string;
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
  itemVariantId: string;
  itemId: string;
  modId: string;
  modName: string;
  registryName: string;
  unlocalizedName: string;
  maxStackSize: number;
  maxDamage: number;
  damage: number;
  nbtHash: string;
  nbtText: string;
  displayName: string;
  tooltipText: string;
  chemicalExpression: string;
  assetUrl: string | null;
  assetWidth: number | null;
  assetHeight: number | null;
  worldGeneration: ItemWorldGenerationRef[];
}

export interface ItemListParams {
  q?: string;
  modId?: string;
  page?: number;
  size?: number;
}
