import { defineAsyncComponent, type Component } from 'vue'
import type { Recipe } from '@/api/recipes.types'

const REGISTRY: Record<string, Component> = {
  gregtech_machine: defineAsyncComponent(
    () => import('./renderers/GregTechRecipePanel.vue'),
  ),
}

const CATEGORY_OVERRIDES: Record<string, Component> = {
  'appliedenergistics2:inscriber': defineAsyncComponent(
    () => import('./renderers/InscriberRecipePanel.vue'),
  ),
}

const DEFAULT_RENDERER = defineAsyncComponent(
  () => import('./renderers/DefaultRecipePanel.vue'),
)

export function resolveRenderer(
  recipe: Pick<Recipe, 'uiKind' | 'categoryId'>,
): Component {
  return (
    CATEGORY_OVERRIDES[recipe.categoryId]
    ?? REGISTRY[recipe.uiKind]
    ?? DEFAULT_RENDERER
  )
}
