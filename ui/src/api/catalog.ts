import { http } from './client'
import type { FluidListEntry, ItemListEntry, PageResponse } from './types'

export interface CatalogQueryParams {
  datasetId: string
  q?: string
  page: number
  size: number
}

function catalogParams(params: CatalogQueryParams) {
  return {
    q: params.q?.trim() || undefined,
    page: params.page,
    size: params.size,
  }
}

export async function listItems(params: CatalogQueryParams): Promise<PageResponse<ItemListEntry>> {
  const response = await http.get<PageResponse<ItemListEntry>>(
    `/datasets/${encodeURIComponent(params.datasetId)}/items`,
    { params: catalogParams(params) },
  )
  return response.data
}

export async function listFluids(params: CatalogQueryParams): Promise<PageResponse<FluidListEntry>> {
  const response = await http.get<PageResponse<FluidListEntry>>(
    `/datasets/${encodeURIComponent(params.datasetId)}/fluids`,
    { params: catalogParams(params) },
  )
  return response.data
}
