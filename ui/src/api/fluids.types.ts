import type { GtDimensionRef } from './gt.types';

export interface FluidSummary {
  fluidVariantId: string;
  fluidId: string;
  modId: string;
  modName: string;
  registryName: string;
  displayName: string;
  gaseous: boolean;
  density: number;
  temperature: number;
  viscosity: number;
  luminosity: number;
  nbtHash: string | null;
  assetUrl: string | null;
}

export interface FluidUndergroundResource {
  fluidId: string;
  dimension: string;
  dimensionDisplay: GtDimensionRef;
  chance: number;
  minAmount: number;
  maxAmount: number;
}

export interface FluidDetail extends FluidSummary {
  unlocalizedName: string;
  runtimeFluidId: number;
  chemicalExpression: string | null;
  nbtText: string | null;
  undergroundResources: FluidUndergroundResource[];
}

export interface FluidListParams {
  q?: string;
  modId?: string;
  page?: number;
  size?: number;
}
