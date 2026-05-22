<script setup lang="ts">
import { computed } from 'vue'
import type { Recipe, RecipeCategory } from '@/api/recipes.types'
import { resolveRenderer } from './recipe/registry'

type SlotPayload = { itemVariantId: string | null; fluidVariantId: string | null }

const props = defineProps<{
  recipe: Recipe
  category?: RecipeCategory | null
  pickHint?: string
}>()

const emit = defineEmits<{
  (e: 'pick', payload: SlotPayload): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: SlotPayload): void
}>()

const Renderer = computed(() => resolveRenderer(props.recipe))
</script>

<template>
  <component
    :is="Renderer"
    :recipe="recipe"
    :category="category"
    :pick-hint="pickHint"
    @pick="(p: SlotPayload) => emit('pick', p)"
    @lookup="(k: 'recipe' | 'usage', p: SlotPayload) => emit('lookup', k, p)"
  />
</template>
