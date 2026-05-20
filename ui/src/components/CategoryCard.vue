<script setup lang="ts">
import type { RecipeCategory } from '@/api/recipes.types'

defineProps<{
  category: RecipeCategory
}>()

defineEmits<{
  (e: 'select', category: RecipeCategory): void
}>()
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
        <img v-if="category.iconAssetUrl" :src="category.iconAssetUrl" :alt="category.displayName" />
      </div>
      <div class="meta">
        <div class="name" :title="category.displayName">{{ category.displayName }}</div>
        <code class="cat-id" :title="category.categoryId">{{ category.categoryId }}</code>
        <div class="tags">
          <el-tag size="small" type="info" effect="plain" round>{{ category.plugin }}</el-tag>
          <el-tag size="small" effect="plain" round>
            {{ category.recipeCount.toLocaleString() }} 配方
          </el-tag>
          <el-tag
            v-if="category.machineCount > 0"
            size="small"
            type="warning"
            effect="plain"
            round
          >
            {{ category.machineCount }} 机器
          </el-tag>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.cat-card {
  min-height: 120px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.cat-card:hover {
  border-color: var(--el-color-primary);
}
.cat-card:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}
.card-body {
  display: flex;
  gap: 12px;
  align-items: flex-start;
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
.name {
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cat-id {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 4px;
}
</style>
