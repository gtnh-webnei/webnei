<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useDatasetStore } from '@features/dataset/store'
import { useSearchList } from '@shared/composables/useSearchList'
import SearchListLayout from '@shared/components/SearchListLayout.vue'
import type { SearchHelpItem } from '@shared/components/SearchHelp.vue'
import RecipeCategoryGrid from '../components/categories/RecipeCategoryGrid.vue'
import { listRecipeCategories } from '../api'

const { t } = useI18n()
const { activeDatasetId } = storeToRefs(useDatasetStore())

const state = useSearchList(activeDatasetId, listRecipeCategories)

const hasDataset = computed(() => activeDatasetId.value != null)

// 配方分类默认搜索 = 名称+模组聚合，@ 单独搜模组。默认帮助文案用配方分类专属，不复用物品/流体。
const helpItems: SearchHelpItem[] = [
  { token: 'text', key: 'catalog.searchHelpRecipeCategoryDefault' },
  { token: '@text', key: 'catalog.searchHelpMod' },
  { token: '|', key: 'catalog.searchHelpOr' },
  { token: '-text / !text', key: 'catalog.searchHelpNegate' },
  { token: '"text"', key: 'catalog.searchHelpQuote' },
]
</script>

<template>
  <SearchListLayout
    v-model:query="state.query.value"
    :title="t('catalog.recipeCategories')"
    :help-items="helpItems"
    :has-dataset="hasDataset"
    :total="state.total.value"
    :loading="state.loading.value"
    :error="state.error.value"
    :has-items="state.items.value.length > 0"
    :page="state.page.value"
    :size="state.size"
    :on-page-change="state.onPageChange"
  >
    <RecipeCategoryGrid :items="state.items.value" />
  </SearchListLayout>
</template>
