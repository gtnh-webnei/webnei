import { defineAsyncComponent, type Component } from 'vue';
import type { Recipe, RecipeCategory } from '@/api/recipes.types';

const CATEGORY_OVERRIDES: Record<string, Component> = {
  'appliedenergistics2:inscriber': defineAsyncComponent(
    () => import('./renderers/InscriberRecipePanel.vue'),
  ),
};

const DEFAULT_RENDERER = defineAsyncComponent(() => import('./renderers/DefaultRecipePanel.vue'));

export function resolveRenderer(
  recipe: Pick<Recipe, 'categoryId'>,
  _category: Pick<RecipeCategory, 'handlerClass'> | null | undefined,
): Component {
  const byCategory = CATEGORY_OVERRIDES[recipe.categoryId];
  if (byCategory) return byCategory;
  // The default renderer now handles metadata (GregTech voltage/EU, Blood Magic LP, etc.),
  // special-item slots, and the tier badge generically via recipe.metadata + the display spec.
  return DEFAULT_RENDERER;
}
