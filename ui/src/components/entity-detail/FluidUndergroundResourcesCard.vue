<script setup lang="ts">
import type { FluidUndergroundResource } from '@/api/fluids.types';
import { formatChancePercent } from '@/utils/format';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  resources: FluidUndergroundResource[];
}>();

const emit = defineEmits<{
  (e: 'open', fluidId: string, dimension: string): void;
}>();
</script>

<template>
  <DetailSectionCard :title="$t('fluid.undergroundResources')">
    <div class="underground-list">
      <button
        v-for="resource in resources"
        :key="`${resource.fluidId}|${resource.dimension}`"
        type="button"
        class="underground-row"
        @click="emit('open', resource.fluidId, resource.dimension)"
      >
        <span class="underground-dimension">
          <img
            v-if="resource.dimensionDisplay.iconAssetUrl"
            :src="resource.dimensionDisplay.iconAssetUrl"
            :alt="resource.dimensionDisplay.displayName"
          >
          <span class="underground-dimension-name">
            {{ resource.dimensionDisplay.displayName }}
          </span>
        </span>
        <span class="underground-stats">
          <el-tag
            size="small"
            effect="plain"
            round
          >
            {{ resource.minAmount }}-{{ resource.maxAmount }} L
          </el-tag>
          <el-tag
            size="small"
            type="warning"
            effect="plain"
            round
          >
            {{ $t('common.probability') }} {{ formatChancePercent(resource.chance) }}
          </el-tag>
        </span>
      </button>
    </div>
  </DetailSectionCard>
</template>

<style scoped>
.underground-list {
  display: grid;
  gap: 8px;
}
.underground-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  gap: 10px;
  align-items: center;
  width: 100%;
  min-height: 48px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-light);
  padding: 7px 10px;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}
.underground-row:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-fill-color);
}
.underground-dimension {
  min-width: 0;
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
}
.underground-dimension img {
  width: 16px;
  height: 16px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.underground-dimension-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.underground-stats {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 4px;
}
</style>
