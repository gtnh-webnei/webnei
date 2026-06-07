<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import type { RecipeCategory } from '@/api/recipes.types';

const { t } = useI18n();

defineProps<{
  category: RecipeCategory;
}>();

defineEmits<{
  (e: 'select', category: RecipeCategory): void;
}>();
</script>

<template>
  <el-card
    shadow="hover"
    class="cat-card"
    role="button"
    tabindex="0"
    @click="$emit('select', category)"
    @keydown.enter.prevent="$emit('select', category)"
    @keydown.space.prevent="$emit('select', category)"
  >
    <div class="card-body">
      <div class="icon-wrap">
        <img
          v-if="category.iconAssetUrl"
          :src="category.iconAssetUrl"
          :alt="category.displayName"
        >
      </div>
      <div class="meta">
        <div class="name-row">
          <div
            class="name"
            :title="category.displayName"
          >
            {{ category.displayName }}
          </div>
          <div class="stats">
            <el-tag
              size="small"
              effect="plain"
              round
              class="stat-tag"
            >
              {{ t('category.recipeCount', { count: category.recipeCount.toLocaleString() }) }}
            </el-tag>
            <el-tag
              v-if="category.machineCount > 0"
              size="small"
              type="warning"
              effect="plain"
              round
              class="stat-tag"
            >
              {{ t('category.machineCount', { count: category.machineCount }) }}
            </el-tag>
          </div>
        </div>
        <div class="mod-row">
          <el-tag
            size="small"
            type="info"
            effect="plain"
            round
            class="mod-tag"
          >
            {{ category.modName }}
          </el-tag>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.cat-card {
  cursor: pointer;
  transition:
    border-color 0.15s,
    background 0.15s;
}
.cat-card :deep(.el-card__body) {
  min-height: 88px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  padding: 14px 16px;
}
.cat-card:hover {
  border-color: var(--el-color-primary);
}
.cat-card:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}
.card-body {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  width: 100%;
}
.icon-wrap {
  width: 48px;
  height: 48px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.icon-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.name-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.name {
  min-width: 0;
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.stats {
  display: inline-grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  align-items: center;
  gap: 4px;
}
.stat-tag {
  flex-shrink: 0;
}
.mod-row {
  display: flex;
  min-width: 0;
  margin-top: 4px;
}
.mod-tag {
  max-width: 100%;
  min-width: 0;
  align-items: center;
}
.mod-tag :deep(.el-tag__content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.25;
  padding-bottom: 1px;
}
</style>
