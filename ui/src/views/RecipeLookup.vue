<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDatasetStore } from '@/stores/dataset'
import { listRecipeCategories, lookupBreakdown, lookupRecipes } from '@/api/recipes'
import { getItemDetail } from '@/api/items'
import { getFluidDetail } from '@/api/fluids'
import type { HandlerBreakdown, LookupKind, Recipe, RecipeCategory } from '@/api/recipes.types'
import RecipePanel from '@/components/RecipePanel.vue'
import ItemDetailPanel from '@/components/ItemDetailPanel.vue'
import FluidDetailPanel from '@/components/FluidDetailPanel.vue'

type Tab = 'detail' | 'recipe' | 'usage'

const route = useRoute()
const router = useRouter()
const datasetStore = useDatasetStore()
const { activeDatasetId } = storeToRefs(datasetStore)

const datasetId = computed(() =>
  String(route.params.datasetId ?? activeDatasetId.value ?? ''),
)
const target = computed(() => String(route.query.target ?? ''))
const isFluid = computed(() => target.value.length > 0 && !target.value.includes('@'))
const tab = computed<Tab>(() => {
  const v = String(route.query.kind ?? 'detail')
  if (v === 'recipe' || v === 'usage' || v === 'detail') return v
  return 'detail'
})

const recipes = ref<Recipe[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const loading = ref(false)
const error = ref<string | null>(null)

const targetName = ref<string | null>(null)
const targetIcon = ref<string | null>(null)

const categoryMap = ref<Map<string, RecipeCategory>>(new Map())
let categoriesPromise: Promise<void> | null = null

const breakdown = ref<HandlerBreakdown[]>([])
const breakdownLoading = ref(false)
const selectedHandlerId = ref<string | null>(null)
const selectedCategoryId = ref<string | null>(null)

const selectedHandler = computed(() =>
  breakdown.value.find((h) => h.handlerId === selectedHandlerId.value) ?? null,
)
const hasSubCategories = computed(
  () => (selectedHandler.value?.categories.length ?? 0) > 1,
)

function ensureCategories() {
  if (categoryMap.value.size > 0) return Promise.resolve()
  if (!categoriesPromise) {
    categoriesPromise = listRecipeCategories(datasetId.value)
      .then((list) => {
        categoryMap.value = new Map(list.map((c) => [c.categoryId, c]))
      })
      .catch((e) => {
        console.warn('listRecipeCategories failed', e)
      })
      .finally(() => {
        categoriesPromise = null
      })
  }
  return categoriesPromise
}

async function fetchTargetMeta() {
  targetName.value = null
  targetIcon.value = null
  if (!target.value) return
  if (isFluid.value) {
    try {
      const f = await getFluidDetail(datasetId.value, target.value)
      targetName.value = f.displayName || f.registryName
      targetIcon.value = f.assetUrl
      return
    } catch {
      // fall through
    }
  }
  try {
    const detail = await getItemDetail(datasetId.value, target.value)
    targetName.value = detail.displayName || detail.registryName
    targetIcon.value = detail.assetUrl
  } catch {
    // ignore
  }
}

async function fetchBreakdown() {
  if (!datasetId.value || !target.value || tab.value === 'detail') {
    breakdown.value = []
    return
  }
  breakdownLoading.value = true
  try {
    const data = await lookupBreakdown(
      datasetId.value,
      target.value,
      tab.value as LookupKind,
    )
    breakdown.value = data
    if (
      selectedHandlerId.value &&
      !data.some((h) => h.handlerId === selectedHandlerId.value)
    ) {
      selectedHandlerId.value = null
      selectedCategoryId.value = null
    }
  } catch (e) {
    breakdown.value = []
    console.warn('lookupBreakdown failed', e)
  } finally {
    breakdownLoading.value = false
  }
}

async function fetchRecipes() {
  if (!datasetId.value || !target.value || tab.value === 'detail') {
    recipes.value = []
    total.value = 0
    return
  }
  loading.value = true
  error.value = null
  try {
    const [data] = await Promise.all([
      lookupRecipes(
        datasetId.value,
        target.value,
        tab.value,
        page.value - 1,
        pageSize.value,
        {
          handlerId: selectedHandlerId.value ?? undefined,
          categoryId: selectedCategoryId.value ?? undefined,
        },
      ),
      ensureCategories(),
    ])
    recipes.value = data.items
    total.value = data.total
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

watch(tab, () => {
  page.value = 1
  selectedHandlerId.value = null
  selectedCategoryId.value = null
  fetchBreakdown()
  fetchRecipes()
})
watch(target, () => {
  page.value = 1
  selectedHandlerId.value = null
  selectedCategoryId.value = null
  fetchTargetMeta()
  fetchBreakdown()
  fetchRecipes()
})
watch(page, () => fetchRecipes())
watch(pageSize, () => {
  page.value = 1
  fetchRecipes()
})

function setTab(value: Tab) {
  if (value === tab.value) return
  router.replace({ query: { ...route.query, kind: value } })
}

function selectHandler(handlerId: string | null) {
  if (selectedHandlerId.value === handlerId) return
  selectedHandlerId.value = handlerId
  selectedCategoryId.value = null
  page.value = 1
  fetchRecipes()
}

function selectCategory(categoryId: string | null) {
  if (selectedCategoryId.value === categoryId) return
  selectedCategoryId.value = categoryId
  page.value = 1
  fetchRecipes()
}

const totalAcrossHandlers = computed(() =>
  breakdown.value.reduce((sum, h) => sum + h.count, 0),
)

function stripPluginPrefix(name: string): string {
  const idx = name.indexOf(' / ')
  return idx >= 0 ? name.slice(idx + 3) : name
}

function splitTrailingParens(name: string): { base: string; suffix: string | null } {
  const m = name.match(/^(.+?)\s*\(([^()]+)\)\s*$/)
  if (m) return { base: m[1].trim(), suffix: m[2].trim() }
  return { base: name, suffix: null }
}

function handlerLabel(displayName: string): string {
  return splitTrailingParens(stripPluginPrefix(displayName)).base
}

function chipLabel(displayName: string): string {
  const stripped = stripPluginPrefix(displayName)
  const { base, suffix } = splitTrailingParens(stripped)
  return suffix ?? base
}

function onSlotPick(payload: { itemVariantId: string | null; fluidVariantId: string | null }) {
  const tgt = payload.itemVariantId ?? payload.fluidVariantId
  if (!tgt) return
  router.replace({
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

onMounted(() => {
  fetchTargetMeta()
  fetchBreakdown()
  fetchRecipes()
})
</script>

<template>
  <div class="lookup">
    <header class="header">
      <div class="target">
        <img v-if="targetIcon" :src="targetIcon" class="target-icon" :alt="targetName ?? ''" />
        <div class="target-info">
          <div class="title">{{ targetName ?? target }}</div>
          <code class="target-id">{{ target }}</code>
        </div>
      </div>
      <div class="actions">
        <el-segmented
          :model-value="tab"
          :options="[
            { label: '详情', value: 'detail' },
            { label: '合成 (R)', value: 'recipe' },
            { label: '用途 (U)', value: 'usage' },
          ]"
          @update:model-value="setTab"
        />
      </div>
    </header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />

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
      <div v-if="breakdown.length > 0" class="handler-tabs">
        <button
          type="button"
          class="handler-tab"
          :class="{ active: selectedHandlerId === null }"
          @click="selectHandler(null)"
        >
          <span class="handler-name">全部</span>
          <span class="handler-badge">{{ totalAcrossHandlers }}</span>
        </button>
        <button
          v-for="h in breakdown"
          :key="h.handlerId"
          type="button"
          class="handler-tab"
          :class="{ active: selectedHandlerId === h.handlerId }"
          :title="h.handlerId"
          @click="selectHandler(h.handlerId)"
        >
          <img
            v-if="h.iconAssetUrl"
            :src="h.iconAssetUrl"
            class="handler-icon"
            :alt="h.displayName"
          />
          <span class="handler-name">{{ handlerLabel(h.displayName) }}</span>
          <span class="handler-badge">{{ h.count }}</span>
        </button>
      </div>

      <div v-if="selectedHandler && hasSubCategories" class="sub-chips">
        <button
          type="button"
          class="sub-chip"
          :class="{ active: selectedCategoryId === null }"
          @click="selectCategory(null)"
        >
          <span class="chip-name">全部</span>
          <span class="chip-badge">{{ selectedHandler.count }}</span>
        </button>
        <button
          v-for="c in selectedHandler.categories"
          :key="c.categoryId"
          type="button"
          class="sub-chip"
          :class="{ active: selectedCategoryId === c.categoryId }"
          :title="c.categoryId"
          @click="selectCategory(c.categoryId)"
        >
          <img
            v-if="c.iconAssetUrl"
            :src="c.iconAssetUrl"
            class="chip-icon"
            :alt="c.displayName"
          />
          <span class="chip-name">{{ chipLabel(c.displayName) }}</span>
          <span class="chip-badge">{{ c.count }}</span>
        </button>
      </div>

      <el-skeleton v-if="loading && recipes.length === 0" :rows="6" animated />
      <el-empty v-else-if="!loading && recipes.length === 0" description="没有匹配的配方" />

      <div v-else v-loading="loading" class="recipes">
        <RecipePanel
          v-for="r in recipes"
          :key="r.recipeId"
          :recipe="r"
          :category="categoryMap.get(r.categoryId) ?? null"
          pick-hint="左键 · 物品详情 / 右键 · 合成来源 / 中键 · 用途去向"
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
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
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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
  transition: background-color 0.15s, border-color 0.15s, color 0.15s;
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
.sub-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}
.sub-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px;
  border-radius: 14px;
  border: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-blank);
  color: var(--el-text-color-regular);
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
  transition: background-color 0.15s, border-color 0.15s, color 0.15s;
}
.sub-chip:hover {
  border-color: var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}
.sub-chip.active {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
.chip-icon {
  width: 14px;
  height: 14px;
  image-rendering: pixelated;
}
.chip-name {
  white-space: nowrap;
}
.chip-badge {
  min-width: 18px;
  padding: 0 5px;
  height: 16px;
  line-height: 16px;
  border-radius: 8px;
  background: var(--el-fill-color-darker);
  color: var(--el-text-color-secondary);
  font-size: 10px;
  text-align: center;
}
.sub-chip.active .chip-badge {
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
</style>
