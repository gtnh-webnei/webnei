<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import EntryIcon from '@shared/components/EntryIcon.vue'
import McSlot from '@shared/ui/McSlot.vue'
import McCard from '@shared/ui/McCard.vue'
import { stripMcFormat } from '@shared/utils/mcFormat'
import type { RecipeCategoryListEntry } from '../../types'

const props = defineProps<{
  entry: RecipeCategoryListEntry
}>()

const { t } = useI18n()

const slotSize = 40

const displayName = computed(() => stripMcFormat(props.entry.displayName))
const modLabel = computed(() => stripMcFormat(props.entry.modName || props.entry.modId))
const recipeLabel = computed(() => t('catalog.recipeCount', { count: props.entry.recipeCount }))
</script>

<template>
  <McCard class="category-card">
    <McSlot
      class="category-card-slot"
      :size="slotSize"
    >
      <EntryIcon
        kind="recipeCategory"
        :icon="entry.icon"
        :alt="displayName"
        :size="slotSize - 4"
      />
    </McSlot>
    <span class="category-card-text">
      <span class="category-card-name">{{ displayName }}</span>
      <span class="category-card-mod">{{ modLabel }}</span>
      <span class="category-card-count">{{ recipeLabel }}</span>
    </span>
  </McCard>
</template>

<style scoped>
.category-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-width: 0;
  height: 68px;
  padding: 8px;
}

.category-card:hover .category-card-name {
  color: var(--mc-ink);
}

.category-card-slot {
  justify-self: center;
  align-self: center;
}

.category-card-text {
  display: grid;
  min-width: 0;
  gap: 1px;
}

.category-card-name,
.category-card-mod,
.category-card-count {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-card-name {
  font-size: 13px;
  font-weight: 800;
  line-height: 1.25;
}

.category-card-mod,
.category-card-count {
  color: var(--app-surface-muted);
  font-size: 11px;
  line-height: 1.25;
}
</style>
