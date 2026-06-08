<script setup lang="ts">
import { computed } from 'vue';
import AppTooltip from './AppTooltip.vue';

const props = withDefaults(
  defineProps<{
    targetId: string | null | undefined;
    displayName: string;
    assetUrl?: string | null;
    variant?: 'hero' | 'icon' | 'text' | 'row';
    placement?: 'top' | 'right' | 'bottom' | 'left';
    classPrefix: string;
  }>(),
  {
    assetUrl: null,
    variant: 'icon',
    placement: 'top',
  },
);

const emit = defineEmits<{
  (e: 'pick'): void;
  (e: 'lookup', kind: 'recipe' | 'usage'): void;
}>();

const className = computed(() => `${props.classPrefix} ${props.classPrefix}--${props.variant}`);
const nameClass = computed(() => `${props.classPrefix}-name`);

function pick() {
  if (!props.targetId) return;
  emit('pick');
}

function lookup(kind: 'recipe' | 'usage', e: MouseEvent) {
  e.preventDefault();
  if (!props.targetId) return;
  emit('lookup', kind);
}

function onAuxClick(e: MouseEvent) {
  if (e.button !== 1) return;
  lookup('usage', e);
}
</script>

<template>
  <AppTooltip :placement="placement">
    <template #content>
      <slot name="tooltip" />
    </template>

    <button
      :class="className"
      type="button"
      @click="pick"
      @contextmenu="lookup('recipe', $event)"
      @auxclick="onAuxClick"
    >
      <img
        v-if="assetUrl"
        :src="assetUrl"
        :alt="displayName"
        loading="lazy"
      >
      <span
        v-if="variant === 'text' || variant === 'row'"
        :class="nameClass"
      >{{
        displayName
      }}</span>
      <slot name="suffix" />
    </button>
  </AppTooltip>
</template>

<style scoped>
.item-ref,
.fluid-ref {
  appearance: none;
  border: 0;
  padding: 0;
  font: inherit;
  color: inherit;
  cursor: pointer;
  background: transparent;
}
.item-ref:focus-visible,
.fluid-ref:focus-visible {
  outline: 1px solid var(--el-color-primary-light-5);
  outline-offset: 2px;
}
.item-ref--hero,
.fluid-ref--hero {
  width: 112px;
  height: 112px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: var(--el-fill-color-lighter);
}
.item-ref--hero img,
.fluid-ref--hero img {
  width: 96px;
  height: 96px;
}
.item-ref--icon,
.fluid-ref--icon {
  width: 32px;
  height: 32px;
  display: inline-grid;
  place-items: center;
}
.item-ref--icon:hover,
.item-ref--icon:focus-visible,
.fluid-ref--icon:hover,
.fluid-ref--icon:focus-visible {
  outline: 1px solid var(--el-color-primary-light-5);
  outline-offset: 2px;
}
.item-ref--text,
.fluid-ref--text {
  width: 100%;
  min-width: 0;
  display: block;
  text-align: left;
}
.item-ref--text:hover,
.item-ref--text:focus-visible,
.fluid-ref--text:hover,
.fluid-ref--text:focus-visible {
  color: var(--el-color-primary);
  outline: none;
}
.item-ref--row,
.fluid-ref--row {
  width: 100%;
  min-width: 0;
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  padding: 6px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
  text-align: left;
}
.item-ref--row:hover,
.item-ref--row:focus-visible,
.fluid-ref--row:hover,
.fluid-ref--row:focus-visible {
  background: var(--el-color-primary-light-9);
  outline: 1px solid var(--el-color-primary-light-5);
}
.item-ref--row img,
.item-ref--icon img,
.fluid-ref--row img,
.fluid-ref--icon img {
  width: 32px;
  height: 32px;
}
.item-ref-name,
.fluid-ref-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-ref img,
.fluid-ref img {
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
  pointer-events: none;
}
</style>
