import { defineAsyncComponent, type Component } from 'vue'
import type { Recipe, RecipeCategory } from '@/api/recipes.types'

const CATEGORY_OVERRIDES: Record<string, Component> = {
  'appliedenergistics2:inscriber': defineAsyncComponent(
    () => import('./renderers/InscriberRecipePanel.vue'),
  ),
}

const HANDLER_OVERRIDES: Record<string, Component> = {
  // Every GT5 RecipeMap routes through GTNEIDefaultHandler — single GregTech renderer.
  'gregtech.nei.GTNEIDefaultHandler': defineAsyncComponent(
    () => import('./renderers/GregTechRecipePanel.vue'),
  ),
}

const DEFAULT_RENDERER = defineAsyncComponent(
  () => import('./renderers/DefaultRecipePanel.vue'),
)

export function resolveRenderer(
  recipe: Pick<Recipe, 'categoryId'>,
  category: Pick<RecipeCategory, 'handlerClass'> | null | undefined,
): Component {
  const byCategory = CATEGORY_OVERRIDES[recipe.categoryId]
  if (byCategory) return byCategory
  const handlerClass = category?.handlerClass
  if (handlerClass && HANDLER_OVERRIDES[handlerClass]) return HANDLER_OVERRIDES[handlerClass]
  return DEFAULT_RENDERER
}
