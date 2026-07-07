<script setup lang="ts">
import EntryCard from './EntryCard.vue'
import type { EntryBase, EntryKind } from '@shared/types'

defineProps<{
  kind: EntryKind
  items: EntryBase[]
}>()
</script>

<template>
  <div class="entry-grid">
    <EntryCard
      v-for="entry in items"
      :key="entry.id"
      :kind="kind"
      :entry="entry"
    />
  </div>
</template>

<style scoped>
.entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  grid-auto-rows: 68px;
  gap: 8px;
  min-width: 0;
}

@media (max-width: 520px) {
  .entry-grid {
    grid-template-columns: 1fr;
  }
}
</style>
