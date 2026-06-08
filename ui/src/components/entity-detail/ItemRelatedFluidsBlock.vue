<script setup lang="ts">
import type { ItemRelatedFluidEntry } from '@/api/extras.types';
import InteractiveFluidRef, {
  type InteractiveFluidRefFluid,
} from '@/components/InteractiveFluidRef.vue';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  fluids: ItemRelatedFluidEntry[];
}>();

const emit = defineEmits<{
  (e: 'pickFluid', fluid: InteractiveFluidRefFluid): void;
  (e: 'lookupFluid', kind: 'recipe' | 'usage', fluid: InteractiveFluidRefFluid): void;
}>();
</script>

<template>
  <DetailSectionCard :title="$t('item.relatedFluids')">
    <div class="related-fluid-list">
      <InteractiveFluidRef
        v-for="entry in fluids"
        :key="entry.fluid.fluidVariantId"
        :fluid="entry.fluid"
        variant="row"
        @pick="emit('pickFluid', $event)"
        @lookup="(kind, target) => emit('lookupFluid', kind, target)"
      />
    </div>
  </DetailSectionCard>
</template>

<style scoped>
.related-fluid-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px;
}
</style>
