<script setup lang="ts">
import { computed } from 'vue'
import { parseMcFormat, type FormattedSegment } from '@shared/utils/mcFormat'

// 渲染带 Minecraft §格式码的文本。凡是可能含 §码的文字（物品名、tooltip 等）都用它。
const props = defineProps<{
  text: string
}>()

const segments = computed(() => parseMcFormat(props.text))

function segmentStyle(seg: FormattedSegment) {
  const decoration: string[] = []
  if (seg.underline) decoration.push('underline')
  if (seg.strikethrough) decoration.push('line-through')
  return {
    color: seg.color ?? undefined,
    fontWeight: seg.bold ? '700' : undefined,
    fontStyle: seg.italic ? 'italic' : undefined,
    textDecorationLine: decoration.length ? decoration.join(' ') : undefined,
  }
}
</script>

<template>
  <span class="mc-text"><span
    v-for="(seg, index) in segments"
    :key="index"
    :style="segmentStyle(seg)"
    :class="{ 'is-obfuscated': seg.obfuscated }"
  >{{ seg.text }}</span></span>
</template>

<style scoped>
.mc-text {
  /* 继承所在处的排版，仅按段落叠加 §码样式 */
  white-space: inherit;
}

.is-obfuscated {
  /* 混淆码：MC 里是逐帧随机字符。此处先做视觉近似（轻微闪烁），不逐字随机 */
  animation: mc-obfuscate 0.6s steps(1) infinite;
}

@keyframes mc-obfuscate {
  50% {
    opacity: 0.55;
  }
}
</style>
