import { http } from './client'
import type { DatasetDefault, DatasetSummary } from './types'

export async function listDatasets(): Promise<DatasetSummary[]> {
  const response = await http.get<DatasetSummary[]>('/datasets')
  return response.data
}

export async function fetchDefaultDatasetId(): Promise<string | null> {
  const response = await http.get<DatasetDefault>('/datasets/default')
  return response.data.datasetId
}
