<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  text: string
}>()

const lines = computed(() =>
  props.text
    ? props.text.split(/\r?\n/).map((l) => l.trim()).filter((l) => l.length > 0)
    : [],
)
</script>

<template>
  <div v-if="lines.length" class="notes">
    <div v-for="(line, idx) in lines" :key="idx" class="note-line">{{ line }}</div>
  </div>
</template>

<style scoped>
.notes {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 10px;
  background: var(--el-fill-color-light);
  border-left: 2px solid var(--el-border-color);
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
.note-line {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
