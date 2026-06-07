<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import AppTooltip from './AppTooltip.vue';
import FluidTooltipContent from './FluidTooltipContent.vue';

export interface InteractiveFluidRefFluid {
  fluidVariantId: string;
  fluidId?: string | null;
  displayName?: string | null;
  assetUrl?: string | null;
  gaseous?: boolean | null;
  temperature?: number | null;
  modName?: string | null;
}

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
  if (!props.fluid.fluidVariantId) return;
  emit('pick', props.fluid);
}
function lookup(kind: 'recipe' | 'usage', e: MouseEvent) {
  e.preventDefault();
  if (!props.fluid.fluidVariantId) return;
  emit('lookup', kind, props.fluid);
}
function onAuxClick(e: MouseEvent) {
  if (e.button !== 1) return;
  lookup('usage', e);
}
</script>

<template>
  <AppTooltip :placement="placement">
    <template #content>
      <FluidTooltipContent
        :fluid="fluid"
        :context="{ hint: t('common.pickHintSlot') }"
      />
    </template>

    <button
      class="fluid-ref"
      :class="`fluid-ref--${variant}`"
      type="button"
      @click="pick"
      @contextmenu="lookup('recipe', $event)"
      @auxclick="onAuxClick"
    >
      <img
        v-if="fluid.assetUrl"
        :src="fluid.assetUrl"
        :alt="displayName"
        loading="lazy"
      >
      <span
        v-if="variant === 'text' || variant === 'row'"
        class="fluid-ref-name"
      >{{
        displayName
      }}</span>
    </button>
  </AppTooltip>
</template>

<style scoped>
.fluid-ref {
  appearance: none;
  border: 0;
  padding: 0;
  font: inherit;
  color: inherit;
  cursor: pointer;
  background: transparent;
}
.fluid-ref:focus-visible {
  outline: 1px solid var(--el-color-primary-light-5);
  outline-offset: 2px;
}
.fluid-ref--icon {
  width: 32px;
  height: 32px;
  display: inline-grid;
  place-items: center;
}
.fluid-ref--icon:hover,
.fluid-ref--icon:focus-visible {
  outline: 1px solid var(--el-color-primary-light-5);
  outline-offset: 2px;
}
.fluid-ref--text {
  width: 100%;
  min-width: 0;
  display: block;
  text-align: left;
}
.fluid-ref--text:hover,
.fluid-ref--text:focus-visible {
  color: var(--el-color-primary);
  outline: none;
}
.fluid-ref--row {
  width: 100%;
  min-width: 0;
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  padding: 6px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
  text-align: left;
}
.fluid-ref--row:hover,
.fluid-ref--row:focus-visible {
  background: var(--el-color-primary-light-9);
  outline: 1px solid var(--el-color-primary-light-5);
}
.fluid-ref--row img,
.fluid-ref--icon img {
  width: 32px;
  height: 32px;
}
.fluid-ref-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.fluid-ref img {
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
  pointer-events: none;
}
</style>
