import type { IconAsset } from '@shared/types'

export interface RecipeCategoryListEntry {
  id: string
  categoryId: string
  displayName: string
  modId: string
  modName: string | null
  recipeCount: number
  icon: IconAsset | null
}
