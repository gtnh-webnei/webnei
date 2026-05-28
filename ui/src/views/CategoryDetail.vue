<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDebounceFn } from '@vueuse/core'
import { useDatasetStore } from '@/stores/dataset'
import { listRecipeCategories, listRecipesByCategory } from '@/api/recipes'
import type { Recipe, RecipeCategory } from '@/api/recipes.types'
import RecipePanel from '@/components/RecipePanel.vue'
import CategoryHeaderRows from '@/components/recipe/CategoryHeaderRows.vue'

const route = useRoute()
const router = useRouter()
const datasetStore = useDatasetStore()
const { activeDatasetId } = storeToRefs(datasetStore)

const datasetId = computed(() =>
  String(route.params.datasetId ?? activeDatasetId.value ?? ''),
)
const categoryId = computed(() => String(route.params.categoryId ?? ''))

const category = ref<RecipeCategory | null>(null)
const recipes = ref<Recipe[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(24)
const loading = ref(false)
const error = ref<string | null>(null)
const q = ref('')
const activeTier = ref<string | null>(null)

async function fetchCategoryMeta() {
  if (!datasetId.value || !categoryId.value) return
  try {
    const list = await listRecipeCategories(datasetId.value)
    category.value = list.find((c) => c.categoryId === categoryId.value) ?? null
  } catch {
    category.value = null
  }
}

async function fetchRecipes() {
  if (!datasetId.value || !categoryId.value) return
  loading.value = true
  error.value = null
  try {
    const data = await listRecipesByCategory(
      datasetId.value,
      categoryId.value,
      q.value,
      page.value - 1,
      pageSize.value,
      { voltageTier: activeTier.value ?? undefined },
    )
    recipes.value = data.items
    total.value = data.total
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

const debouncedReset = useDebounceFn(() => {
  page.value = 1
  fetchRecipes()
}, 250)

watch([datasetId, categoryId], () => {
  page.value = 1
  activeTier.value = null
  fetchCategoryMeta()
  fetchRecipes()
})
watch(q, () => debouncedReset())
watch(activeTier, () => {
  page.value = 1
  fetchRecipes()
})
watch(page, () => fetchRecipes())
watch(pageSize, () => {
  page.value = 1
  fetchRecipes()
})

function onSlotPick(payload: { itemVariantId: string | null; fluidVariantId: string | null }) {
  const tgt = payload.itemVariantId ?? payload.fluidVariantId
  if (!tgt) return
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: tgt, kind: 'detail' },
  })
}

function onSlotLookup(
  next: 'recipe' | 'usage',
  payload: { itemVariantId: string | null; fluidVariantId: string | null },
) {
  const tgt = payload.itemVariantId ?? payload.fluidVariantId
  if (!tgt) return
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: tgt, kind: next },
  })
}

function back() {
  router.back()
}

onMounted(() => {
  fetchCategoryMeta()
  fetchRecipes()
})
</script>

<template>
  <div class="category-detail">
    <header class="header">
      <el-button text @click="back">← 返回</el-button>
    </header>

    <section class="hero">
      <div class="icon-wrap">
        <img v-if="category?.iconAssetUrl" :src="category.iconAssetUrl" :alt="category.displayName" />
      </div>
      <div class="title-info">
        <h1>{{ category?.displayName ?? categoryId }}</h1>
        <div class="meta-row">
          <code class="cat-id">{{ categoryId }}</code>
          <el-tag v-if="category" size="small" type="info" effect="plain" round>
            {{ category.plugin }}
          </el-tag>
        </div>
      </div>
    </section>

    <CategoryHeaderRows
      :dataset-id="datasetId"
      :category-id="categoryId"
      :active-tier="activeTier"
      @update:active-tier="activeTier = $event"
    />

    <div class="toolbar">
      <el-input
        v-model="q"
        placeholder="搜索配方中的物品 / 流体名或 id"
        clearable
        class="search-input"
      />
      <div class="spacer" />
      <div class="total">
        共 <strong>{{ total.toLocaleString() }}</strong> 条
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />

    <el-skeleton v-if="loading && recipes.length === 0" :rows="6" animated />
    <el-empty v-else-if="!loading && recipes.length === 0" description="该分类下没有配方" />

    <div v-else v-loading="loading" class="recipes">
      <RecipePanel
        v-for="r in recipes"
        :key="r.recipeId"
        :recipe="r"
        :category="category"
        pick-hint="左键 · 物品详情 / 右键 · 合成来源 / 中键 · 用途去向"
        @pick="onSlotPick"
        @lookup="onSlotLookup"
      />
    </div>

    <div v-if="total > 0" class="pager">
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
.cat-id {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 11px;
  color: var(--el-text-color-secondary);
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
