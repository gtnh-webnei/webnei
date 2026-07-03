import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchDefaultDatasetId, listDatasets } from '@/api/datasets'
import type { DatasetSummary } from '@/api/types'
import { browserLocale, setLocale } from '@/i18n'

const STORAGE_KEY = 'webnei.activeDatasetId'

function findById(datasets: DatasetSummary[], datasetId: string | null): DatasetSummary | null {
  if (!datasetId) return null
  return datasets.find((dataset) => dataset.datasetId === datasetId) ?? null
}

// 无存储时的默认解析：优先后端配置的默认 id，其次列表中语言最贴合浏览器的最新数据集。
function resolveDefault(datasets: DatasetSummary[], configuredId: string | null): DatasetSummary | null {
  const configured = findById(datasets, configuredId)
  if (configured) return configured

  const locale = browserLocale()
  return datasets.find((dataset) => dataset.language === locale) ?? datasets[0] ?? null
}

export const useDatasetStore = defineStore('dataset', () => {
  const datasets = ref<DatasetSummary[]>([])
  const activeDatasetId = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const loaded = ref(false)

  const activeDataset = computed(() => findById(datasets.value, activeDatasetId.value))

  function applyActiveDataset(dataset: DatasetSummary | null) {
    activeDatasetId.value = dataset?.datasetId ?? null
    setLocale(dataset?.language ?? browserLocale())
  }

  async function load() {
    if (loading.value) return
    loading.value = true
    error.value = null
    try {
      const [rows, configuredId] = await Promise.all([listDatasets(), fetchDefaultDatasetId()])
      datasets.value = rows

      const storedId = localStorage.getItem(STORAGE_KEY)
      const storedDataset = findById(rows, storedId)
      if (storedId && !storedDataset) {
        localStorage.removeItem(STORAGE_KEY)
      }

      applyActiveDataset(storedDataset ?? resolveDefault(rows, configuredId))
      loaded.value = true
    } catch (err) {
      error.value = err instanceof Error ? err.message : String(err)
      datasets.value = []
      applyActiveDataset(null)
    } finally {
      loading.value = false
    }
  }

  function selectDataset(datasetId: string) {
    const dataset = findById(datasets.value, datasetId)
    if (!dataset) return
    localStorage.setItem(STORAGE_KEY, dataset.datasetId)
    applyActiveDataset(dataset)
  }

  return { datasets, activeDatasetId, activeDataset, loading, error, loaded, load, selectDataset }
})
