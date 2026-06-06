<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import IngredientTooltipBase, { type IngredientTooltipContext } from './IngredientTooltipBase.vue';

export interface FluidTooltipFluid {
  fluidVariantId: string | null;
  displayName?: string | null;
  tooltipText?: string | null;
  gaseous?: boolean | null;
  temperature?: number | null;
  modName?: string | null;
}

const props = withDefaults(
  defineProps<{
    fluid: FluidTooltipFluid;
    context?: IngredientTooltipContext;
  }>(),
  {
    context: () => ({}),
  },
);

const { t } = useI18n();

const ingredient = computed(() => ({
  displayName: props.fluid.displayName ?? props.fluid.fluidVariantId ?? '—',
  tooltipText: props.fluid.tooltipText,
  modName: props.fluid.modName,
}));
const fluidStateLabel = computed(() => {
  if (props.fluid.gaseous == null) return null;
  return props.fluid.gaseous ? t('fluid.gaseous') : t('fluid.liquid');
});
const fluidTemperatureLabel = computed(() => {
  if (props.fluid.temperature == null) return null;
  return `${props.fluid.temperature.toLocaleString()} K`;
});
</script>

<template>
  <IngredientTooltipBase :ingredient="ingredient" :context="context" variant="fluid">
    <div v-if="fluidTemperatureLabel" class="fluid-line temperature-line">
      {{ t('fluid.temperature') }}:{{ fluidTemperatureLabel }}
    </div>
    <div v-if="fluidStateLabel" class="fluid-line state-line">
      {{ t('fluid.state') }}:{{ fluidStateLabel }}
    </div>
  </IngredientTooltipBase>
</template>

<style scoped>
.fluid-line {
  white-space: nowrap;
}
.temperature-line {
  color: #5555ff;
}
.state-line {
  color: #00aa00;
}
</style>
