<script setup lang="ts">
import type { FluidBlockEntry } from '@/api/extras.types';
import InteractiveItemRef from '@/components/InteractiveItemRef.vue';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  blocks: FluidBlockEntry[];
}>();

const emit = defineEmits<{
  (e: 'openItem', itemVariantId: string): void;
  (e: 'lookupItem', kind: 'recipe' | 'usage', itemVariantId: string): void;
}>();
</script>

<template>
  <DetailSectionCard :title="$t('fluid.correspondingBlock')">
    <div class="block-list">
      <InteractiveItemRef
        v-for="b in blocks"
        :key="b.item.itemVariantId"
        :item="b.item"
        variant="row"
        @pick="(item) => emit('openItem', item.itemVariantId)"
        @lookup="(kind, item) => emit('lookupItem', kind, item.itemVariantId)"
      />
    </div>
  </DetailSectionCard>
</template>

<style scoped>
.block-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px;
}
</style>
