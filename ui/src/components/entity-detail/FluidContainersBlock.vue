<script setup lang="ts">
import type { FluidContainerEntry } from '@/api/extras.types';
import ExtrasBlock from './ExtrasBlock.vue';

defineProps<{
  containers: FluidContainerEntry[];
}>();

const emit = defineEmits<{
  (e: 'openItem', itemVariantId: string): void;
}>();
</script>

<template>
  <ExtrasBlock
    :title="$t('common.fluidContainer')"
    :count="containers.length"
  >
    <div class="container-list">
      <div
        v-for="(c, idx) in containers"
        :key="`${c.fluidVariantId}-${c.containerItemVariantId}-${idx}`"
        class="container-row"
      >
        <div
          class="container-cell"
          :title="c.containerDisplayName ?? c.containerItemVariantId"
          @click="emit('openItem', c.containerItemVariantId)"
        >
          <img
            v-if="c.containerAssetUrl"
            :src="c.containerAssetUrl"
            loading="lazy"
          >
          <span class="container-name">{{
            c.containerDisplayName ?? c.containerItemVariantId
          }}</span>
        </div>
        <div class="container-arrow">
          <span
            v-if="c.amount > 0"
            class="container-amount"
          >{{ c.amount }} mB</span>
          <span class="container-arrow-line">→</span>
        </div>
        <div
          class="container-cell"
          :title="c.emptyContainerDisplayName ?? c.emptyContainerItemVariantId"
          @click="emit('openItem', c.emptyContainerItemVariantId)"
        >
          <img
            v-if="c.emptyContainerAssetUrl"
            :src="c.emptyContainerAssetUrl"
            loading="lazy"
          >
          <span class="container-name">{{
            c.emptyContainerDisplayName ?? c.emptyContainerItemVariantId
          }}</span>
        </div>
      </div>
    </div>
  </ExtrasBlock>
</template>

<style scoped>
.container-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.container-row {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}
.container-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 6px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  cursor: pointer;
  min-width: 0;
  background: var(--el-bg-color);
  transition: border-color 0.15s;
}
.container-cell:hover {
  border-color: var(--el-color-primary);
}
.container-cell img {
  width: 22px;
  height: 22px;
  object-fit: contain;
  image-rendering: pixelated;
  flex-shrink: 0;
}
.container-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}
.container-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 10px;
  line-height: 1.1;
  color: var(--el-text-color-secondary);
}
.container-amount {
  font-variant-numeric: tabular-nums;
}
.container-arrow-line {
  font-size: 14px;
  color: var(--el-text-color-disabled);
}
</style>
