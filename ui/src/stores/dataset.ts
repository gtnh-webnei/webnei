import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { listDatasets } from '@/api/datasets'
import type { DatasetSummary } from '@/api/types'
import { useExtrasStore } from '@/stores/extras'

const STORAGE_KEY = 'webnei.activeDatasetId'

export const useDatasetStore = defineStore('dataset', () => {
  const datasets = ref<DatasetSummary[]>([])
  const activeDatasetId = ref<string | null>(localStorage.getItem(STORAGE_KEY))
  const loading = ref(false)
  const error = ref<string | null>(null)

  const active = computed<DatasetSummary | null>(() => {
    if (!activeDatasetId.value) return null
    return datasets.value.find((d) => d.datasetId === activeDatasetId.value) ?? null
  })

  async function load() {
    loading.value = true
    error.value = null
    try {
      datasets.value = await listDatasets()
      if (datasets.value.length === 0) {
        activeDatasetId.value = null
        localStorage.removeItem(STORAGE_KEY)
        return
      }
      const stored = activeDatasetId.value
      const exists = stored && datasets.value.some((d) => d.datasetId === stored)
      if (!exists) {
        setActive(datasets.value[0].datasetId)
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  function setActive(datasetId: string) {
    const prev = activeDatasetId.value
    activeDatasetId.value = datasetId
    localStorage.setItem(STORAGE_KEY, datasetId)
    if (prev && prev !== datasetId) {
      useExtrasStore().clearDataset(prev)
    }
  }

  return { datasets, activeDatasetId, active, loading, error, load, setActive }
})
