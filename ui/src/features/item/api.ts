import { http } from '@shared/api/client'
import type { PageResponse, SearchListParams } from '@shared/types'
import type { ItemListEntry } from './types'

export async function listItems(params: SearchListParams): Promise<PageResponse<ItemListEntry>> {
  const response = await http.post<PageResponse<ItemListEntry>>('/item/list', {
    datasetId: params.datasetId,
    q: params.q?.trim() || undefined,
    page: params.page,
    size: params.size,
  })
  return response.data
}
