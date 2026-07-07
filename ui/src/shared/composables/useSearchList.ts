import { ref, watch, type Ref } from 'vue'
import { watchDebounced } from '@vueuse/core'
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
  size?: number
  debounce?: number
}

/**
 * 搜索 + 分页 + 竞态 + 防抖 的通用列表逻辑，与具体领域无关。
 * 传入活动数据集 id 的 ref 与 fetcher（按 SearchListParams 取一页数据），
 * 返回可直接绑定到布局的响应式状态。query 变更重置到首页；翻页与数据集切换触发重取；
 * 仅采纳最新一次请求的结果。
 */
export function useSearchList<T>(
  activeDatasetId: Ref<string | null>,
  fetcher: (params: SearchListParams) => Promise<PageResponse<T>>,
  options: Options = {},
): SearchListState<T> {
  const size = options.size ?? 60
  const debounce = options.debounce ?? 250

  const query = ref('')
  const page = ref(0)
  const total = ref(0)
  const items = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const error = ref<string | null>(null)
  let requestId = 0

  async function load() {
    const datasetId = activeDatasetId.value
    if (!datasetId) {
      items.value = []
      total.value = 0
      return
    }

    const currentRequest = ++requestId
    loading.value = true
    error.value = null
    try {
      const response = await fetcher({ datasetId, q: query.value, page: page.value, size })
      if (currentRequest !== requestId) return
      items.value = response.items
      total.value = response.total
    } catch (err) {
      if (currentRequest !== requestId) return
      error.value = err instanceof Error ? err.message : String(err)
      items.value = []
      total.value = 0
    } finally {
      if (currentRequest === requestId) {
        loading.value = false
      }
    }
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
