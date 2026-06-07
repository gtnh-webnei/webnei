<script setup lang="ts">
import { computed } from 'vue';
import type { NeiPanelEntry } from '@/api/items.types';

const props = withDefaults(
  defineProps<{
    item: NeiPanelEntry | null;
    size?: number;
    interactive?: boolean;
  }>(),
  {
    size: 32,
    interactive: true,
  },
);

const emit = defineEmits<{
  (e: 'click', item: NeiPanelEntry): void;
  (e: 'lookup', kind: 'recipe' | 'usage', item: NeiPanelEntry): void;
}>();

const sizeStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
}));

function onClick() {
  if (!props.interactive || !props.item) return;
  emit('click', props.item);
}

function onContextMenu(e: MouseEvent) {
  if (!props.interactive || !props.item) return;
  e.preventDefault();
  emit('lookup', 'recipe', props.item);
}

function onAuxClick(e: MouseEvent) {
  if (!props.interactive || !props.item) return;
  // middle click = usage
  if (e.button === 1) {
    e.preventDefault();
    emit('lookup', 'usage', props.item);
  }
}
</script>

<template>
  <div
    v-if="item"
    class="item-icon"
    :class="{ interactive }"
    :style="sizeStyle"
    role="button"
    :tabindex="interactive ? 0 : -1"
    @click="onClick"
    @contextmenu="onContextMenu"
    @auxclick="onAuxClick"
  >
    <img
      v-if="item.assetUrl"
      :src="item.assetUrl"
      :alt="item.displayName"
      loading="lazy"
      decoding="async"
      draggable="false"
    >
    <div
      v-else
      class="placeholder"
    >
      ?
    </div>
  </div>
  <div
    v-else
    class="item-icon empty"
    :style="sizeStyle"
  />
</template>

<style scoped>
.item-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  border: 1px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}
.item-icon.interactive {
  cursor: pointer;
}
.item-icon.interactive:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.item-icon.interactive:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}
.item-icon.empty {
  background: var(--el-fill-color);
  opacity: 0.4;
}
img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.placeholder {
  font-size: 0.7em;
  color: var(--el-text-color-secondary);
}
</style>
