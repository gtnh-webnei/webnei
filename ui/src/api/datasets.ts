import { http } from './client'
import type { DatasetListResponse } from './types'

export async function listDatasets(): Promise<DatasetListResponse> {
  const response = await http.get<DatasetListResponse>('/dataset/list')
  return response.data
}
