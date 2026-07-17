import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import type { TooltipKeyState } from '@shared/tooltip/types'

const LEFT_SHIFT_CODE = 'ShiftLeft'
const LEFT_CONTROL_CODE = 'ControlLeft'
const VISIBLE_STATE: DocumentVisibilityState = 'visible'

const leftShiftPressed = ref(false)
const leftControlPressed = ref(false)
const keyState = computed<TooltipKeyState>(() => {
  if (leftShiftPressed.value && leftControlPressed.value) return 'lshift_lcontrol'
  if (leftShiftPressed.value) return 'lshift'
  if (leftControlPressed.value) return 'lcontrol'
  return 'none'
})

let consumerCount = 0

function reset() {
  leftShiftPressed.value = false
  leftControlPressed.value = false
}

function handleKeydown(event: KeyboardEvent) {
  if (event.code === LEFT_SHIFT_CODE) leftShiftPressed.value = true
  if (event.code === LEFT_CONTROL_CODE) leftControlPressed.value = true
}

function handleKeyup(event: KeyboardEvent) {
  if (event.code === LEFT_SHIFT_CODE) leftShiftPressed.value = false
  if (event.code === LEFT_CONTROL_CODE) leftControlPressed.value = false
}

function handleVisibilityChange() {
  if (document.visibilityState !== VISIBLE_STATE) reset()
}

function acquire() {
  consumerCount += 1
  if (consumerCount !== 1) return

  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('keyup', handleKeyup)
  window.addEventListener('blur', reset)
  document.addEventListener('visibilitychange', handleVisibilityChange)
}

function release() {
  consumerCount -= 1
  if (consumerCount !== 0) return

  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('keyup', handleKeyup)
  window.removeEventListener('blur', reset)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  reset()
}

export function useTooltipKeyState() {
  onMounted(acquire)
  onBeforeUnmount(release)

  return keyState
}
