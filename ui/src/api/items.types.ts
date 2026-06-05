export interface NeiPanelEntry {
  itemVariantId: string;
  itemId: string;
  modId: string;
  registryName: string;
  damage: number;
  nbtHash: string;
  displayName: string;
  assetUrl: string | null;
  panelIndex: number;
}

export interface ItemDetail {
  itemVariantId: string;
  itemId: string;
  modId: string;
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
}

export interface ItemListParams {
  q?: string;
  modId?: string;
  page?: number;
  size?: number;
}
