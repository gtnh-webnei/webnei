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
  grid-template-columns: 40px minmax(0, 1fr);
  align-items: center;
  gap: 9px;
  min-width: 0;
  min-height: 56px;
  padding: 6px 10px 6px 8px;
  color: var(--mc-panel-text);
  text-decoration: none;
}

.aspect-chip-name {
  display: -webkit-box;
  min-width: 0;
  overflow: hidden;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.3;
  overflow-wrap: anywhere;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
