import { computed, onMounted, ref, watch, type ComputedRef, type Ref } from 'vue'

export interface FilteredListOptions<T> {
  datasetId: ComputedRef<string>
  fetcher: (datasetId: string) => Promise<T[]>
  searchFields: (item: T) => string[]
  secondaryField?: (item: T) => string
}

export interface FilteredListState<T> {
  q: Ref<string>
  secondary: Ref<string>
  all: Ref<T[]>
  filtered: ComputedRef<T[]>
  secondaryOptions: ComputedRef<string[]>
  loading: Ref<boolean>
  error: Ref<string | null>
  refresh: () => Promise<void>
}

export function useFilteredList<T>(opts: FilteredListOptions<T>): FilteredListState<T> {
  const { datasetId, fetcher, searchFields, secondaryField } = opts

  const q = ref('')
  const secondary = ref('')
  const all = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const error = ref<string | null>(null)

  const secondaryOptions = computed<string[]>(() => {
    if (!secondaryField) return []
    const set = new Set<string>()
    for (const item of all.value) {
      const v = secondaryField(item)
      if (v) set.add(v)
    }
    return Array.from(set).sort()
  })

  const filtered = computed<T[]>(() => {
    const needle = q.value.trim().toLowerCase()
    return all.value.filter((item) => {
      if (secondary.value && secondaryField && secondaryField(item) !== secondary.value) {
        return false
      }
      if (!needle) return true
      return searchFields(item).some((v) => v && v.toLowerCase().includes(needle))
    })
  })

  async function refresh() {
    if (!datasetId.value) return
    loading.value = true
    error.value = null
    try {
      all.value = await fetcher(datasetId.value)
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e)
    } finally {
      loading.value = false
    }
  }

  watch(datasetId, (id, prev) => {
    if (id && id !== prev) refresh()
  })
  onMounted(refresh)

  return { q, secondary, all, filtered, secondaryOptions, loading, error, refresh }
}
