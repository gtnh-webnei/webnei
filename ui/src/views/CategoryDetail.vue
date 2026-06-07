<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useDebounceFn } from '@vueuse/core';
import { useI18n } from 'vue-i18n';
import { useDatasetStore } from '@/stores/dataset';
import { useRecipeCategoryStore } from '@/stores/recipeCategories';
import { listRecipesByCategory } from '@/api/recipes';
import type { Recipe, RecipeCategory } from '@/api/recipes.types';
import RecipePanel from '@/components/RecipePanel.vue';
import CategoryHeaderRows from '@/components/recipe/CategoryHeaderRows.vue';
import { useEntityNavigation } from '@/composables/useEntityNavigation';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const datasetStore = useDatasetStore();
const recipeCategoryStore = useRecipeCategoryStore();
const { activeDatasetId } = storeToRefs(datasetStore);

const datasetId = computed(() => String(route.params.datasetId ?? activeDatasetId.value ?? ''));
const categoryId = computed(() => String(route.params.categoryId ?? ''));
const entityNavigation = useEntityNavigation(router, datasetId);

const category = ref<RecipeCategory | null>(null);
const recipes = ref<Recipe[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(12);
const loading = ref(false);
const error = ref<string | null>(null);
const q = ref('');
const tierQuery = ref('');
const activeTier = ref<string | null>(null);
let requestId = 0;

async function fetchCategoryMeta() {
  if (!datasetId.value || !categoryId.value) return;
  try {
    category.value = await recipeCategoryStore.findById(datasetId.value, categoryId.value);
  } catch {
    category.value = null;
  }
}

async function fetchRecipes() {
  if (!datasetId.value || !categoryId.value) return;
  const currentRequest = ++requestId;
  loading.value = true;
  error.value = null;
  try {
    const data = await listRecipesByCategory(
      datasetId.value,
      categoryId.value,
      q.value,
      page.value - 1,
      pageSize.value,
      { voltageTier: activeTier.value ?? undefined },
    );
    if (currentRequest !== requestId) return;
    recipes.value = data.items;
    total.value = data.total;
  } catch (e) {
    if (currentRequest !== requestId) return;
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    if (currentRequest === requestId) loading.value = false;
  }
}

const debouncedReset = useDebounceFn(() => {
  tierQuery.value = q.value;
  if (page.value !== 1) {
    page.value = 1;
  } else {
    fetchRecipes();
  }
}, 250);

watch([datasetId, categoryId], () => {
  page.value = 1;
  activeTier.value = null;
  tierQuery.value = q.value;
  fetchCategoryMeta();
  fetchRecipes();
});
watch(q, () => debouncedReset());
watch(activeTier, () => {
  if (page.value !== 1) {
    page.value = 1;
  } else {
    fetchRecipes();
  }
});
watch(page, () => fetchRecipes());
watch(pageSize, () => {
  if (page.value !== 1) {
    page.value = 1;
  } else {
    fetchRecipes();
  }
});

function onSlotPick(payload: { itemVariantId: string | null; fluidVariantId: string | null }) {
  entityNavigation.pick(payload);
}

function onSlotLookup(
  next: 'recipe' | 'usage',
  payload: { itemVariantId: string | null; fluidVariantId: string | null },
) {
  entityNavigation.lookup(next, payload);
}

function back() {
  router.back();
}

onMounted(() => {
  fetchCategoryMeta();
  fetchRecipes();
});
</script>

<template>
  <div class="category-detail">
    <header class="header">
      <el-button
        text
        @click="back"
      >
        {{ t('common.back') }}
      </el-button>
    </header>

    <section class="hero">
      <div class="icon-wrap">
        <img
          v-if="category?.iconAssetUrl"
          :src="category.iconAssetUrl"
          :alt="category.displayName"
        >
      </div>
      <div class="title-info">
        <h1>{{ category?.displayName ?? categoryId }}</h1>
        <div class="meta-row">
          <el-tag
            v-if="category"
            size="small"
            type="info"
            effect="plain"
            round
          >
            {{ category.modName }}
          </el-tag>
        </div>
      </div>
    </section>

    <CategoryHeaderRows
      :dataset-id="datasetId"
      :category-id="categoryId"
      :active-tier="activeTier"
      :query="tierQuery"
      @update:active-tier="activeTier = $event"
    />

    <div class="toolbar">
      <el-input
        v-model="q"
        :placeholder="t('category.categorySearchPlaceholder')"
        clearable
        class="search-input"
      />
      <div class="spacer" />
      <div class="total">
        {{ t('common.totalCount') }} <strong>{{ total.toLocaleString() }}</strong>
        {{ t('common.items') }}
      </div>
    </div>

    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
    />

    <el-skeleton
      v-if="loading && recipes.length === 0"
      :rows="6"
      animated
    />
    <el-empty
      v-else-if="!loading && recipes.length === 0"
      :description="t('category.noRecipes')"
    />

    <div
      v-else
      v-loading="loading"
      class="recipes"
    >
      <RecipePanel
        v-for="r in recipes"
        :key="r.recipeId"
        :recipe="r"
        :category="category"
        :pick-hint="t('common.pickHintCategory')"
        @pick="onSlotPick"
        @lookup="onSlotLookup"
      />
    </div>

    <div
      v-if="total > 0"
      class="pager"
    >
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 48, 96]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.category-detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.header {
  display: flex;
  align-items: center;
}
.hero {
  display: flex;
  align-items: center;
  gap: 12px;
}
.icon-wrap {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
}
.icon-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.title-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.title-info h1 {
  margin: 0;
  font-size: 20px;
  line-height: 1.2;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.search-input {
  width: 320px;
  max-width: 100%;
}
.spacer {
  flex: 1;
}
.total {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}
.total strong {
  color: var(--el-text-color-primary);
}
.recipes {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
  align-items: start;
}
.pager {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
