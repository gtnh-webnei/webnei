<script setup lang="ts">
import type { FluidContainerEntry } from '@/api/extras.types';
import InteractiveItemRef from '@/components/InteractiveItemRef.vue';
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
      <InteractiveItemRef
        v-for="(entry, idx) in containers"
        :key="`${entry.container.itemVariantId}-${idx}`"
        :item="entry.container"
        variant="row"
        @pick="(item) => emit('openItem', item.itemVariantId)"
      >
        <template #suffix>
          <span
            v-if="entry.amount > 0"
            class="container-amount"
          >{{ entry.amount }} mB</span>
        </template>
      </InteractiveItemRef>
    </div>
  </DetailSectionCard>
</template>

<style scoped>
.container-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}
.container-amount {
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  color: var(--el-text-color-secondary);
}
</style>
