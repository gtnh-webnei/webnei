<script setup lang="ts">
import type { NeiPanelEntry } from '@/api/items.types';
import ItemIcon from './ItemIcon.vue';

const props = defineProps<{
  item: NeiPanelEntry;
  selected?: boolean;
}>();

defineEmits<{
  (e: 'select', item: NeiPanelEntry): void;
}>();
</script>

<template>
  <div
    class="item-card"
    :class="{ selected }"
    tabindex="0"
    role="button"
    @click="$emit('select', item)"
    @keydown.enter="$emit('select', item)"
  >
    <ItemIcon
      :item="item"
      :size="40"
      :interactive="false"
    />
    <div class="meta">
      <div class="name">
        {{ item.displayName || item.registryName }}
      </div>
      <div class="sub">
        <el-tag
          size="small"
          type="info"
          effect="plain"
          round
          class="mod-tag"
        >
          {{ item.modName }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.item-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-bg-color);
  cursor: pointer;
  transition:
    border-color 0.15s,
    background 0.15s,
    box-shadow 0.15s;
  min-width: 0;
}
.item-card:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.item-card.selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px var(--el-color-primary);
}
.item-card:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}
.meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.name {
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sub {
  display: flex;
  align-items: center;
  min-width: 0;
}
.mod-tag {
  max-width: 100%;
  min-width: 0;
  align-items: center;
}
.mod-tag :deep(.el-tag__content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.25;
  padding-bottom: 1px;
}
.id {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}
</style>
