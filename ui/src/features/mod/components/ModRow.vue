<script setup lang="ts">
import type { ModListEntry } from '../types'

const props = defineProps<{
  entry: ModListEntry
  selected: boolean
}>()

const emit = defineEmits<{
  select: [id: string]
}>()

function selectEntry() {
  emit('select', props.entry.id)
}
</script>

<template>
  <li class="mod-entry">
    <button
      type="button"
      class="mod-row"
      :class="{ 'is-selected': selected }"
      :aria-pressed="selected"
      @click="selectEntry"
    >
      <span class="mod-row-name">{{ entry.name }}</span>
      <span class="mod-row-version">{{ entry.version }}</span>
      <span class="mod-row-id">{{ entry.modId }}</span>
    </button>
  </li>
</template>

<style scoped>
.mod-entry {
  min-width: 0;
}

.mod-row {
  display: grid;
  grid-template-rows: repeat(3, var(--forge-slot-line-height));
  align-content: center;
  width: 100%;
  height: var(--forge-slot-height);
  min-width: 0;
  padding: 6px 3px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--forge-list-text);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.mod-row:hover,
.mod-row:focus-visible {
  border-color: var(--forge-list-selected-border);
}

.mod-row.is-selected {
  border-color: var(--forge-list-selected-border);
  background: var(--forge-list-selected-inner);
  box-shadow: 0 0 0 1px var(--forge-list-selected-border) inset;
}

.mod-row-name,
.mod-row-version,
.mod-row-id {
  min-width: 0;
  overflow: hidden;
  font-size: var(--forge-slot-font-size);
  font-weight: 700;
  line-height: var(--forge-slot-line-height);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mod-row-name {
  color: var(--forge-list-text);
}

.mod-row-version,
.mod-row-id {
  color: var(--forge-list-muted);
}
</style>
