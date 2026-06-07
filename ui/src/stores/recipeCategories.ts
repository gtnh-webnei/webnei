import { defineStore } from 'pinia';
import { ref } from 'vue';
import { listRecipeCategories } from '@/api/recipes';
import type { RecipeCategory } from '@/api/recipes.types';

export const useRecipeCategoryStore = defineStore('recipeCategories', () => {
  const categoriesByDataset = ref<Record<string, RecipeCategory[]>>({});
  const pendingByDataset = new Map<string, Promise<RecipeCategory[]>>();

  async function load(datasetId: string): Promise<RecipeCategory[]> {
    const cached = categoriesByDataset.value[datasetId];
    if (cached) return cached;

    const pending = pendingByDataset.get(datasetId);
    if (pending) return pending;

    const request = listRecipeCategories(datasetId)
      .then((categories) => {
        categoriesByDataset.value = {
          ...categoriesByDataset.value,
          [datasetId]: categories,
        };
        return categories;
      })
      .finally(() => {
        pendingByDataset.delete(datasetId);
      });

    pendingByDataset.set(datasetId, request);
    return request;
  }

  async function findById(datasetId: string, categoryId: string): Promise<RecipeCategory | null> {
    const categories = await load(datasetId);
    return categories.find((category) => category.categoryId === categoryId) ?? null;
  }

  function clearDataset(datasetId: string) {
    const rest = { ...categoriesByDataset.value };
    delete rest[datasetId];
    categoriesByDataset.value = rest;
    pendingByDataset.delete(datasetId);
  }

  return { categoriesByDataset, load, findById, clearDataset };
});
