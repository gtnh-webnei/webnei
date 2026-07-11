import { ref, watch, type Ref } from 'vue'
import { watchDebounced } from '@vueuse/core'
import { pageSizeForViewport } from '@shared/utils/pageSize'
import { useRacedLoader } from '@shared/composables/useRacedLoader'
import type { PageResponse, SearchListParams } from '@shared/types'

export interface SearchListState<T> {
  query: Ref<string>
  page: Ref<number>
  total: Ref<number>
  items: Ref<T[]>
  loading: Ref<boolean>
  error: Ref<string | null>
  size: number
  onPageChange: (currentPage: number) => void
}

interface Options {
  debounce?: number
}

/**
 * 分页搜索列表逻辑：在 useRacedLoader（防抖+竞态+loading/error 内核）之上叠加分页。
 * 每页条数在初始化时按窗口宽度分档取一次，之后固定。
 * query 变更重置到首页；翻页/数据集切换触发重取；仅采纳最新一次请求的结果。
 */
export function useSearchList<T>(
  activeDatasetId: Ref<string | null>,
  fetcher: (params: SearchListParams) => Promise<PageResponse<T>>,
  options: Options = {},
): SearchListState<T> {
  const debounce = options.debounce ?? 250
  const size = pageSizeForViewport()

  const query = ref('')
  const page = ref(0)
  const total = ref(0)
  const items = ref<T[]>([]) as Ref<T[]>
  const { loading, error, cancel, run } = useRacedLoader()

  async function load() {
    const datasetId = activeDatasetId.value
    if (!datasetId) {
      cancel()
      items.value = []
      total.value = 0
      return
    }
    await run(
      () => fetcher({ datasetId, q: query.value, page: page.value, size }),
      (response) => {
        items.value = response.items
        total.value = response.total
      },
      () => {
        items.value = []
        total.value = 0
      },
    )
  }

  function resetAndLoad() {
    if (page.value === 0) {
      void load()
    } else {
      page.value = 0
    }
  }

  function onPageChange(currentPage: number) {
    page.value = Math.max(currentPage - 1, 0)
  }

  watchDebounced([activeDatasetId, query], resetAndLoad, { debounce, immediate: true })
  watch(page, () => void load())

  return { query, page, total, items, loading, error, size, onPageChange }
}
