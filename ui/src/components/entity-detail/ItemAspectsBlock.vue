<script setup lang="ts">
import type { AspectEntry } from '@/api/extras.types';
import ExtrasBlock from './ExtrasBlock.vue';

defineProps<{
  aspects: AspectEntry[];
}>();
</script>

<template>
  <ExtrasBlock
    :title="$t('item.thaumcraftAspects')"
    :count="aspects.length"
  >
    <div class="aspect-list">
      <div
        v-for="a in aspects"
        :key="a.aspectId"
        class="aspect-item"
        :title="a.description"
      >
        <img
          v-if="a.iconAssetUrl"
          :src="a.iconAssetUrl"
          :alt="a.name"
          class="aspect-icon"
          loading="lazy"
        >
        <div class="aspect-meta">
          <div class="aspect-name">
            {{ a.name }}
            <el-tag
              v-if="a.primal"
              size="small"
              type="warning"
              effect="plain"
              round
            >
              {{ $t('item.primal') }}
            </el-tag>
          </div>
          <div class="aspect-amount">
            × {{ a.amount }}
          </div>
        </div>
      </div>
    </div>
  </ExtrasBlock>
</template>

<style scoped>
.aspect-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.aspect-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}
.aspect-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
  image-rendering: pixelated;
}
.aspect-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.aspect-name {
  font-size: 12px;
  font-weight: 500;
  display: grid;
  grid-template-columns: minmax(0, max-content) max-content;
  align-items: center;
  gap: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.aspect-amount {
  font-size: 11px;
  color: var(--el-color-primary);
  font-variant-numeric: tabular-nums;
}
</style>
