import type { EntryBase, IconAsset } from '@shared/types'

export interface AspectBrief {
  id: string
  displayName: string
  color: number
  icon: IconAsset | null
}

export interface AspectListEntry extends AspectBrief {
  id: string
  description: string
  primal: boolean
  components: AspectBrief[]
  associatedItemCount: number
}

export interface AspectDetail extends AspectBrief {
  id: string
  description: string
  primal: boolean
  components: AspectBrief[]
  usedBy: AspectBrief[]
}

export interface AspectItemEntry extends EntryBase {
  amount: number
}
