<script setup lang="ts">
import type { ModListEntry } from '../types'

defineProps<{
  entry: ModListEntry
}>()
</script>

<template>
  <li class="mod-entry">
    <span class="mod-entry-head">
      <span class="mod-entry-name">{{ entry.name }}</span>
      <span
        v-if="entry.version"
        class="mod-entry-version"
      >{{ entry.version }}</span>
    </span>
    <span class="mod-entry-id">{{ entry.modId }}</span>
  </li>
</template>

<style scoped>
.mod-entry {
  display: grid;
  gap: 1px;
  min-width: 0;
  padding: 6px 0;
}

.mod-entry-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}

.mod-entry-name {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  color: var(--mc-panel-text);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mod-entry-version {
  flex: 0 0 auto;
  color: var(--app-surface-muted);
  font-size: 10px;
  font-weight: 600;
  line-height: 1.25;
  white-space: nowrap;
}

.mod-entry-id {
  min-width: 0;
  overflow: hidden;
  color: var(--mc-emerald-dark);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 10px;
  font-weight: 600;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
  opacity: 0.75;
}
</style>
