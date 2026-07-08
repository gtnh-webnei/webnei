<script setup lang="ts">
import RecipeCategoryCard from './RecipeCategoryCard.vue'
import type { RecipeCategoryListEntry } from '../../types'

defineProps<{
  items: RecipeCategoryListEntry[]
}>()
</script>

<template>
  <div class="category-grid">
    <RecipeCategoryCard
      v-for="entry in items"
      :key="entry.id"
      :entry="entry"
    />
  </div>
</template>

<style scoped>
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  grid-auto-rows: 68px;
  gap: 8px;
  min-width: 0;
}

@media (max-width: 520px) {
  .category-grid {
    grid-template-columns: 1fr;
  }
}
</style>
