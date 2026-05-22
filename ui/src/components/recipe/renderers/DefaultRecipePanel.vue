<script setup lang="ts">
import { computed } from 'vue'
import type { Recipe, RecipeCategory } from '@/api/recipes.types'
import RecipePanelShell from '../RecipePanelShell.vue'
import SidesLayout from '../SidesLayout.vue'

const props = defineProps<{
  recipe: Recipe
  category?: RecipeCategory | null
  pickHint?: string
}>()

const emit = defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
}>()

const declaredCols = computed(() => props.category?.itemInputWidth ?? null)
const declaredRows = computed(() => props.category?.itemInputHeight ?? null)
</script>

<template>
  <RecipePanelShell
    :recipe="recipe"
    :category="category"
    :declared-cols="declaredCols"
    :declared-rows="declaredRows"
  >
    <SidesLayout
      :slots="recipe.slots"
      :category="category"
      :pick-hint="pickHint"
      @pick="(p) => emit('pick', p)"
      @lookup="(k, p) => emit('lookup', k, p)"
    />
  </RecipePanelShell>
</template>
