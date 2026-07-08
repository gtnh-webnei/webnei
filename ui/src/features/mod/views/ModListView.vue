<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useDatasetStore } from '@features/dataset/store'
import { useSearchQuery } from '@shared/composables/useSearchQuery'
import SearchListLayout from '@shared/components/SearchListLayout.vue'
import type { SearchHelpItem } from '@shared/components/SearchHelp.vue'
import ModList from '../components/ModList.vue'
import { listMods } from '../api'

const { t } = useI18n()
const { activeDatasetId } = storeToRefs(useDatasetStore())

const state = useSearchQuery(activeDatasetId, listMods)

const hasDataset = computed(() => activeDatasetId.value != null)

// 模组搜索仅按名称与 ID 匹配（页面可见字段）。
const helpItems: SearchHelpItem[] = [{ token: 'text', key: 'catalog.searchHelpModDefault' }]
</script>

<template>
  <SearchListLayout
    v-model:query="state.query.value"
    :title="t('catalog.mods')"
    :help-items="helpItems"
    :has-dataset="hasDataset"
    :total="state.total.value"
    :loading="state.loading.value"
    :error="state.error.value"
    :has-items="state.items.value.length > 0"
  >
    <ModList :items="state.items.value" />
  </SearchListLayout>
</template>
