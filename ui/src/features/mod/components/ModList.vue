<script setup lang="ts">
import { computed } from 'vue'
import ModRow from './ModRow.vue'
import type { ModListEntry } from '../types'

const props = defineProps<{
  items: ModListEntry[]
}>()

interface ModSection {
  letter: string
  mods: ModListEntry[]
}

function sectionKey(name: string): string {
  const first = name.trim().charAt(0).toUpperCase()
  return first >= 'A' && first <= 'Z' ? first : '#'
}

const sections = computed<ModSection[]>(() => {
  const grouped = new Map<string, ModListEntry[]>()
  for (const mod of props.items) {
    const key = sectionKey(mod.name)
    const bucket = grouped.get(key)
    if (bucket) bucket.push(mod)
    else grouped.set(key, [mod])
  }
  return [...grouped.entries()]
    .sort(([a], [b]) => {
      if (a === '#') return 1
      if (b === '#') return -1
      return a < b ? -1 : a > b ? 1 : 0
    })
    .map(([letter, mods]) => ({ letter, mods }))
})
</script>

<template>
  <div class="mod-directory">
    <section
      v-for="section in sections"
      :key="section.letter"
      class="mod-section"
    >
      <h3 class="mod-section-letter">
        {{ section.letter }}
      </h3>
      <ol class="mod-section-list">
        <ModRow
          v-for="mod in section.mods"
          :key="mod.id"
          :entry="mod"
        />
      </ol>
    </section>
  </div>
</template>

<style scoped>
.mod-directory {
  display: grid;
  gap: 14px;
  min-width: 0;
}

.mod-section {
  min-width: 0;
}

.mod-section-letter {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 6px;
  color: var(--mc-emerald-dark);
  font-size: 15px;
  font-weight: 900;
  letter-spacing: 0.08em;
}

.mod-section-letter::after {
  content: '';
  flex: 1 1 auto;
  height: 2px;
  background: rgb(49 45 37 / 0.18);
}

.mod-section-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 0 28px;
  min-width: 0;
  margin: 0;
  padding: 0;
  list-style: none;
}

.mod-section-list :deep(.mod-entry) {
  border-top: 1px solid rgb(49 45 37 / 0.1);
}
</style>
