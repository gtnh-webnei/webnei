<script setup lang="ts">
import { computed } from 'vue'
import McCard from '@shared/ui/McCard.vue'
import AspectSigil from './AspectSigil.vue'
import type { AspectBrief } from '../types'

// 要素芯片：与目录 EntryCard 同一套 raised NEI 卡片语言。
const SIGIL_SIZE = 28

const props = defineProps<{
  entry: AspectBrief
  to?: { name: string; params: { aspectId: string } }
}>()

const isLink = computed(() => props.to != null)
</script>

<template>
  <McCard
    class="aspect-chip"
    tone="raised"
    :tag="isLink ? 'router-link' : 'div'"
    :clickable="isLink"
    v-bind="isLink ? { to } : {}"
  >
    <AspectSigil
      :icon="entry.icon"
      :name="entry.displayName"
      :color="entry.color"
      :size="SIGIL_SIZE"
    />
    <span class="aspect-chip-name">{{ entry.displayName }}</span>
  </McCard>
</template>

<style scoped>
.aspect-chip {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  height: 52px;
  min-width: 0;
  padding: 0 10px 0 8px;
  color: var(--mc-panel-text);
  text-decoration: none;
}

.aspect-chip-name {
  min-width: 0;
  overflow: hidden;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
