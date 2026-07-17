import {
  onBeforeUnmount,
  readonly,
  ref,
  toValue,
  watch,
  type MaybeRefOrGetter,
} from 'vue'

const OBFUSCATION_INTERVAL_MS = 50
const REDUCED_MOTION_QUERY = '(prefers-reduced-motion: reduce)'
const VISIBLE_STATE: DocumentVisibilityState = 'visible'

const tick = ref(0)
const prefersReducedMotion = ref(false)
const readonlyTick = readonly(tick)
const readonlyPrefersReducedMotion = readonly(prefersReducedMotion)

let activeConsumers = 0
let timerId: ReturnType<typeof window.setInterval> | null = null
let reducedMotionQuery: MediaQueryList | null = null

function stopTimer() {
  if (timerId === null) return
  window.clearInterval(timerId)
  timerId = null
}

function startTimer() {
  if (
    timerId !== null ||
    activeConsumers === 0 ||
    prefersReducedMotion.value ||
    document.visibilityState !== VISIBLE_STATE
  ) {
    return
  }

  timerId = window.setInterval(() => {
    tick.value++
  }, OBFUSCATION_INTERVAL_MS)
}

function handleVisibilityChange() {
  if (document.visibilityState === VISIBLE_STATE) {
    startTimer()
  } else {
    stopTimer()
  }
}

function handleReducedMotionChange(event: MediaQueryListEvent) {
  prefersReducedMotion.value = event.matches
  if (event.matches) {
    stopTimer()
  } else {
    startTimer()
  }
}

function addConsumer() {
  activeConsumers++
  if (activeConsumers > 1) return

  reducedMotionQuery = window.matchMedia(REDUCED_MOTION_QUERY)
  prefersReducedMotion.value = reducedMotionQuery.matches
  reducedMotionQuery.addEventListener('change', handleReducedMotionChange)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  startTimer()
}

function removeConsumer() {
  activeConsumers--
  if (activeConsumers > 0) return

  activeConsumers = 0
  stopTimer()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  reducedMotionQuery?.removeEventListener('change', handleReducedMotionChange)
  reducedMotionQuery = null
  prefersReducedMotion.value = false
}

export function useMcObfuscationTick(active: MaybeRefOrGetter<boolean>) {
  let subscribed = false

  const stopWatching = watch(
    () => toValue(active),
    (isActive) => {
      if (isActive === subscribed) return
      subscribed = isActive
      if (isActive) {
        addConsumer()
      } else {
        removeConsumer()
      }
    },
    { immediate: true },
  )

  onBeforeUnmount(() => {
    stopWatching()
    if (subscribed) {
      subscribed = false
      removeConsumer()
    }
  })

  return {
    tick: readonlyTick,
    prefersReducedMotion: readonlyPrefersReducedMotion,
  }
}
