<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { useMediaQuery } from '@vueuse/core'
import type { Placement } from 'element-plus'
import { useTooltipKeyState } from '@shared/composables/useTooltipKeyState'
import type { McTooltipData } from '@shared/tooltip/types'
import McTooltipContent from './McTooltipContent.vue'

type TooltipTrigger = 'hover' | 'click' | 'focus' | 'contextmenu'
type TooltipInstance = { hide: (event?: Event) => void }

const TOOLTIP_VIEWPORT_GAP_PROPERTY = '--mc-tooltip-viewport-gap'
const tooltipViewportGap = Number.parseFloat(
  getComputedStyle(document.documentElement).getPropertyValue(TOOLTIP_VIEWPORT_GAP_PROPERTY),
)
const tooltipPopperOptions = {
  modifiers: [
    {
      name: 'preventOverflow',
      options: {
        boundary: 'viewport',
        rootBoundary: 'viewport',
        padding: tooltipViewportGap,
        mainAxis: true,
        altAxis: true,
        tether: true,
      },
    },
    {
      name: 'flip',
      options: {
        boundary: 'viewport',
        rootBoundary: 'viewport',
        padding: tooltipViewportGap,
      },
    },
  ],
}

const props = withDefaults(
  defineProps<{
    data: McTooltipData
    placement?: Placement
    trigger?: TooltipTrigger | TooltipTrigger[]
    disabled?: boolean
    focusable?: boolean
  }>(),
  {
    placement: 'bottom',
    trigger: () => ['hover', 'focus'],
    disabled: false,
    focusable: false,
  },
)

const COARSE_POINTER_QUERY = '(pointer: coarse)'
const isCoarsePointer = useMediaQuery(COARSE_POINTER_QUERY)
useTooltipKeyState()
const tooltipRef = ref<TooltipInstance>()
const effectiveTrigger = computed<TooltipTrigger | TooltipTrigger[]>(() => {
  if (!isCoarsePointer.value) return props.trigger
  return props.focusable ? ['click', 'focus'] : 'click'
})

function hideTooltip(event: KeyboardEvent) {
  tooltipRef.value?.hide(event)
}

function handleDocumentKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    hideTooltip(event)
  }
}

function handleShow() {
  document.addEventListener('keydown', handleDocumentKeydown)
}

function handleHide() {
  document.removeEventListener('keydown', handleDocumentKeydown)
}

onBeforeUnmount(handleHide)
</script>

<template>
  <el-tooltip
    ref="tooltipRef"
    :placement="placement"
    :trigger="effectiveTrigger"
    :disabled="disabled"
    popper-class="mc-tooltip mc-tooltip--panel"
    :popper-options="tooltipPopperOptions"
    :show-arrow="false"
    strategy="fixed"
    :enterable="true"
    @show="handleShow"
    @hide="handleHide"
  >
    <template #content>
      <McTooltipContent :data="data">
        <template #custom>
          <slot name="content" />
        </template>
      </McTooltipContent>
    </template>
    <span
      v-if="focusable"
      class="mc-tooltip-anchor"
      tabindex="0"
      @keydown.esc.stop="hideTooltip"
    >
      <slot />
    </span>
    <slot v-else />
  </el-tooltip>
</template>

<style scoped>
.mc-tooltip-anchor {
  display: block;
  width: 100%;
  height: 100%;
  min-width: 0;
}
</style>
