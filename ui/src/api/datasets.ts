import { http } from './client'
import type { DatasetDetail, DatasetSummary, ModSummary, PageResponse } from './types'

export async function listDatasets(): Promise<DatasetSummary[]> {
  const { data } = await http.get<DatasetSummary[]>('/datasets')
  return data
}

export async function getDatasetDetail(datasetId: string): Promise<DatasetDetail> {
  const { data } = await http.get<DatasetDetail>(`/datasets/${encodeURIComponent(datasetId)}`)
  return data
}

export async function listModsPage(
  datasetId: string,
  params: {
    q?: string
    sort?: string
    desc?: boolean
    page: number
    size: number
  },
): Promise<PageResponse<ModSummary>> {
  const qs: Record<string, string | number | boolean> = {
    page: params.page,
    size: params.size,
  }
  if (params.q) qs.q = params.q
  if (params.sort) qs.sort = params.sort
  if (params.desc) qs.desc = true
  const { data } = await http.get<PageResponse<ModSummary>>(
    `/datasets/${encodeURIComponent(datasetId)}/mods/page`,
    { params: qs },
  )
  return data
}
