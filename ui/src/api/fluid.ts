import { http } from './client'
import type { CatalogQueryParams, FluidListEntry, PageResponse } from './types'

export async function listFluids(params: CatalogQueryParams): Promise<PageResponse<FluidListEntry>> {
  const response = await http.post<PageResponse<FluidListEntry>>('/fluid/list', {
    datasetId: params.datasetId,
    q: params.q?.trim() || undefined,
    page: params.page,
    size: params.size,
  })
  return response.data
}
