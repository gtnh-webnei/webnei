import { http } from './client'
import type {
  QuestDetail,
  QuestLineDetail,
  QuestLineSummary,
} from './quests.types'

export async function listQuestLines(datasetId: string): Promise<QuestLineSummary[]> {
  const { data } = await http.get<QuestLineSummary[]>(
    `/datasets/${encodeURIComponent(datasetId)}/quest-lines`,
  )
  return data
}

export async function getQuestLineDetail(
  datasetId: string,
  lineId: string,
): Promise<QuestLineDetail> {
  const { data } = await http.get<QuestLineDetail>(
    `/datasets/${encodeURIComponent(datasetId)}/quest-lines/${encodeURIComponent(lineId)}`,
  )
  return data
}

export async function getQuestDetail(
  datasetId: string,
  questId: string,
): Promise<QuestDetail> {
  const { data } = await http.get<QuestDetail>(
    `/datasets/${encodeURIComponent(datasetId)}/quests/${encodeURIComponent(questId)}`,
  )
  return data
}
