export interface MobSummary {
  mobVariantId: string
  mobId: string
  modId: string
  entityName: string
  displayName: string
  width: number
  height: number
  maxHealth: number
  armor: number
  immuneToFire: boolean
  leashable: boolean
  assetUrl: string | null
}

export interface MobDropRow {
  dropType: string
  listIndex: number
  itemVariantId: string
  displayName: string | null
  assetUrl: string | null
  stackSize: number
  probability: number
  lootable: boolean
  playerOnly: boolean
}

export interface MobInfo {
  allowedInPeaceful: boolean
  soulVialUsable: boolean
  allowedInfernal: boolean
  alwaysInfernal: boolean
}

export interface MobDetail {
  summary: MobSummary
  info: MobInfo
  drops: MobDropRow[]
}

export interface MobListParams {
  q?: string
  modId?: string
  page?: number
  size?: number
}
