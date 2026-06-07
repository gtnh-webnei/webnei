<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import AppTooltip from './AppTooltip.vue';
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
  if (!props.item.itemVariantId) return;
  emit('pick', props.item);
}

function lookup(kind: 'recipe' | 'usage', e: MouseEvent) {
  e.preventDefault();
  if (!props.item.itemVariantId) return;
  emit('lookup', kind, props.item);
}

function onAuxClick(e: MouseEvent) {
  if (e.button !== 1) return;
  lookup('usage', e);
}
</script>

<template>
  <AppTooltip :placement="placement">
    <template #content>
      <ItemTooltipContent
        :item="item"
        :context="{ hint: t('common.pickHintSlot') }"
      />
    </template>

    <button
      class="item-ref"
      :class="`item-ref--${variant}`"
      type="button"
      @click="pick"
      @contextmenu="lookup('recipe', $event)"
      @auxclick="onAuxClick"
    >
      <img
        v-if="item.assetUrl"
        :src="item.assetUrl"
        :alt="displayName"
        loading="lazy"
      >
      <span
        v-if="variant === 'text' || variant === 'row'"
        class="item-ref-name"
      >{{
        displayName
      }}</span>
    </button>
  </AppTooltip>
</template>

<style scoped>
.item-ref {
  appearance: none;
  border: 0;
  padding: 0;
  font: inherit;
  color: inherit;
  cursor: pointer;
  background: transparent;
}
.item-ref:focus-visible {
  outline: 1px solid var(--el-color-primary-light-5);
  outline-offset: 2px;
}
.item-ref--hero {
  width: 112px;
  height: 112px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: var(--el-fill-color-lighter);
}
.item-ref--hero img {
  width: 96px;
  height: 96px;
}
.item-ref--icon {
  width: 32px;
  height: 32px;
  display: inline-grid;
  place-items: center;
}
.item-ref--icon:hover,
.item-ref--icon:focus-visible {
  outline: 1px solid var(--el-color-primary-light-5);
  outline-offset: 2px;
}
.item-ref--text {
  width: 100%;
  min-width: 0;
  display: block;
  text-align: left;
}
.item-ref--text:hover,
.item-ref--text:focus-visible {
  color: var(--el-color-primary);
  outline: none;
}
.item-ref--row {
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
.item-ref--row:hover,
.item-ref--row:focus-visible {
  background: var(--el-color-primary-light-9);
  outline: 1px solid var(--el-color-primary-light-5);
}
.item-ref--row img,
.item-ref--icon img {
  width: 32px;
  height: 32px;
}
.item-ref-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-ref img {
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
  pointer-events: none;
}
</style>
