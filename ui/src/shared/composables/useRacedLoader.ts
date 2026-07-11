import { ref, type Ref } from 'vue'

export interface RacedLoader {
  loading: Ref<boolean>
  error: Ref<string | null>
  /** 使当前请求失效并清理加载状态。 */
  cancel: () => void
  /** 执行一次异步加载，只有最新一次的结果会被采纳（竞态保护）。 */
  run: <T>(
    fetch: () => Promise<T>,
    onSuccess: (value: T) => void,
    onError: (error: unknown) => void,
  ) => Promise<void>
}

/**
 * 受控异步加载内核：只负责 loading / error / 竞态，与分页和返回结构无关。
 * 搜索列表、分页列表等都在它之上组合，避免各自重写这段逻辑。
 */
export function useRacedLoader(): RacedLoader {
  const loading = ref(false)
  const error = ref<string | null>(null)
  let requestId = 0

  function cancel() {
    requestId += 1
    loading.value = false
    error.value = null
  }

  async function run<T>(
    fetch: () => Promise<T>,
    onSuccess: (value: T) => void,
    onError: (error: unknown) => void,
  ) {
    const currentRequest = ++requestId
    loading.value = true
    error.value = null
    try {
      const value = await fetch()
      if (currentRequest !== requestId) return
      onSuccess(value)
    } catch (err) {
      if (currentRequest !== requestId) return
      error.value = err instanceof Error ? err.message : String(err)
      onError(err)
    } finally {
      if (currentRequest === requestId) {
        loading.value = false
      }
    }
  }

  return { loading, error, cancel, run }
}
