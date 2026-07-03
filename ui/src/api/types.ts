export interface DatasetSummary {
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

export interface DatasetDefault {
  datasetId: string | null
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
