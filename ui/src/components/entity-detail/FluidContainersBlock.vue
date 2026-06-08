<script setup lang="ts">
import type { FluidContainerEntry } from '@/api/extras.types';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  containers: FluidContainerEntry[];
}>();

const emit = defineEmits<{
  (e: 'openItem', itemVariantId: string): void;
}>();
</script>

<template>
  <DetailSectionCard :title="$t('common.fluidContainer')">
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
          <img v-if="c.containerAssetUrl" :src="c.containerAssetUrl" loading="lazy" />
          <span class="container-name">{{
            c.containerDisplayName ?? c.containerItemVariantId
          }}</span>
          <span v-if="c.amount > 0" class="container-amount">{{ c.amount }} mB</span>
        </div>
      </div>
    </div>
  </DetailSectionCard>
</template>

<style scoped>
.container-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}
.container-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  font-size: 12px;
}
.container-cell {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr) auto;
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
.container-amount {
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  color: var(--el-text-color-secondary);
}
</style>
