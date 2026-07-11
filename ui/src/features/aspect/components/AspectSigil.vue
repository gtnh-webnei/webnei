<script setup lang="ts">
import { computed, type CSSProperties } from 'vue'
import EntryIcon from '@shared/components/EntryIcon.vue'
import type { IconAsset } from '@shared/types'

const props = withDefaults(
  defineProps<{
    icon: IconAsset | null
    name: string
    color: number
    size?: number
  }>(),
  { size: 64 },
)

const aspectColor = computed(() => `#${(props.color & 0xffffff).toString(16).padStart(6, '0')}`)
const sigilStyle = computed<CSSProperties>(() => ({
  '--aspect-color': aspectColor.value,
  '--aspect-sigil-size': `${props.size}px`,
}))
</script>

<template>
  <span
    class="aspect-sigil"
    :style="sigilStyle"
  >
    <span
      class="aspect-sigil-ring"
      aria-hidden="true"
    />
    <span class="aspect-sigil-icon">
      <EntryIcon
        kind="aspect"
        :icon="icon"
        :alt="name"
        :size="size"
      />
    </span>
  </span>
</template>
