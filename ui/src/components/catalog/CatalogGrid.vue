<script setup lang="ts">
import CatalogEntryCard from './CatalogEntryCard.vue'
import type { CatalogEntry, CatalogKind } from '@/api/types'

defineProps<{
  kind: CatalogKind
  items: CatalogEntry[]
}>()
</script>

<template>
  <div class="catalog-grid">
    <CatalogEntryCard
      v-for="entry in items"
      :key="entry.id"
      :kind="kind"
      :entry="entry"
    />
  </div>
</template>

<style scoped>
.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  grid-auto-rows: 68px;
  gap: 8px;
  min-width: 0;
}

@media (max-width: 520px) {
  .catalog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
