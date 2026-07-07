export interface DatasetListEntry {
  datasetId: string
  packSlug: string
  packVersion: string
  variant: string
  language: string
  displayName: string
  schemaVersion: string
  exporterVersion: string
  createdAt: string
  minecraftVersion: string
}

export interface DatasetListResponse {
  defaultId: string | null
  items: DatasetListEntry[]
}

export interface ApiError {
  code: string
  message: string
}

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  total: number
}

export interface CatalogQueryParams {
  datasetId: string
  q?: string
  page: number
  size: number
}

export interface IconAsset {
  url: string
  width: number | null
  height: number | null
  metadataJson: string | null
}

export type CatalogKind = 'item' | 'fluid'

export interface CatalogEntryBase {
  id: string
  displayName: string
  modId: string
  modName: string | null
  registryName: string
  icon: IconAsset | null
}

export interface ItemListEntry extends CatalogEntryBase {
  itemVariantId: string
  itemId: string
  damage: number
  listIndex: number
  tooltipText: string
  chemicalExpression: string
}

export interface FluidListEntry extends CatalogEntryBase {
  fluidId: string
  chemicalExpression: string
  temperature: number
  gaseous: boolean
}

export type CatalogEntry = ItemListEntry | FluidListEntry
