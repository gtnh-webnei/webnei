<script setup lang="ts" generic="T extends EntryBase">
import EntryCard from './EntryCard.vue'
import type { EntryBase, EntryKind } from '@shared/types'

defineProps<{
  kind: EntryKind
  items: T[]
}>()

defineSlots<{
  trailing?: (props: { entry: T }) => unknown
}>()
</script>

<template>
  <div class="entry-grid">
    <EntryCard
      v-for="entry in items"
      :key="entry.id"
      :kind="kind"
      :entry="entry"
    >
      <template
        v-if="$slots.trailing"
        #trailing
      >
        <slot
          name="trailing"
          :entry="entry"
        />
      </template>
    </EntryCard>
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
