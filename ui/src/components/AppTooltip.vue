<script setup lang="ts">
withDefaults(
  defineProps<{
    content?: string | null;
    disabled?: boolean;
    placement?:
      | 'top'
      | 'top-start'
      | 'top-end'
      | 'bottom'
      | 'bottom-start'
      | 'bottom-end'
      | 'left'
      | 'right';
    showAfter?: number;
    hideAfter?: number;
  }>(),
  {
    content: null,
    disabled: false,
    placement: 'top',
    showAfter: 120,
    hideAfter: 80,
  },
);
</script>

<template>
  <el-tooltip
    :placement="placement"
    :show-after="showAfter"
    :hide-after="hideAfter"
    :disabled="disabled || (!content && !$slots.content)"
    effect="light"
    popper-class="webnei-tooltip"
  >
    <template #content>
      <div class="webnei-tooltip-content">
        <slot name="content">{{ content }}</slot>
      </div>
    </template>
    <slot />
  </el-tooltip>
</template>

<style scoped>
:global(.webnei-tooltip) {
  max-width: min(320px, calc(100vw - 32px));
}

:global(.webnei-tooltip .webnei-tooltip-content) {
  max-width: min(320px, calc(100vw - 32px));
  max-height: 220px;
  overflow: hidden;
  overflow-wrap: anywhere;
  white-space: pre-line;
}
</style>
