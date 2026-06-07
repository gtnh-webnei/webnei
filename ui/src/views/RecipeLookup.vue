<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { storeToRefs } from 'pinia';
import { useDatasetStore } from '@/stores/dataset';
import { useRecipeCategoryStore } from '@/stores/recipeCategories';
import { lookupBreakdown, lookupRecipes } from '@/api/recipes';
import { getItemDetail } from '@/api/items';
import { getFluidDetail } from '@/api/fluids';
import type { HandlerBreakdown, LookupKind, Recipe, RecipeCategory } from '@/api/recipes.types';
import RecipePanel from '@/components/RecipePanel.vue';
import ItemDetailPanel from '@/components/ItemDetailPanel.vue';
import FluidDetailPanel from '@/components/FluidDetailPanel.vue';
import CategoryHeaderRows from '@/components/recipe/CategoryHeaderRows.vue';
import { useEntityNavigation } from '@/composables/useEntityNavigation';

type Tab = 'detail' | 'recipe' | 'usage';

const route = useRoute();
const router = useRouter();
const datasetStore = useDatasetStore();
const recipeCategoryStore = useRecipeCategoryStore();
const { activeDatasetId } = storeToRefs(datasetStore);
const { t } = useI18n();

function parseTab(value: unknown): Tab {
  const v = String(value ?? 'detail');
  if (v === 'recipe' || v === 'usage' || v === 'detail') return v;
  return 'detail';
}

const datasetId = computed(() => String(route.params.datasetId ?? activeDatasetId.value ?? ''));
const target = computed(() => String(route.query.target ?? ''));
const entityNavigation = useEntityNavigation(router, datasetId);
const isFluid = computed(() => target.value.length > 0 && !target.value.includes('@'));
const tab = computed<Tab>(() => parseTab(route.query.kind));
const lookupTabs = computed<{ label: string; value: Tab }[]>(() => [
  { label: t('lookup.detailTab'), value: 'detail' },
  { label: t('lookup.recipeTab'), value: 'recipe' },
  { label: t('lookup.usageTab'), value: 'usage' },
]);

