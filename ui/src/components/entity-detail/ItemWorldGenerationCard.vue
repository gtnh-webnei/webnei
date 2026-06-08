<script setup lang="ts">
import type { ItemWorldGenerationRef } from '@/api/items.types';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  resources: ItemWorldGenerationRef[];
}>();

const emit = defineEmits<{
  (e: 'open', section: string, key: string): void;
}>();
</script>

<template>
  <DetailSectionCard :title="$t('item.worldGeneration')">
    <div class="worldgen-list">
      <button
        v-for="resource in resources"
        :key="`${resource.section}|${resource.key}`"
        type="button"
        class="worldgen-row"
        @click="emit('open', resource.section, resource.key)"
      >
        <span class="worldgen-title">{{ resource.title }}</span>
        <span class="worldgen-type">{{ resource.type }}</span>
        <span
          v-if="resource.dimensions.length"
          class="worldgen-dimensions"
        >
          <span
            v-for="dim in resource.dimensions.slice(0, 6)"
            :key="dim.dimension"
            class="worldgen-dimension-icon"
          >
            <img
              v-if="dim.iconAssetUrl && resource.section !== 'bartworks-ores'"
              :src="dim.iconAssetUrl"
              :alt="dim.displayName"
            >
            <span
              v-else
              class="worldgen-dimension-text"
            >{{ dim.displayName }}</span>
          </span>
        </span>
        <span class="worldgen-stat">{{ resource.statText }}</span>
      </button>
    </div>
  </DetailSectionCard>
</template>

<style scoped>
.worldgen-list {
  display: grid;
  gap: 8px;
}
.worldgen-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  gap: 4px 8px;
  align-items: center;
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-light);
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}
.worldgen-row:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.worldgen-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}
.worldgen-type,
.worldgen-dimensions,
.worldgen-stat {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.worldgen-type,
.worldgen-stat {
  text-align: right;
}
.worldgen-dimensions {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 5px;
  align-items: center;
  min-width: 0;
  overflow: hidden;
}
.worldgen-dimension-icon {
  min-width: 26px;
  width: auto;
  height: 26px;
  display: inline-grid;
  place-items: center;
}
.worldgen-dimension-icon img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.worldgen-dimension-text {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.worldgen-stat {
  color: var(--el-color-primary);
}
</style>
