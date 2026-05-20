<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDatasetStore } from '@/stores/dataset'
import { listRecipeCategoriesPage, listRecipeCategoryPlugins } from '@/api/recipes'
import type { RecipeCategory } from '@/api/recipes.types'
import { usePagedBrowser } from '@/composables/usePagedBrowser'
import BrowserToolbar from '@/components/BrowserToolbar.vue'
import CategoryCard from '@/components/CategoryCard.vue'

const HIDE_EMPTY_KEY = 'webnei.categoryGallery.hideEmpty'
const PAGE_SIZE_KEY = 'webnei.categoryGallery.pageSize'

const route = useRoute()
const router = useRouter()
const datasetStore = useDatasetStore()
const { activeDatasetId } = storeToRefs(datasetStore)

const datasetId = computed(() =>
  String(route.params.datasetId ?? activeDatasetId.value ?? ''),
)

const hideEmpty = ref<boolean>(localStorage.getItem(HIDE_EMPTY_KEY) !== '0')
watch(hideEmpty, (v) => localStorage.setItem(HIDE_EMPTY_KEY, v ? '1' : '0'))

const extras = computed(() => ({ hideEmpty: hideEmpty.value }))

const browser = usePagedBrowser<RecipeCategory, { hideEmpty: boolean }>({
  datasetId,
  fetcher: (id, params) =>
    listRecipeCategoriesPage(id, {
      q: params.q,
      plugin: params.secondary,
      hideEmpty: params.hideEmpty,
      page: params.page,
      size: params.size,
    }),
  optionsFetcher: listRecipeCategoryPlugins,
  extras,
  storageKey: PAGE_SIZE_KEY,
  defaultSize: 24,
})
const { q, secondary, page, pageSize, items, total, loading, error, secondaryOptions } = browser

function openCategory(c: RecipeCategory) {
  router.push({
    name: 'category',
    params: { datasetId: datasetId.value, categoryId: c.categoryId },
  })
}
</script>

<template>
  <div class="gallery">
    <header class="header">
      <h1>配方分类</h1>
      <p class="lead">浏览 NEI 注册的所有配方处理器</p>
    </header>

    <BrowserToolbar
      v-model:q="q"
      v-model:secondary="secondary"
      :secondary-options="secondaryOptions"
      placeholder="搜索分类名 / id / handler"
      secondary-placeholder="全部 Plugin"
      :total="items.length"
      total-label="显示"
      :total-suffix="`/ ${total}`"
    >
      <template #extra>
        <el-checkbox v-model="hideEmpty" border size="default">
          隐藏 0 配方
        </el-checkbox>
      </template>
    </BrowserToolbar>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading && items.length === 0" :rows="6" animated />
    <el-empty v-else-if="!loading && items.length === 0" description="没有匹配的分类" />

    <div v-else v-loading="loading" class="grid">
      <CategoryCard
        v-for="c in items"
        :key="c.categoryId"
        :category="c"
        @select="openCategory"
      />
    </div>

    <div class="pager">
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
.gallery {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.header h1 {
  margin: 0 0 4px 0;
  font-size: 22px;
}
.lead {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}
.pager {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