const recipes = ref<Recipe[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(12);
const loading = ref(false);
const error = ref<string | null>(null);

const targetName = ref<string | null>(null);
const targetIcon = ref<string | null>(null);

const categoryMap = ref<Map<string, RecipeCategory>>(new Map());
const categoryMapDatasetId = ref<string | null>(null);

const breakdown = ref<HandlerBreakdown[]>([]);
const breakdownLoading = ref(false);
const selectedCategoryId = ref<string | null>(null);
const selectedTier = ref<string | null>(null);

type CategoryTab = {
  handlerId: string;
  handlerDisplayName: string;
  categoryId: string;
  displayName: string;
  iconAssetUrl: string | null;
  count: number;
};

const categoryTabs = computed<CategoryTab[]>(() =>
  breakdown.value.flatMap((h) =>
    h.categories.map((c) => ({
      handlerId: h.handlerId,
      handlerDisplayName: h.displayName,
      categoryId: c.categoryId,
      displayName: c.displayName,
      iconAssetUrl: c.iconAssetUrl,
      count: c.count,
    })),
  ),
);

async function ensureCategories() {
  if (categoryMapDatasetId.value === datasetId.value && categoryMap.value.size > 0) return;
  try {
    const categories = await recipeCategoryStore.load(datasetId.value);
    categoryMap.value = new Map(categories.map((c) => [c.categoryId, c]));
    categoryMapDatasetId.value = datasetId.value;
  } catch (e) {
    console.warn('listRecipeCategories failed', e);
  }
}

async function fetchTargetMeta() {
  targetName.value = null;
  targetIcon.value = null;
  if (!target.value) return;
  if (isFluid.value) {
    try {
      const f = await getFluidDetail(datasetId.value, target.value);
      targetName.value = f.displayName || f.registryName;
      targetIcon.value = f.assetUrl;
      return;
    } catch {
      // fall through
    }
  }
  try {
    const detail = await getItemDetail(datasetId.value, target.value);
    targetName.value = detail.displayName || detail.registryName;
    targetIcon.value = detail.assetUrl;
  } catch {
    // ignore
  }
}

async function fetchBreakdown() {
  if (!datasetId.value || !target.value || tab.value === 'detail') {
    breakdown.value = [];
    return;
  }
  breakdownLoading.value = true;
  try {
    const data = await lookupBreakdown(datasetId.value, target.value, tab.value as LookupKind);
    breakdown.value = data;
    const flat = data.flatMap((h) => h.categories.map((c) => c.categoryId));
    if (flat.length === 0) {
      selectedCategoryId.value = null;
      selectedTier.value = null;
    } else if (!selectedCategoryId.value || !flat.includes(selectedCategoryId.value)) {
      selectedCategoryId.value = flat[0];
      selectedTier.value = null;
    }
  } catch (e) {
    breakdown.value = [];
    console.warn('lookupBreakdown failed', e);
  } finally {
    breakdownLoading.value = false;
  }
}

async function fetchRecipes() {
  if (!datasetId.value || !target.value || tab.value === 'detail') {
    recipes.value = [];
    total.value = 0;
    return;
  }
  if (!selectedCategoryId.value) {
    recipes.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    const [data] = await Promise.all([
      lookupRecipes(datasetId.value, target.value, tab.value, page.value - 1, pageSize.value, {
        categoryId: selectedCategoryId.value,
        voltageTier: selectedTier.value ?? undefined,
      }),
      ensureCategories(),
    ]);
    recipes.value = data.items;
    total.value = data.total;
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}

watch(tab, () => {
  page.value = 1;
  selectedCategoryId.value = null;
  selectedTier.value = null;
  fetchBreakdown();
  fetchRecipes();
});
watch(target, () => {
  page.value = 1;
  selectedCategoryId.value = null;
  selectedTier.value = null;
  fetchTargetMeta();
  fetchBreakdown();
  fetchRecipes();
});
watch(page, () => fetchRecipes());
watch(pageSize, () => {
  page.value = 1;
  fetchRecipes();
});
watch(selectedCategoryId, () => {
  page.value = 1;
  fetchRecipes();
});
watch(selectedTier, () => {
  page.value = 1;
  fetchRecipes();
});

function setTab(value: Tab) {
  if (value === tab.value) return;
  router.replace({ query: { ...route.query, kind: value } });
}

function selectCategory(categoryId: string) {
  if (selectedCategoryId.value === categoryId) return;
  selectedCategoryId.value = categoryId;
  selectedTier.value = null;
}

function onSlotPick(payload: { itemVariantId: string | null; fluidVariantId: string | null }) {
  entityNavigation.pick(payload, true);
}

function onSlotLookup(
  next: 'recipe' | 'usage',
  payload: { itemVariantId: string | null; fluidVariantId: string | null },
) {
  entityNavigation.lookup(next, payload);
}

onMounted(() => {
  fetchTargetMeta();
  fetchBreakdown();
  fetchRecipes();
});
</script>

<template>
  <div class="lookup">
    <header class="header">
      <div class="target">
        <img
          v-if="targetIcon"
          :src="targetIcon"
          class="target-icon"
          :alt="targetName ?? ''"
        >
        <div class="target-info">
          <div class="title">
            {{ targetName ?? target }}
          </div>
          <code class="target-id">{{ target }}</code>
        </div>
      </div>
      <nav
        class="page-tabs lookup-tabs"
        :aria-label="t('lookup.viewTabsLabel')"
      >
        <button
          v-for="item in lookupTabs"
          :key="item.value"
          type="button"
          class="page-tab"
          :class="{ active: tab === item.value }"
          @click="setTab(item.value)"
        >
          {{ item.label }}
        </button>
      </nav>
    </header>

    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
    />

    <template v-if="tab === 'detail'">
      <FluidDetailPanel
        v-if="isFluid"
        :dataset-id="datasetId"
        :fluid-variant-id="target"
      />
      <ItemDetailPanel
        v-else
        :dataset-id="datasetId"
        :item-variant-id="target"
      />
    </template>

    <template v-else>
      <div
        v-if="categoryTabs.length > 0"
        class="handler-tabs"
      >
        <button
          v-for="c in categoryTabs"
          :key="c.categoryId"
          type="button"
          class="handler-tab"
          :class="{ active: selectedCategoryId === c.categoryId }"
          :title="`${c.handlerDisplayName}\n${c.categoryId}`"
          @click="selectCategory(c.categoryId)"
        >
          <img
            v-if="c.iconAssetUrl"
            :src="c.iconAssetUrl"
            class="handler-icon"
            :alt="c.displayName"
          >
          <span class="handler-name">{{ c.displayName }}</span>
          <span class="handler-badge">{{ c.count }}</span>
        </button>
      </div>

      <CategoryHeaderRows
        v-if="selectedCategoryId"
        :dataset-id="datasetId"
        :category-id="selectedCategoryId"
        :active-tier="selectedTier"
        :hide-machines="true"
        :target="target"
        :kind="tab as LookupKind"
        @update:active-tier="selectedTier = $event"
      />

      <el-skeleton
        v-if="loading && recipes.length === 0"
        :rows="6"
        animated
      />
      <el-empty
        v-else-if="!loading && recipes.length === 0"
        :description="t('lookup.noMatch')"
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
          :category="categoryMap.get(r.categoryId) ?? null"
          :pick-hint="t('lookup.pickHint')"
          @pick="onSlotPick"
          @lookup="onSlotLookup"
        />
      </div>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[6, 12, 24, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </template>
  </div>
</template>

<style scoped>
.lookup {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  align-items: center;
  gap: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.lookup-tabs {
  min-width: 0;
  justify-content: end;
}
.target {
  display: flex;
  gap: 12px;
  align-items: center;
}
.target-icon {
  width: 48px;
  height: 48px;
  image-rendering: pixelated;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}
.target-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.target-id {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  word-break: break-all;
}
.handler-tabs {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 8px;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.handler-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color);
  background: var(--el-fill-color-blank);
  color: var(--el-text-color-regular);
  font-size: 13px;
  line-height: 1;
  cursor: pointer;
  transition:
    background-color 0.15s,
    border-color 0.15s,
    color 0.15s;
}
.handler-tab:hover {
  border-color: var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}
.handler-tab.active {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
.handler-icon {
  width: 18px;
  height: 18px;
  image-rendering: pixelated;
}
.handler-name {
  white-space: nowrap;
}
.handler-badge {
  min-width: 22px;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
  border-radius: 9px;
  background: var(--el-fill-color-darker);
  color: var(--el-text-color-secondary);
  font-size: 11px;
  text-align: center;
}
.handler-tab.active .handler-badge {
  background: var(--el-color-primary);
  color: var(--el-color-white);
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
@media (max-width: 760px) {
  .header {
    grid-template-columns: minmax(0, 1fr);
  }
  .lookup-tabs {
    justify-content: start;
  }
}
</style>
