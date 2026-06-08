<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { FluidRef } from '@/api/entityRefs.types';
import BaseInteractiveRef from './BaseInteractiveRef.vue';
import FluidTooltipContent from './FluidTooltipContent.vue';

export type InteractiveFluidRefFluid = FluidRef;

const props = withDefaults(
  defineProps<{
    fluid: InteractiveFluidRefFluid;
    variant?: 'icon' | 'text' | 'row';
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
  (e: 'pick', fluid: InteractiveFluidRefFluid): void;
  (e: 'lookup', kind: 'recipe' | 'usage', fluid: InteractiveFluidRefFluid): void;
}>();

const { t } = useI18n();

const displayName = computed(
  () => props.label || props.fluid.displayName || props.fluid.fluidVariantId || '—',
);

function pick() {
  emit('pick', props.fluid);
}

function lookup(kind: 'recipe' | 'usage') {
  emit('lookup', kind, props.fluid);
}
</script>

<template>
  <BaseInteractiveRef
    class-prefix="fluid-ref"
    :target-id="fluid.fluidVariantId"
    :display-name="displayName"
    :asset-url="fluid.assetUrl"
    :variant="variant"
    :placement="placement"
    @pick="pick"
    @lookup="lookup"
  >
    <template #tooltip>
      <FluidTooltipContent
        :fluid="fluid"
        :context="{ hint: t('common.pickHintSlot') }"
      />
    </template>
  </BaseInteractiveRef>
</template>
