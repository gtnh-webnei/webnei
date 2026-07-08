import { http } from '@shared/api/client'
import type { PageResponse, SearchListParams } from '@shared/types'
import type { RecipeCategoryListEntry } from './types'

export async function listRecipeCategories(
  params: SearchListParams,
): Promise<PageResponse<RecipeCategoryListEntry>> {
  const response = await http.post<PageResponse<RecipeCategoryListEntry>>('/recipe/categories/list', {
    datasetId: params.datasetId,
    q: params.q?.trim() || undefined,
    page: params.page,
    size: params.size,
  })
  return response.data
}
