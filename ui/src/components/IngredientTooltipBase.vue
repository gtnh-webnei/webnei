<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import MinecraftTooltipText from './MinecraftTooltipText.vue';

export interface IngredientTooltipIngredient {
  displayName: string;
  tooltipText?: string | null;
  modName?: string | null;
}

export interface IngredientTooltipContext {
  hint?: string | null;
  amountLabel?: string | null;
  probabilityLabel?: string | null;
}

const { t } = useI18n();

withDefaults(
  defineProps<{
    ingredient: IngredientTooltipIngredient;
    context?: IngredientTooltipContext;
    variant?: 'item' | 'fluid';
  }>(),
  {
    context: () => ({}),
    variant: 'item',
  },
);
</script>

<template>
  <div
    class="ingredient-tooltip-content"
    :class="`ingredient-tooltip-content--${variant}`"
  >
    <MinecraftTooltipText
      v-if="ingredient.tooltipText"
      :text="ingredient.tooltipText"
    />
    <div
      v-else
      class="ingredient-tooltip-fallback"
    >
      <div class="ingredient-tooltip-name">
        {{ ingredient.displayName }}
      </div>
      <slot />
      <div
        v-if="ingredient.modName"
        class="ingredient-tooltip-mod"
      >
        {{ ingredient.modName }}
      </div>
    </div>
    <dl
      v-if="context.amountLabel || context.probabilityLabel"
      class="ingredient-tooltip-meta"
    >
      <div
        v-if="context.amountLabel"
        class="meta-chip"
      >
        <dt>{{ t('common.amount') }}</dt>
        <dd>{{ context.amountLabel }}</dd>
      </div>
      <div
        v-if="context.probabilityLabel"
        class="meta-chip probability"
      >
        <dt>{{ t('common.probability') }}</dt>
        <dd>{{ context.probabilityLabel }}</dd>
      </div>
    </dl>
    <div
      v-if="context.hint"
      class="ingredient-tooltip-keys"
    >
      {{ context.hint }}
    </div>
  </div>
</template>

<style scoped>
.ingredient-tooltip-content {
  display: grid;
  gap: 6px;
  max-width: min(520px, calc(100vw - 32px));
}
.ingredient-tooltip-fallback {
  display: grid;
  gap: 6px;
  min-width: 0;
}
.ingredient-tooltip-name {
  font-size: 14px;
  font-weight: 600;
  word-break: break-word;
}
.ingredient-tooltip-keys {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.ingredient-tooltip-meta {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 6px;
  align-items: center;
  margin: 0;
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 6px;
}
.meta-chip {
  display: grid;
  grid-template-columns: max-content max-content;
  gap: 4px;
  align-items: baseline;
  min-width: 0;
  border-radius: 4px;
  background: var(--el-fill-color-light);
  padding: 3px 6px;
  font-size: 12px;
  line-height: 1.2;
}
.meta-chip dt {
  color: var(--el-text-color-secondary);
}
.meta-chip dd {
  margin: 0;
  color: var(--el-color-primary);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.meta-chip.probability dd {
  color: var(--el-color-danger);
}
.ingredient-tooltip-content--fluid .ingredient-tooltip-fallback {
  font-family: 'WebNEI GTNH Glyphs', Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.45;
}
.ingredient-tooltip-content--fluid .ingredient-tooltip-name {
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding-bottom: 6px;
}
.ingredient-tooltip-mod {
  color: #5555ff;
  font-size: 12px;
  font-style: italic;
  white-space: nowrap;
}
</style>
