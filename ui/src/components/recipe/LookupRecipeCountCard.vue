<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import type { LookupKind } from '@/api/recipes.types';
import DetailSectionCard from '@/components/entity-detail/DetailSectionCard.vue';

const props = defineProps<{
  recipeCount: number;
  usageCount: number;
  activeKind: LookupKind | null;
}>();

const emit = defineEmits<{
  'select-kind': [kind: LookupKind];
}>();

const { t } = useI18n();
</script>

<template>
  <DetailSectionCard :title="t('lookup.recipeCountCardTitle')">
    <div class="counts">
      <button
        type="button"
        class="count-card"
        :class="{ active: props.activeKind === 'recipe' }"
        :disabled="props.recipeCount === 0"
        @click="emit('select-kind', 'recipe')"
      >
        <span class="count-label">{{ t('lookup.recipeCountRecipe') }}</span>
        <span class="count-value">{{ props.recipeCount }}</span>
        <span class="count-hint">{{ t('common.recipeArrow') }}</span>
      </button>
      <button
        type="button"
        class="count-card"
        :class="{ active: props.activeKind === 'usage' }"
        :disabled="props.usageCount === 0"
        @click="emit('select-kind', 'usage')"
      >
        <span class="count-label">{{ t('lookup.recipeCountUsage') }}</span>
        <span class="count-value">{{ props.usageCount }}</span>
        <span class="count-hint">{{ t('common.usageArrow') }}</span>
      </button>
    </div>
  </DetailSectionCard>
</template>

<style scoped>
.counts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.count-card {
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: pointer;
  text-align: left;
  font: inherit;
  color: inherit;
  transition:
    border-color 0.15s,
    background 0.15s;
}
.count-card:not(:disabled):hover,
.count-card.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.count-card:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.count-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
.count-value {
  font-size: 18px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.count-hint {
  font-size: 11px;
  color: var(--el-color-primary);
}
</style>
