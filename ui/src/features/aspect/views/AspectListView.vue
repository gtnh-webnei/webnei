<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useDatasetStore } from '@features/dataset/store'
import SearchListLayout from '@shared/components/SearchListLayout.vue'
import type { SearchHelpItem } from '@shared/components/SearchHelp.vue'
import { useRacedLoader } from '@shared/composables/useRacedLoader'
import AspectGrid from '../components/AspectGrid.vue'
import { listAspects } from '../api'
import type { AspectListEntry } from '../types'
import '../styles/aspect-theme.css'

const { t } = useI18n()
const { activeDatasetId } = storeToRefs(useDatasetStore())
const query = ref('')
const entries = ref<AspectListEntry[]>([])
const { loading, error, cancel, run } = useRacedLoader()

watch(
  activeDatasetId,
  (datasetId) => {
    cancel()
    entries.value = []
    if (!datasetId) return
    void run(
      () => listAspects(datasetId),
      (response) => {
        entries.value = response
      },
      () => {
        entries.value = []
      },
    )
  },
  { immediate: true },
)

const filteredEntries = computed(() => {
  const needle = query.value.trim().toLowerCase()
  if (!needle) return entries.value
  return entries.value.filter((entry) =>
    [entry.displayName, entry.description].some((value) => value.toLowerCase().includes(needle)),
  )
})

const hasDataset = computed(() => activeDatasetId.value != null)
const visibleError = computed(() => (error.value ? t('aspect.listLoadError') : null))
const helpItems: SearchHelpItem[] = [{ token: 'text', key: 'aspect.searchHelpDefault' }]
</script>

<template>
  <SearchListLayout
    v-model:query="query"
    class="aspect-research-shell"
    :title="t('aspect.title')"
    :help-items="helpItems"
    :has-dataset="hasDataset"
    :total="filteredEntries.length"
    :loading="loading"
    :error="visibleError"
    :has-items="filteredEntries.length > 0"
  >
    <AspectGrid :items="filteredEntries" />
  </SearchListLayout>
</template>
