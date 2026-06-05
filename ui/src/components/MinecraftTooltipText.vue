<script setup lang="ts">
import { computed } from 'vue';
import { parseMinecraftFormatting } from '@/utils/minecraftFormatting';

const props = defineProps<{
  text: string;
}>();

const rawLines = computed(() => props.text.split('\n').filter((line) => line.length > 0));
const titleLine = computed(() =>
  rawLines.value[0] ? parseMinecraftFormatting(rawLines.value[0]) : [],
);
const bodyLines = computed(() => rawLines.value.slice(1).map(parseMinecraftFormatting));
</script>

<template>
  <div class="minecraft-tooltip-text">
    <div v-if="titleLine.length" class="mc-title">
      <span
        v-for="(segment, segmentIndex) in titleLine"
        :key="segmentIndex"
        :class="segment.classes"
      >
        {{ segment.text }}
      </span>
    </div>
    <div class="mc-body">
      <div v-for="(line, lineIndex) in bodyLines" :key="lineIndex" class="mc-line">
        <span v-for="(segment, segmentIndex) in line" :key="segmentIndex" :class="segment.classes">
          {{ segment.text }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.minecraft-tooltip-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  line-height: 1.35;
}
.mc-title {
  min-width: 0;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
  font-weight: 600;
  padding-top: 4px;
  padding-bottom: 5px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.mc-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  margin-top: 4px;
}
.mc-line {
  min-width: 0;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}
.mc-color-0 {
  color: #111827;
}
.mc-color-1 {
  color: #1d4ed8;
}
.mc-color-2 {
  color: #15803d;
}
.mc-color-3 {
  color: #0f766e;
}
.mc-color-4 {
  color: #b91c1c;
}
.mc-color-5 {
  color: #a21caf;
}
.mc-color-6 {
  color: #c2410c;
}
.mc-color-7 {
  color: #6b7280;
}
.mc-color-8 {
  color: #4b5563;
}
.mc-color-9 {
  color: #2563eb;
}
.mc-color-a {
  color: #16a34a;
}
.mc-color-b {
  color: #0891b2;
}
.mc-color-c {
  color: #dc2626;
}
.mc-color-d {
  color: #c026d3;
}
.mc-color-e {
  color: #ca8a04;
}
.mc-color-f {
  color: #111827;
}
html.dark .mc-color-0 {
  color: #000000;
}
html.dark .mc-color-1 {
  color: #0000aa;
}
html.dark .mc-color-2 {
  color: #00aa00;
}
html.dark .mc-color-3 {
  color: #00aaaa;
}
html.dark .mc-color-4 {
  color: #aa0000;
}
html.dark .mc-color-5 {
  color: #aa00aa;
}
html.dark .mc-color-6 {
  color: #ffaa00;
}
html.dark .mc-color-7 {
  color: #aaaaaa;
}
html.dark .mc-color-8 {
  color: #555555;
}
html.dark .mc-color-9 {
  color: #5555ff;
}
html.dark .mc-color-a {
  color: #55ff55;
}
html.dark .mc-color-b {
  color: #55ffff;
}
html.dark .mc-color-c {
  color: #ff5555;
}
html.dark .mc-color-d {
  color: #ff55ff;
}
html.dark .mc-color-e {
  color: #ffff55;
}
html.dark .mc-color-f {
  color: #ffffff;
}
.mc-bold {
  font-weight: 700;
}
.mc-italic {
  font-style: italic;
}
.mc-underline {
  text-decoration-line: underline;
}
.mc-strikethrough {
  text-decoration-line: line-through;
}
.mc-underline.mc-strikethrough {
  text-decoration-line: underline line-through;
}
</style>
