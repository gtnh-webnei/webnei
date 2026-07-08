import { ref, type Ref } from 'vue'
import { watchDebounced } from '@vueuse/core'
import { useRacedLoader } from '@shared/composables/useRacedLoader'

export interface SearchQueryState<T> {
  query: Ref<string>
  items: Ref<T[]>
  total: Ref<number>
  loading: Ref<boolean>
  error: Ref<string | null>
}

interface Options {
  debounce?: number
}

/**
 * 不分页的搜索列表逻辑：在 useRacedLoader 之上组合。
 * 一次性拉取当前数据集下的全部结果，query 变更防抖后重取；无分页概念。
 * 适用于条目量小、无需翻页的目录（如模组清单）。
 */
export function useSearchQuery<T>(
  activeDatasetId: Ref<string | null>,
  fetcher: (datasetId: string, q: string) => Promise<T[]>,
  options: Options = {},
): SearchQueryState<T> {
  const debounce = options.debounce ?? 250

  const query = ref('')
  const items = ref<T[]>([]) as Ref<T[]>
  const total = ref(0)
  const { loading, error, run } = useRacedLoader()

  async function load() {
    const datasetId = activeDatasetId.value
    if (!datasetId) {
      items.value = []
      total.value = 0
      return
    }
    await run(
      () => fetcher(datasetId, query.value),
      (response) => {
        items.value = response
        total.value = response.length
      },
      () => {
        items.value = []
        total.value = 0
      },
    )
  }

  watchDebounced([activeDatasetId, query], () => void load(), { debounce, immediate: true })

  return { query, items, total, loading, error }
}
