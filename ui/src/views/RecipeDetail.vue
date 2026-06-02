<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { storeToRefs } from 'pinia';
import { useDatasetStore } from '@/stores/dataset';
import { getRecipeDetail, listRecipeCategories } from '@/api/recipes';
import type { Recipe, RecipeCategory } from '@/api/recipes.types';
import RecipePanel from '@/components/RecipePanel.vue';

const route = useRoute();
const router = useRouter();
const datasetStore = useDatasetStore();
const { activeDatasetId } = storeToRefs(datasetStore);
const { t } = useI18n();

const datasetId = computed(() => String(route.params.datasetId ?? activeDatasetId.value ?? ''));
const recipeId = computed(() => String(route.params.recipeId ?? ''));

const recipe = ref<Recipe | null>(null);
const category = ref<RecipeCategory | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

async function fetchRecipe() {
  if (!datasetId.value || !recipeId.value) return;
  loading.value = true;
  error.value = null;
  try {
    const r = await getRecipeDetail(datasetId.value, recipeId.value);
    recipe.value = r;
    const categories = await listRecipeCategories(datasetId.value);
    category.value = categories.find((c) => c.categoryId === r.categoryId) ?? null;
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
    recipe.value = null;
    category.value = null;
  } finally {
    loading.value = false;
  }
}

function onSlotPick(payload: { itemVariantId: string | null; fluidVariantId: string | null }) {
  const tgt = payload.itemVariantId ?? payload.fluidVariantId;
  if (!tgt) return;
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: tgt, kind: 'detail' },
  });
}

function onSlotLookup(
  next: 'recipe' | 'usage',
  payload: { itemVariantId: string | null; fluidVariantId: string | null },
) {
  const tgt = payload.itemVariantId ?? payload.fluidVariantId;
  if (!tgt) return;
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: tgt, kind: next },
  });
}

function back() {
  router.back();
}

watch([datasetId, recipeId], fetchRecipe);
onMounted(fetchRecipe);
</script>

<template>
  <div class="recipe-detail">
    <header class="header">
      <el-button text @click="back">{{ t('common.back') }}</el-button>
      <span v-if="recipe" class="title">{{ recipe.categoryDisplayName }}</span>
    </header>

    <el-skeleton v-if="loading && !recipe" :rows="4" animated />
    <el-alert v-else-if="error" type="error" :title="error" :closable="false" show-icon />

    <template v-else-if="recipe">
      <section class="meta">
        <dl class="meta-list">
          <dt>{{ t('recipe.recipeId') }}</dt>
          <dd class="mono">{{ recipe.recipeId }}</dd>
          <dt>{{ t('recipe.sourcePlugin') }}</dt>
          <dd>{{ recipe.sourcePlugin || t('recipe.placeholder') }}</dd>
          <dt>{{ t('recipe.sourceRef') }}</dt>
          <dd class="mono">{{ recipe.sourceRef || t('recipe.placeholder') }}</dd>
          <template v-if="recipe.description">
            <dt>{{ t('recipe.description') }}</dt>
            <dd>{{ recipe.description }}</dd>
          </template>
        </dl>
      </section>

      <section class="panel">
        <RecipePanel
          :recipe="recipe"
          :category="category"
          @pick="onSlotPick"
          @lookup="onSlotLookup"
        />
      </section>
    </template>

    <el-empty v-else :description="t('recipe.notFound')" />
  </div>
</template>

<style scoped>
.recipe-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
}
.header {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 32px;
}
.header .title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 10px 14px;
  background: var(--el-bg-color);
}
.meta-list {
  display: grid;
  grid-template-columns: 96px 1fr;
  row-gap: 4px;
  column-gap: 12px;
  margin: 0;
}
.meta-list dt {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
.meta-list dd {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
  word-break: break-all;
}
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.panel {
  display: flex;
  justify-content: center;
}
</style>
