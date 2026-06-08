<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import BaseInteractiveRef from './BaseInteractiveRef.vue';
import ItemTooltipContent from './ItemTooltipContent.vue';

export interface InteractiveItemRefItem {
  itemVariantId: string;
  displayName?: string | null;
  tooltipText?: string | null;
  assetUrl?: string | null;
}

const props = withDefaults(
  defineProps<{
    item: InteractiveItemRefItem;
    variant?: 'hero' | 'icon' | 'text' | 'row';
    placement?: 'top' | 'right' | 'bottom' | 'left';
    label?: string;
  }>(),
  {
    variant: 'icon',
    placement: 'top',
    label: '',
  },
);

const emit = defineEmits<{
  (e: 'pick', item: InteractiveItemRefItem): void;
  (e: 'lookup', kind: 'recipe' | 'usage', item: InteractiveItemRefItem): void;
}>();

const { t } = useI18n();

const displayName = computed(
  () => props.label || props.item.displayName || props.item.itemVariantId || '—',
);

function pick() {
  emit('pick', props.item);
}

function lookup(kind: 'recipe' | 'usage') {
  emit('lookup', kind, props.item);
}
</script>

<template>
  <BaseInteractiveRef
    class-prefix="item-ref"
    :target-id="item.itemVariantId"
    :display-name="displayName"
    :asset-url="item.assetUrl"
    :variant="variant"
    :placement="placement"
    @pick="pick"
    @lookup="lookup"
  >
    <template #tooltip>
      <ItemTooltipContent
        :item="item"
        :context="{ hint: t('common.pickHintSlot') }"
      />
    </template>
  </BaseInteractiveRef>
</template>
