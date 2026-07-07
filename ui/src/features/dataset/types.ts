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
