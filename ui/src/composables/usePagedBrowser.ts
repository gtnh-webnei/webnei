import { computed, onMounted, ref, watch, type ComputedRef, type Ref } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import type { PageResponse } from '@/api/types'

export interface PagedBrowserOptions<T, E extends Record<string, unknown> = Record<string, never>> {
  datasetId: ComputedRef<string>
  fetcher: (datasetId: string, params: {
    q?: string
    secondary?: string
    page: number
    size: number
  } & E) => Promise<PageResponse<T>>
  optionsFetcher?: (datasetId: string) => Promise<string[]>
  extras?: ComputedRef<E>
  storageKey: string
  defaultSize: number
  debounceMs?: number
}

export interface PagedBrowserState<T> {
  q: Ref<string>
  secondary: Ref<string>
  page: Ref<number>
  pageSize: Ref<number>
  items: Ref<T[]>
  total: Ref<number>
  loading: Ref<boolean>
  error: Ref<string | null>
  secondaryOptions: Ref<string[]>
  refresh: () => Promise<void>
}

export function usePagedBrowser<T, E extends Record<string, unknown> = Record<string, never>>(
  opts: PagedBrowserOptions<T, E>,
): PagedBrowserState<T> {
  const { datasetId, fetcher, optionsFetcher, extras, storageKey, defaultSize, debounceMs = 250 } = opts

  const q = ref('')
  const secondary = ref('')
  const page = ref(1)
  const pageSize = ref<number>(Number(localStorage.getItem(storageKey)) || defaultSize)
  const items = ref<T[]>([]) as Ref<T[]>
  const total = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const secondaryOptions = ref<string[]>([])

  watch(pageSize, (v) => localStorage.setItem(storageKey, String(v)))

  async function refresh() {
    if (!datasetId.value) return
    loading.value = true
    error.value = null
    try {
      const extraParams = (extras?.value ?? {}) as E
      const data = await fetcher(datasetId.value, {
        ...extraParams,
        q: q.value || undefined,
        secondary: secondary.value || undefined,
        page: page.value - 1,
        size: pageSize.value,
      })
      items.value = data.items
      total.value = data.total
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  async function refreshOptions() {
    if (!optionsFetcher || !datasetId.value) return
    try {
      secondaryOptions.value = await optionsFetcher(datasetId.value)
    } catch (e) {
      console.warn('options fetch failed', e)
    }
  }

  const debouncedReset = useDebounceFn(() => {
    page.value = 1
    refresh()
  }, debounceMs)

  watch([q, secondary], () => debouncedReset())
  if (extras) {
    watch(extras, () => {
      page.value = 1
      refresh()
    }, { deep: true })
  }
  watch(pageSize, () => {
    page.value = 1
    refresh()
  })
  watch(page, () => refresh())
  watch(datasetId, (id, prev) => {
    if (id && id !== prev) {
      page.value = 1
      refreshOptions()
      refresh()
    }
  })

  onMounted(() => {
    refreshOptions()
    refresh()
  })

  return {
    q,
    secondary,
    page,
    pageSize,
    items,
    total,
    loading,
    error,
    secondaryOptions,
    refresh,
  }
}
