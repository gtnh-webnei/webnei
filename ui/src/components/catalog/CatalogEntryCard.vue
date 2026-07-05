<script setup lang="ts">
import { computed } from 'vue'
import EntryIcon from './EntryIcon.vue'
import McSlot from '@/components/ui/McSlot.vue'
import McCard from '@/components/ui/McCard.vue'
import { stripMcFormat } from '@/utils/mcFormat'
import type { CatalogEntry, CatalogKind } from '@/api/types'

const props = defineProps<{
  kind: CatalogKind
  entry: CatalogEntry
}>()

const slotSize = 40

const displayName = computed(() => stripMcFormat(props.entry.displayName))
const modLabel = computed(() => stripMcFormat(props.entry.modName || props.entry.modId))
const detailLabel = computed(() => `${props.entry.modId}:${props.entry.registryName}`)
</script>

<template>
  <McCard class="catalog-card">
    <McSlot
      class="catalog-card-slot"
      :size="slotSize"
    >
      <EntryIcon
        :kind="kind"
        :icon="entry.icon"
        :alt="displayName"
        :size="slotSize - 4"
      />
    </McSlot>
    <span class="catalog-card-text">
      <span class="catalog-card-name">{{ displayName }}</span>
      <span class="catalog-card-mod">{{ modLabel }}</span>
      <span class="catalog-card-detail">{{ detailLabel }}</span>
    </span>
  </McCard>
</template>

<style scoped>
.catalog-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-width: 0;
  height: 68px;
  padding: 8px;
}

.catalog-card:hover .catalog-card-name {
  color: var(--mc-ink);
}

.catalog-card-slot {
  justify-self: center;
  align-self: center;
}

.catalog-card-text {
  display: grid;
  min-width: 0;
  gap: 1px;
}

.catalog-card-name,
.catalog-card-mod,
.catalog-card-detail {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.catalog-card-name {
  font-size: 13px;
  font-weight: 800;
  line-height: 1.25;
}

.catalog-card-mod,
.catalog-card-detail {
  color: var(--app-surface-muted);
  font-size: 11px;
  line-height: 1.25;
}
</style>
