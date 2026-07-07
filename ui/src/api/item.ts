import { http } from './client'
import type { CatalogQueryParams, ItemListEntry, PageResponse } from './types'

export async function listItems(params: CatalogQueryParams): Promise<PageResponse<ItemListEntry>> {
  const response = await http.post<PageResponse<ItemListEntry>>('/item/list', {
    datasetId: params.datasetId,
    q: params.q?.trim() || undefined,
    page: params.page,
    size: params.size,
  })
  return response.data
}
