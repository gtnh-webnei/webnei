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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.mc-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.mc-line {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mc-color-0 {
  color: #000;
}
.mc-color-1 {
  color: #0000aa;
}
.mc-color-2 {
  color: #00aa00;
}
.mc-color-3 {
  color: #00aaaa;
}
.mc-color-4 {
  color: #aa0000;
}
.mc-color-5 {
  color: #aa00aa;
}
.mc-color-6 {
  color: #ffaa00;
}
.mc-color-7 {
  color: #aaaaaa;
}
.mc-color-8 {
  color: #555555;
}
.mc-color-9 {
  color: #5555ff;
}
.mc-color-a {
  color: #55ff55;
}
.mc-color-b {
  color: #55ffff;
}
.mc-color-c {
  color: #ff5555;
}
.mc-color-d {
  color: #ff55ff;
}
.mc-color-e {
  color: #ffff55;
}
.mc-color-f {
  color: #ffffff;
}
.mc-bold {
  font-weight: 700;
}
.mc-italic {
  font-style: italic;
}
.mc-underline {
  text-decoration: underline;
}
.mc-strikethrough {
  text-decoration: line-through;
}
</style>
