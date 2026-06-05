<script setup lang="ts">
const popperOptions = {
  modifiers: [
    {
      name: 'preventOverflow',
      options: {
        boundary: 'viewport',
        padding: 12,
      },
    },
    {
      name: 'flip',
      options: {
        fallbackPlacements: ['bottom', 'top', 'right', 'left'],
        padding: 12,
      },
    },
  ],
};

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
    :popper-options="popperOptions"
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
  width: max-content;
  max-width: min(720px, calc(100vw - 24px));
}

:global(.webnei-tooltip .webnei-tooltip-content) {
  width: max-content;
  max-width: min(720px, calc(100vw - 24px));
  max-height: calc(100vh - 96px);
  overflow: visible;
  overflow-wrap: anywhere;
  white-space: pre-line;
}

:global(.webnei-tooltip .minecraft-tooltip-text) {
  max-height: calc(100vh - 160px);
}

:global(.webnei-tooltip .minecraft-tooltip-text .mc-title) {
  flex-shrink: 0;
}

:global(.webnei-tooltip .minecraft-tooltip-text .mc-body) {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  scrollbar-gutter: stable;
  padding-right: 8px;
}
</style>
