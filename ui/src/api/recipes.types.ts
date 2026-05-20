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

export interface GregTechRecipeInfo {
  voltageTier: string
  voltage: number
  amperage: number
  durationTicks: number
  requiresCleanroom: boolean
  additionalInfo: string
  specialItems: GregTechSpecialItem[]
}

export interface Recipe {
  recipeId: string
  categoryId: string
  categoryDisplayName: string
  uiKind: string
  uiTemplateId: string
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
  uiKind: string
  uiTemplateId: string
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
