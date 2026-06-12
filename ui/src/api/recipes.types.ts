export interface RecipeSlotCandidate {
  itemVariantId: string | null;
  fluidVariantId: string | null;
  amount: number;
  displayName: string | null;
  modId: string | null;
  modName: string | null;
  fluidGaseous: boolean | null;
  fluidTemperature: number | null;
  tooltipText: string | null;
  assetUrl: string | null;
}

export interface RecipeSlot {
  role: string;
  slotIndex: number;
  itemVariantId: string | null;
  fluidVariantId: string | null;
  amount: number;
  probability: number;
  groupId: string | null;
  displayName: string | null;
  modId: string | null;
  modName: string | null;
  fluidGaseous: boolean | null;
  fluidTemperature: number | null;
  tooltipText: string | null;
  assetUrl: string | null;
  candidates: RecipeSlotCandidate[];
  placement?: string | null;
  slotGroupKey?: string | null;
  slotGroupOrder?: number;
  metadata: Record<string, MetadataValue>;
}

export type GregTechRecipeKind = 'PROCESSING' | 'FUEL';

export interface MaterialRef {
  $ref: 'Materials';
  name: string;
}

export interface SolarFactoryWaferData {
  tierRequired: number;
  minimumWaferTier: number;
  minimumWaferCount: number;
}

export interface QuantumComputerData {
  maxHeat: number;
  subZero: boolean;
  computation: number;
  coolConstant: number;
  heatConstant: number;
}

export type MetadataJsonValue =
  | MaterialRef
  | SolarFactoryWaferData
  | QuantumComputerData
  | Record<string, unknown>
  | unknown[];

export interface MetadataValue {
  valueType: 'int' | 'long' | 'float' | 'double' | 'bool' | 'str' | 'enum' | 'json' | string;
  valueText: string | null;
  valueJson: MetadataJsonValue | null;
}

export interface Recipe {
  recipeId: string;
  categoryId: string;
  categoryDisplayName: string;
  sourcePlugin: string;
  sourceModName: string;
  sourceRef: string;
  description: string;
  slots: RecipeSlot[];
  metadata: Record<string, MetadataValue>;
}

export interface RecipeCategory {
  categoryId: string;
  handlerId: string;
  displayName: string;
  shapeless: boolean;
  iconAssetUrl: string | null;
  itemInputWidth: number;
  itemInputHeight: number;
  fluidInputWidth: number;
  fluidInputHeight: number;
  itemOutputWidth: number;
  itemOutputHeight: number;
  fluidOutputWidth: number;
  fluidOutputHeight: number;
  recipeCount: number;
  applicableItemCount: number;
  modName: string;
  handlerClass: string;
}

export type LookupKind = 'recipe' | 'usage';

export type LookupTargetType = 'item' | 'fluid';

export interface LookupTargetHeader {
  targetType: LookupTargetType;
  targetId: string;
  displayName: string | null;
  assetUrl: string | null;
  modId: string | null;
  modName: string | null;
  gaseous: boolean | null;
  recipeCount: number;
  usageCount: number;
}

export interface CategoryBreakdown {
  categoryId: string;
  displayName: string;
  iconAssetUrl: string | null;
  count: number;
}

export interface HandlerBreakdown {
  handlerId: string;
  displayName: string;
  iconAssetUrl: string | null;
  count: number;
  categories: CategoryBreakdown[];
}

export interface CategoryApplicableItem {
  itemVariantId: string;
  displayName: string | null;
  assetUrl: string | null;
  role: string;
  displayOrder: number;
}

export interface CategoryVoltageTier {
  tier: string;
  recipeCount: number;
}
