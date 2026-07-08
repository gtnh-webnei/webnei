import { http } from '@shared/api/client'
import type { ModListEntry } from './types'

export async function listMods(datasetId: string, q: string): Promise<ModListEntry[]> {
  const response = await http.post<ModListEntry[]>('/mod/list', {
    datasetId,
    q: q.trim() || undefined,
  })
  return response.data
}
