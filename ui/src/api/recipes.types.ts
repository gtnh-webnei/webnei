export interface SlotLayout {
  role: string
  slotIndex: number
  x: number
  y: number
  width: number
  height: number
  slotStyle: string
}

export interface RecipeSlotCandidate {
  itemVariantId: string | null
  fluidVariantId: string | null
  amount: number
  displayName: string | null
  modId: string | null
  assetUrl: string | null
}

export interface RecipeSlot {
  role: string
  slotIndex: number
  itemVariantId: string | null
  fluidVariantId: string | null
  amount: number
  probability: number
  groupId: string | null
  displayName: string | null
  modId: string | null
  assetUrl: string | null
  candidates: RecipeSlotCandidate[]
}

export interface GregTechSpecialItem {
  itemVariantId: string
  displayName: string | null
  modId: string | null
  assetUrl: string | null
}

export type GregTechRecipeKind = 'PROCESSING' | 'FUEL'

export interface MaterialRef {
  $ref: 'Materials'
  name: string
}

export interface SolarFactoryWaferData {
  tierRequired: number
  minimumWaferTier: number
  minimumWaferCount: number
}

export interface QuantumComputerData {
  maxHeat: number
  subZero: boolean
  computation: number
  coolConstant: number
  heatConstant: number
}

export type MetadataJsonValue =
  | MaterialRef
  | SolarFactoryWaferData
  | QuantumComputerData
  | Record<string, unknown>
  | unknown[]

export interface MetadataValue {
  valueType: 'int' | 'long' | 'float' | 'double' | 'bool' | 'str' | 'enum' | 'json' | string
  valueText: string | null
  valueJson: MetadataJsonValue | null
}

export interface GregTechRecipeInfo {
  recipeKind: GregTechRecipeKind
  visibleInNei: boolean
  voltageTier: string | null
  voltage: number | null
  amperage: number | null
  durationTicks: number
  specialValue: number | null
  specialItems: GregTechSpecialItem[]
  metadata: Record<string, MetadataValue>
}

export interface Recipe {
  recipeId: string
  categoryId: string
  categoryDisplayName: string
  sourcePlugin: string
  sourceRef: string
  description: string
  slots: RecipeSlot[]
  layout: SlotLayout[]
  canvasWidth: number | null
  canvasHeight: number | null
  backgroundAssetUrl: string | null
  gregtech: GregTechRecipeInfo | null
}

export interface RecipeCategory {
  categoryId: string
  plugin: string
  handlerId: string
  displayName: string
  shapeless: boolean
  iconItemVariantId: string
  iconDisplayName: string | null
  iconAssetUrl: string | null
  iconInfo: string
  itemInputWidth: number
  itemInputHeight: number
  fluidInputWidth: number
  fluidInputHeight: number
  itemOutputWidth: number
  itemOutputHeight: number
  fluidOutputWidth: number
  fluidOutputHeight: number
  supportsRecipeLookup: boolean
  supportsUsageLookup: boolean
  displayOrder: number
  canvasWidth: number | null
  canvasHeight: number | null
  backgroundAssetUrl: string | null
  recipeCount: number
  machineCount: number
  // NEI HandlerInfo
  modId: string
  modName: string
  handlerClass: string
  handlerCanvasWidth: number
  handlerCanvasHeight: number
  handlerYShift: number
  handlerMultipleWidgetsAllowed: boolean
  iconImageResource: string
  iconImageX: number
  iconImageY: number
  iconImageWidth: number
  iconImageHeight: number
  iconImageTextureWidth: number
  iconImageTextureHeight: number
}

export type LookupKind = 'recipe' | 'usage'

export interface CategoryBreakdown {
  categoryId: string
  displayName: string
  iconAssetUrl: string | null
  count: number
}

export interface HandlerBreakdown {
  handlerId: string
  displayName: string
  iconAssetUrl: string | null
  count: number
  categories: CategoryBreakdown[]
}

export interface CategoryMachine {
  itemVariantId: string
  displayName: string | null
  assetUrl: string | null
  role: string
  displayOrder: number
}

export interface CategoryVoltageTier {
  tier: string
  recipeCount: number
}
