import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import { useMediaQuery } from '@vueuse/core'

export type ThemeMode = 'light' | 'dark' | 'auto'

const STORAGE_KEY = 'webnei.theme'

function readStored(): ThemeMode {
  const v = localStorage.getItem(STORAGE_KEY)
  return v === 'light' || v === 'dark' || v === 'auto' ? v : 'auto'
}

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>(readStored())
  const prefersDark = useMediaQuery('(prefers-color-scheme: dark)')

  const effective = computed<'light' | 'dark'>(() => {
    if (mode.value === 'auto') return prefersDark.value ? 'dark' : 'light'
    return mode.value
  })

  function setMode(value: ThemeMode) {
    mode.value = value
  }

  function apply() {
    const root = document.documentElement
    if (effective.value === 'dark') {
      root.classList.add('dark')
      root.setAttribute('data-theme', 'dark')
      root.style.colorScheme = 'dark'
    } else {
      root.classList.remove('dark')
      root.setAttribute('data-theme', 'light')
      root.style.colorScheme = 'light'
    }
  }

  watch(mode, (value) => {
    localStorage.setItem(STORAGE_KEY, value)
    apply()
  })
  watch(effective, () => apply())

  return { mode, effective, setMode, apply }
})
