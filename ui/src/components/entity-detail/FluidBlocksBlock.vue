<script setup lang="ts">
import type { FluidBlockEntry } from '@/api/extras.types';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  blocks: FluidBlockEntry[];
}>();

const emit = defineEmits<{
  (e: 'openItem', itemVariantId: string): void;
}>();
</script>

<template>
  <DetailSectionCard :title="$t('fluid.correspondingBlock')">
    <div class="block-list">
      <div
        v-for="b in blocks"
        :key="b.blockItemVariantId"
        class="block-cell"
        :title="b.blockDisplayName ?? b.blockItemVariantId"
        @click="emit('openItem', b.blockItemVariantId)"
      >
        <img
          v-if="b.blockAssetUrl"
          :src="b.blockAssetUrl"
          loading="lazy"
        >
        <span class="block-name">{{ b.blockDisplayName ?? b.blockItemVariantId }}</span>
      </div>
    </div>
  </DetailSectionCard>
</template>

<style scoped>
.block-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.block-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  cursor: pointer;
  background: var(--el-bg-color);
  transition: border-color 0.15s;
  font-size: 12px;
}
.block-cell:hover {
  border-color: var(--el-color-primary);
}
.block-cell img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  image-rendering: pixelated;
}
.block-name {
  font-size: 12px;
}
</style>
