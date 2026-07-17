<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTooltipKeyState } from '@shared/composables/useTooltipKeyState'
import { resolveMcTooltipLines } from '@shared/tooltip/resolveMcTooltipLines'
import type { McTooltipData } from '@shared/tooltip/types'
import McText from './McText.vue'

const props = defineProps<{
  data: McTooltipData
}>()

const { t } = useI18n()
const keyState = useTooltipKeyState()
const lines = computed(() => {
  if (props.data.kind === 'custom') return []
  return resolveMcTooltipLines(props.data, keyState.value, t)
})
</script>

<template>
  <div class="mc-tooltip-content mc-scroll-area">
    <slot
      v-if="data.kind === 'custom'"
      name="custom"
    />
    <template v-else>
      <div
        v-for="(line, index) in lines"
        :key="index"
        class="mc-tooltip-line"
        :class="[
          `is-${line.role}`,
          { 'has-section-gap': line.sectionGapAfter },
          { 'is-reserved': line.blank || line.customRenderer },
        ]"
        :aria-hidden="line.blank || line.customRenderer || undefined"
      >
        <McText
          v-if="!line.blank && !line.customRenderer"
          :text="line.text"
        />
      </div>
    </template>
  </div>
</template>

<style scoped>
.mc-tooltip-content {
  width: max-content;
  max-width: var(--mc-tooltip-max-width);
  max-height: var(--mc-tooltip-max-height);
  overflow: auto;
  padding: var(--mc-tooltip-padding);
  border: var(--mc-tooltip-border-width) solid transparent;
  border-radius: var(--mc-tooltip-border-radius);
  background:
    linear-gradient(var(--mc-tooltip-bg), var(--mc-tooltip-bg)) padding-box,
    linear-gradient(to bottom, var(--mc-tooltip-border-start), var(--mc-tooltip-border-end)) border-box;
  color: var(--mc-tooltip-text);
  font-size: var(--mc-tooltip-font-size);
  line-height: var(--mc-tooltip-line-height);
}

.mc-tooltip-line {
  min-height: var(--mc-tooltip-line-height);
  white-space: nowrap;
}

.mc-tooltip-line.has-section-gap {
  margin-bottom: var(--mc-tooltip-title-gap);
}

.mc-tooltip-line.is-reserved {
  height: var(--mc-tooltip-line-height);
}
</style>
