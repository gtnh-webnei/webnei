<script setup lang="ts" generic="T extends EntryBase">
import EntryCard from './EntryCard.vue'
import type { EntryBase, EntryKind } from '@shared/types'

withDefaults(
  defineProps<{
    kind: EntryKind
    items: T[]
    tone?: 'raised' | 'inset'
    layout?: 'grid' | 'list'
  }>(),
  {
    tone: 'raised',
    layout: 'grid',
  },
)

defineSlots<{
  item?: (props: { entry: T }) => unknown
  trailing?: (props: { entry: T }) => unknown
}>()
</script>

<template>
  <div
    class="entry-grid"
    :class="[`is-${layout}`]"
  >
    <template
      v-for="entry in items"
      :key="entry.id"
    >
      <slot
        name="item"
        :entry="entry"
      >
        <EntryCard
          :kind="kind"
          :entry="entry"
          :tone="tone"
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
      </slot>
    </template>
  </div>
</template>

<style scoped>
.entry-grid {
  display: grid;
  min-width: 0;
}

.entry-grid.is-grid {
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  grid-auto-rows: 68px;
  gap: 8px;
}

.entry-grid.is-list {
  grid-template-columns: minmax(0, 1fr);
  gap: 6px;
}

@media (max-width: 520px) {
  .entry-grid.is-grid {
    grid-template-columns: 1fr;
  }
}
</style>
