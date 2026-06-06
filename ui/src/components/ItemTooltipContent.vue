<script setup lang="ts">
import { computed } from 'vue';
import IngredientTooltipBase, { type IngredientTooltipContext } from './IngredientTooltipBase.vue';

export interface ItemTooltipItem {
  itemVariantId: string | null;
  displayName?: string | null;
  tooltipText?: string | null;
  modName?: string | null;
}

const props = withDefaults(
  defineProps<{
    item: ItemTooltipItem;
    context?: IngredientTooltipContext;
  }>(),
  {
    context: () => ({}),
  },
);

const ingredient = computed(() => ({
  displayName: props.item.displayName ?? props.item.itemVariantId ?? '—',
  tooltipText: props.item.tooltipText,
  modName: props.item.modName,
}));
</script>

<template>
  <IngredientTooltipBase :ingredient="ingredient" :context="context" variant="item" />
</template>
