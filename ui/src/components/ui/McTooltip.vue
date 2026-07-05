<script setup lang="ts">
import type { Placement } from 'element-plus'

// 通用 MC tooltip：包 el-tooltip，套 mc-tooltip 皮肤（皮肤见 styles/element/popper.scss）。
withDefaults(
  defineProps<{
    placement?: Placement
    trigger?: 'hover' | 'click' | 'focus' | 'contextmenu'
    disabled?: boolean
  }>(),
  {
    placement: 'bottom',
    trigger: 'hover',
    disabled: false,
  },
)
</script>

<template>
  <el-tooltip
    :placement="placement"
    :trigger="trigger"
    :disabled="disabled"
    popper-class="mc-tooltip"
    :show-arrow="false"
  >
    <template #content>
      <slot name="content" />
    </template>
    <slot />
  </el-tooltip>
</template>
