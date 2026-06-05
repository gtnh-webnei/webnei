export interface FluidSummary {
  fluidVariantId: string;
  fluidId: string;
  modId: string;
  registryName: string;
  displayName: string;
  gaseous: boolean;
  density: number;
  temperature: number;
  viscosity: number;
  luminosity: number;
  nbtHash: string;
  assetUrl: string | null;
}

export interface FluidDetail extends FluidSummary {
  unlocalizedName: string;
  runtimeFluidId: number;
  chemicalExpression: string;
  nbtText: string;
}

export interface FluidListParams {
  q?: string;
  modId?: string;
  page?: number;
  size?: number;
}
