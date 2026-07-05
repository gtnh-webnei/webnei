<script setup lang="ts">
import { computed } from 'vue'
import type { CatalogKind, IconAsset } from '@/api/types'

interface IconAnimationMetadata {
  animation?: {
    type?: string
    layout?: string
    frameWidth?: number
    frameHeight?: number
    frameCount?: number
    tickMs?: number
  }
}

const props = defineProps<{
  kind: CatalogKind
  icon: IconAsset | null
  alt: string
  size: number
}>()

const animation = computed(() => {
  if (!props.icon?.metadataJson) return null
  try {
    const metadata = JSON.parse(props.icon.metadataJson) as IconAnimationMetadata
    const value = metadata.animation
    if (!value || value.type !== 'spritesheet' || value.layout !== 'horizontal') return null
    if (!value.frameCount || value.frameCount <= 1 || !value.tickMs || value.tickMs <= 0) return null
    return {
      frameCount: value.frameCount,
      durationMs: value.frameCount * value.tickMs,
    }
  } catch {
    return null
  }
})

const spriteStyle = computed(() => {
  if (!props.icon) return undefined
  const frameCount = animation.value?.frameCount ?? 1
  // 单帧显示尺寸 = 图标边长（由 size 决定）
  const frame = props.size
  const style: Record<string, string> = {
    backgroundImage: `url("${props.icon.url}")`,
    backgroundSize: `${frame * frameCount}px ${frame}px`,
    '--icon-shift': `-${frame * frameCount}px`,
  }
  if (animation.value) {
    style.animationDuration = `${animation.value.durationMs}ms`
    style.animationTimingFunction = `steps(${animation.value.frameCount})`
  }
  return style
})
</script>

<template>
  <span class="entry-icon">
    <img
      v-if="kind === 'item' && icon && !animation"
      class="entry-icon-image"
      :src="icon.url"
      :alt="alt"
      loading="lazy"
      decoding="async"
    >
    <span
      v-else-if="icon"
      class="entry-sprite-frame"
      :class="{ 'is-animated': animation }"
      :style="spriteStyle"
      role="img"
      :aria-label="alt"
    />
    <span
      v-else
      class="entry-icon-placeholder"
      aria-hidden="true"
    />
  </span>
</template>

<style scoped>
.entry-icon {
  display: inline-grid;
  place-items: center;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.entry-icon-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}

.entry-sprite-frame {
  display: block;
  width: 100%;
  height: 100%;
  background-position: 0 0;
  background-repeat: no-repeat;
  image-rendering: pixelated;
}

.entry-sprite-frame.is-animated {
  animation-name: icon-spritesheet;
  animation-iteration-count: infinite;
}

.entry-icon-placeholder {
  width: 24px;
  height: 24px;
  background: rgb(255 255 255 / 0.14);
}

@keyframes icon-spritesheet {
  to {
    background-position: var(--icon-shift) 0;
  }
}
</style>
