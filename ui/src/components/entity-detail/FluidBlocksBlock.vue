<script setup lang="ts">
import type { FluidBlockEntry } from '@/api/extras.types';
import InteractiveItemRef, {
  type InteractiveItemRefItem,
} from '@/components/InteractiveItemRef.vue';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  blocks: FluidBlockEntry[];
}>();

const emit = defineEmits<{
  (e: 'openItem', itemVariantId: string): void;
  (e: 'lookupItem', kind: 'recipe' | 'usage', itemVariantId: string): void;
}>();

function itemRefFromBlock(block: FluidBlockEntry): InteractiveItemRefItem {
  return {
    itemVariantId: block.blockItemVariantId,
    displayName: block.blockDisplayName,
    tooltipText: block.blockTooltipText,
    assetUrl: block.blockAssetUrl,
  };
}
</script>

<template>
  <DetailSectionCard :title="$t('fluid.correspondingBlock')">
    <div class="block-list">
      <InteractiveItemRef
        v-for="b in blocks"
        :key="b.blockItemVariantId"
        :item="itemRefFromBlock(b)"
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
