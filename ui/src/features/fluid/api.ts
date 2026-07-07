import { http } from '@shared/api/client'
import type { PageResponse, SearchListParams } from '@shared/types'
import type { FluidListEntry } from './types'

export async function listFluids(params: SearchListParams): Promise<PageResponse<FluidListEntry>> {
  const response = await http.post<PageResponse<FluidListEntry>>('/fluid/list', {
    datasetId: params.datasetId,
    q: params.q?.trim() || undefined,
    page: params.page,
    size: params.size,
  })
  return response.data
}
