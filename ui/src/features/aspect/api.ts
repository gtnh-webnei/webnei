import { http } from '@shared/api/client'
import type { PageResponse } from '@shared/types'
import type { AspectDetail, AspectItemEntry, AspectListEntry } from './types'

const aspectListRequests = new Map<string, Promise<AspectListEntry[]>>()

export function listAspects(datasetId: string): Promise<AspectListEntry[]> {
  const cached = aspectListRequests.get(datasetId)
  if (cached) return cached

  const request = http
    .post<AspectListEntry[]>('/aspect/list', { datasetId })
    .then((response) => response.data)
    .catch((error: unknown) => {
      aspectListRequests.delete(datasetId)
      throw error
    })
  aspectListRequests.set(datasetId, request)
  return request
}

export async function getAspectDetail(datasetId: string, aspectId: string): Promise<AspectDetail> {
  const response = await http.get<AspectDetail>(`/aspect/${encodeURIComponent(aspectId)}`, {
    params: { datasetId },
  })
  return response.data
}

export interface AspectItemListParams {
  datasetId: string
  aspectId: string
  q?: string
  page: number
  size: number
}

export async function listAspectItems(
  params: AspectItemListParams,
): Promise<PageResponse<AspectItemEntry>> {
  const response = await http.post<PageResponse<AspectItemEntry>>('/aspect/items/list', {
    datasetId: params.datasetId,
    aspectId: params.aspectId,
    q: params.q?.trim() || undefined,
    page: params.page,
    size: params.size,
  })
  return response.data
}
