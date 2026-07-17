<script setup lang="ts">
import { computed } from 'vue'
import { useMcObfuscationTick } from '@shared/composables/useMcObfuscationTick'
import { parseMcFormat, type FormattedSegment } from '@shared/utils/mcFormat'

const OBFUSCATION_GLYPH_GROUPS = [
  'ABCDEFGHJKLMNOPQRSTUVWXYZabcdeghjmnopqrsuvwxyz0123456789?#$%&+-=',
  'It*',
  'fk',
  'i!',
  'l',
  '@',
] as const
const OBFUSCATION_GLYPHS_BY_CHARACTER = new Map(
  OBFUSCATION_GLYPH_GROUPS.flatMap((group) =>
    Array.from(group, (character) => [character, group] as const),
  ),
)
const TICK_SEED = 0x6d2b79f5
const SEGMENT_SEED = 0x9e3779b1

// 渲染带 Minecraft §格式码的文本。凡是可能含 §码的文字（物品名、tooltip 等）都用它。
const props = defineProps<{
  text: string
}>()

const segments = computed(() => parseMcFormat(props.text))
const hasAnimatedObfuscation = computed(() =>
  segments.value.some(
    (segment) =>
      segment.obfuscated &&
      Array.from(segment.text).some((character) =>
        OBFUSCATION_GLYPHS_BY_CHARACTER.has(character),
      ),
  ),
)
const { tick: obfuscationTick, prefersReducedMotion } = useMcObfuscationTick(
  hasAnimatedObfuscation,
)
const renderedSegments = computed(() => {
  if (prefersReducedMotion.value) return segments.value

  const tick = obfuscationTick.value
  return segments.value.map((segment, index) =>
    segment.obfuscated
      ? { ...segment, text: obfuscateText(segment.text, tick, index) }
      : segment,
  )
})

function obfuscateText(text: string, tick: number, segmentIndex: number): string {
  let state = Math.imul(tick + 1, TICK_SEED) ^ Math.imul(segmentIndex + 1, SEGMENT_SEED)

  return Array.from(text, (character) => {
    const glyphs = OBFUSCATION_GLYPHS_BY_CHARACTER.get(character)
    if (!glyphs) return character

    state ^= state << 13
    state ^= state >>> 17
    state ^= state << 5
    return glyphs[(state >>> 0) % glyphs.length]
  }).join('')
}

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
    v-for="(seg, index) in renderedSegments"
    :key="index"
    :style="segmentStyle(seg)"
  >{{ seg.text }}</span></span>
</template>

<style scoped>
.mc-text {
  /* 继承所在处的排版，仅按段落叠加 §码样式 */
  white-space: inherit;
}
</style>
