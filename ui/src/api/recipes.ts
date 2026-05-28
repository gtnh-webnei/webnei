import { http } from './client'
import type { PageResponse } from './types'
import type {
  CategoryMachine,
  CategoryVoltageTier,
  HandlerBreakdown,
  LookupKind,
  Recipe,
  RecipeCategory,
} from './recipes.types'

export async function listRecipeCategories(datasetId: string): Promise<RecipeCategory[]> {
  const { data } = await http.get<RecipeCategory[]>(
    `/datasets/${encodeURIComponent(datasetId)}/recipe-categories`,
  )
  return data
}

export async function listRecipeCategoriesPage(
  datasetId: string,
  params: {
    q?: string
    plugin?: string
    hideEmpty?: boolean
    page: number
    size: number
  },
): Promise<PageResponse<RecipeCategory>> {
  const qs: Record<string, string | number | boolean> = {
    page: params.page,
    size: params.size,
  }
  if (params.q) qs.q = params.q
  if (params.plugin) qs.plugin = params.plugin
  if (params.hideEmpty) qs.hideEmpty = true
  const { data } = await http.get<PageResponse<RecipeCategory>>(
    `/datasets/${encodeURIComponent(datasetId)}/recipe-categories/page`,
    { params: qs },
  )
  return data
}

export async function listRecipeCategoryPlugins(datasetId: string): Promise<string[]> {
  const { data } = await http.get<string[]>(
    `/datasets/${encodeURIComponent(datasetId)}/recipe-categories/plugins`,
  )
  return data
}

export async function lookupRecipes(
  datasetId: string,
  target: string,
  kind: LookupKind,
  page: number,
  size: number,
  filters: { handlerId?: string; categoryId?: string; voltageTier?: string } = {},
): Promise<PageResponse<Recipe>> {
  const params: Record<string, string | number> = { target, kind, page, size }
  if (filters.handlerId) params.handlerId = filters.handlerId
  if (filters.categoryId) params.categoryId = filters.categoryId
  if (filters.voltageTier) params.voltageTier = filters.voltageTier
  const { data } = await http.get<PageResponse<Recipe>>(
    `/datasets/${encodeURIComponent(datasetId)}/recipes/lookup`,
    { params },
  )
  return data
}

export async function lookupBreakdown(
  datasetId: string,
  target: string,
  kind: LookupKind,
): Promise<HandlerBreakdown[]> {
  const { data } = await http.get<HandlerBreakdown[]>(
    `/datasets/${encodeURIComponent(datasetId)}/recipes/lookup/breakdown`,
    { params: { target, kind } },
  )
  return data
}

export async function getRecipeDetail(datasetId: string, recipeId: string): Promise<Recipe> {
  const { data } = await http.get<Recipe>(
    `/datasets/${encodeURIComponent(datasetId)}/recipes/${encodeURIComponent(recipeId)}`,
  )
  return data
}

export async function listRecipesByCategory(
  datasetId: string,
  categoryId: string,
  q: string,
  page: number,
  size: number,
  filters: { voltageTier?: string } = {},
): Promise<PageResponse<Recipe>> {
  const params: Record<string, string | number> = { page, size }
  const trimmed = q.trim()
  if (trimmed) params.q = trimmed
  if (filters.voltageTier) params.voltageTier = filters.voltageTier
  const { data } = await http.get<PageResponse<Recipe>>(
    `/datasets/${encodeURIComponent(datasetId)}/categories/${encodeURIComponent(categoryId)}/recipes`,
    { params },
  )
  return data
}

export async function listCategoryMachines(
  datasetId: string,
  categoryId: string,
): Promise<CategoryMachine[]> {
  const { data } = await http.get<CategoryMachine[]>(
    `/datasets/${encodeURIComponent(datasetId)}/categories/${encodeURIComponent(categoryId)}/machines`,
  )
  return data
}

export async function listCategoryVoltageTiers(
  datasetId: string,
  categoryId: string,
  scope: { target?: string; kind?: LookupKind } = {},
): Promise<CategoryVoltageTier[]> {
  const params: Record<string, string> = {}
  if (scope.target) params.target = scope.target
  if (scope.kind) params.kind = scope.kind
  const { data } = await http.get<CategoryVoltageTier[]>(
    `/datasets/${encodeURIComponent(datasetId)}/categories/${encodeURIComponent(categoryId)}/voltage-tiers`,
    { params },
  )
  return data
}
