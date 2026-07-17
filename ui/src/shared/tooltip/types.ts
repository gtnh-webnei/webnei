export type TooltipKeyState = 'none' | 'lshift' | 'lcontrol' | 'lshift_lcontrol'
export type TooltipType = 'standard' | 'key'
export type TooltipRole = 'title' | 'body' | 'context'

export interface TooltipSnapshot {
  tooltipType: TooltipType
  keyState: TooltipKeyState
  text: string
}

export interface ItemMcTooltipValue {
  tooltipSnapshots: readonly TooltipSnapshot[]
  modName: string | null
}

export interface FluidMcTooltipValue {
  displayName: string
  chemicalExpression: string | null
  temperature: number
  gaseous: boolean
  modName: string | null
}

export type McTooltipData =
  | { kind: 'item'; value: ItemMcTooltipValue }
  | { kind: 'fluid'; value: FluidMcTooltipValue }
  | { kind: 'custom' }

export interface McTooltipLine {
  text: string
  role: TooltipRole
  blank: boolean
  sectionGapAfter: boolean
  customRenderer: boolean
}
