<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useDatasetStore } from '@features/dataset/store'
import { useSearchList } from '@shared/composables/useSearchList'
import SearchListLayout from '@shared/components/SearchListLayout.vue'
import EntryGrid from '@shared/components/EntryGrid.vue'
import type { SearchHelpItem } from '@shared/components/SearchHelp.vue'
import { listFluids } from '../api'

const { t } = useI18n()
const { activeDatasetId } = storeToRefs(useDatasetStore())

const state = useSearchList(activeDatasetId, listFluids)

const hasDataset = computed(() => activeDatasetId.value != null)

// 流体没有矿典/提示搜索，仅名称、模组、ID。
const helpItems: SearchHelpItem[] = [
  { token: 'text', key: 'catalog.searchHelpFluidDefault' },
  { token: '@text', key: 'catalog.searchHelpMod' },
  { token: '&text', key: 'catalog.searchHelpIdentifier' },
  { token: '|', key: 'catalog.searchHelpOr' },
  { token: '-text / !text', key: 'catalog.searchHelpNegate' },
  { token: '"text"', key: 'catalog.searchHelpQuote' },
]
</script>

<template>
  <SearchListLayout
    v-model:query="state.query.value"
    :title="t('catalog.fluids')"
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
    <EntryGrid
      kind="fluid"
      :items="state.items.value"
    />
  </SearchListLayout>
</template>
