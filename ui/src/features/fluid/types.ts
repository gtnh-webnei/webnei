import type { EntryBase } from '@shared/types'

export interface FluidListEntry extends EntryBase {
  fluidId: string
  chemicalExpression: string
  temperature: number
  gaseous: boolean
}
