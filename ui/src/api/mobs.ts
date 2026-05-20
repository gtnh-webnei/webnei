import { http } from './client'
import type { PageResponse } from './types'
import type { MobDetail, MobListParams, MobSummary } from './mobs.types'

export async function listMobs(
  datasetId: string,
  params: MobListParams,
): Promise<PageResponse<MobSummary>> {
  const { data } = await http.get<PageResponse<MobSummary>>(
    `/datasets/${encodeURIComponent(datasetId)}/mobs`,
    { params },
  )
  return data
}

export async function listMobMods(datasetId: string): Promise<string[]> {
  const { data } = await http.get<string[]>(
    `/datasets/${encodeURIComponent(datasetId)}/mobs/mods`,
  )
  return data
}

export async function getMobDetail(
  datasetId: string,
  mobVariantId: string,
): Promise<MobDetail> {
  const { data } = await http.get<MobDetail>(
    `/datasets/${encodeURIComponent(datasetId)}/mobs/${encodeURIComponent(mobVariantId)}`,
  )
  return data
}
