import { http } from '@shared/api/client'
import type { IconAsset, PageResponse } from '@shared/types'
import type { AspectBrief, AspectDetail, AspectItemEntry, AspectListEntry } from './types'

// 后端 brief 历史上可能序列化为 aspectId；统一收敛到 id，页面只认 id。
interface RawAspectBrief {
  id?: string
  aspectId?: string
  displayName: string
  color: number
  icon: IconAsset | null
}

interface RawAspectListEntry extends RawAspectBrief {
  description: string
  primal: boolean
  components?: RawAspectBrief[]
  associatedItemCount: number
}

interface RawAspectDetail extends RawAspectBrief {
  description: string
  primal: boolean
  components?: RawAspectBrief[]
  usedBy?: RawAspectBrief[]
}

function briefId(raw: RawAspectBrief): string {
  const id = raw.id ?? raw.aspectId
  if (!id) {
    throw new Error('Aspect brief is missing id')
  }
  return id
}

function normalizeBrief(raw: RawAspectBrief): AspectBrief {
  return {
    id: briefId(raw),
    displayName: raw.displayName,
    color: raw.color,
    icon: raw.icon,
  }
}

function normalizeListEntry(raw: RawAspectListEntry): AspectListEntry {
  return {
    ...normalizeBrief(raw),
    description: raw.description,
    primal: raw.primal,
    components: (raw.components ?? []).map(normalizeBrief),
    associatedItemCount: raw.associatedItemCount,
  }
}

function normalizeDetail(raw: RawAspectDetail): AspectDetail {
  return {
    ...normalizeBrief(raw),
    description: raw.description,
    primal: raw.primal,
    components: (raw.components ?? []).map(normalizeBrief),
    usedBy: (raw.usedBy ?? []).map(normalizeBrief),
  }
}

const aspectListRequests = new Map<string, Promise<AspectListEntry[]>>()

export function listAspects(datasetId: string): Promise<AspectListEntry[]> {
  const cached = aspectListRequests.get(datasetId)
  if (cached) return cached

  const request = http
    .post<RawAspectListEntry[]>('/aspect/list', { datasetId })
    .then((response) => response.data.map(normalizeListEntry))
    .catch((error: unknown) => {
      aspectListRequests.delete(datasetId)
      throw error
    })
  aspectListRequests.set(datasetId, request)
  return request
}

export async function getAspectDetail(datasetId: string, aspectId: string): Promise<AspectDetail> {
  const response = await http.get<RawAspectDetail>(`/aspect/${encodeURIComponent(aspectId)}`, {
    params: { datasetId },
  })
  return normalizeDetail(response.data)
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
