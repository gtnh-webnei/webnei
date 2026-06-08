export interface ItemRef {
  itemVariantId: string;
  displayName: string | null;
  tooltipText: string | null;
  assetUrl: string | null;
}

export interface FluidRef {
  fluidVariantId: string;
  fluidId: string | null;
  modId: string | null;
  modName: string | null;
  displayName: string | null;
  gaseous: boolean | null;
  temperature: number | null;
  assetUrl: string | null;
}
