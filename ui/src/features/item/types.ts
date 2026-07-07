import type { EntryBase } from '@shared/types'

export interface ItemListEntry extends EntryBase {
  itemVariantId: string
  itemId: string
  damage: number
  listIndex: number
  tooltipText: string
  chemicalExpression: string
}
