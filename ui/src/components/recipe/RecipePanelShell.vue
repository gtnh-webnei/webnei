<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { Recipe, RecipeCategory } from '@/api/recipes.types';

const props = defineProps<{
  recipe: Recipe;
  category?: RecipeCategory | null;
  declaredCols?: number | null;
  declaredRows?: number | null;
}>();

const { t } = useI18n();

const shapeless = computed(() => !!props.category?.shapeless);
const declaredOrdered = computed(
  () => !shapeless.value && (props.declaredCols ?? 0) > 0 && (props.declaredRows ?? 0) > 0,
);
const itemInputCount = computed(
  () => props.recipe.slots.filter((s) => s.role === 'item_input').length,
);
const descriptionLines = computed(() =>
  props.recipe.description
    ? props.recipe.description
        .split(/\r?\n/)
        .map((l) => l.trim())
        .filter((l) => l.length > 0)
    : [],
);
</script>

<template>
  <el-card class="recipe-panel" shadow="never">
    <template #header>
      <div class="header">
        <div class="title-block">
          <div class="title">{{ recipe.categoryDisplayName }}</div>
          <div class="sub">
            <el-tag size="small" type="info" effect="plain" round>
              {{ recipe.sourcePlugin }}
            </el-tag>
            <el-tag v-if="shapeless" size="small" type="warning" effect="dark" round>
              {{ t('recipe.shapeless')
              }}<span v-if="itemInputCount">
                · {{ itemInputCount }}{{ t('recipe.gridSlots') }}</span
              >
            </el-tag>
            <el-tag v-else-if="declaredOrdered" size="small" type="success" effect="plain" round>
              {{ t('recipe.shapedPrefix', { w: declaredCols, h: declaredRows }) }}
            </el-tag>
          </div>
        </div>
        <div class="header-tag">
          <slot name="header-tag" />
        </div>
      </div>
    </template>

    <div class="body">
      <div v-if="descriptionLines.length" class="description-block">
        <div v-for="(line, idx) in descriptionLines" :key="idx" class="description-line">
          {{ line }}
        </div>
      </div>

      <slot />

      <slot name="footer" />
    </div>
  </el-card>
</template>

<style scoped>
.recipe-panel {
  height: 100%;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.header-tag {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
}
.title-block {
  min-width: 0;
}
.title {
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sub {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
}
.recipe-id {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
  cursor: help;
}
.body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.description-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-left: 3px solid var(--el-color-primary);
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
}
.description-line {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
