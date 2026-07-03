import { createI18n } from 'vue-i18n'
import { messages } from './messages'

export type SupportedLocale = keyof typeof messages

const FALLBACK_LOCALE: SupportedLocale = 'zh_CN'

export function normalizeLocale(value: string | null | undefined): SupportedLocale {
  const normalized = value?.replace('-', '_').toLowerCase() ?? ''
  if (normalized.startsWith('zh')) return 'zh_CN'
  if (normalized.startsWith('en')) return 'en_US'
  return FALLBACK_LOCALE
}

export function browserLocale(): SupportedLocale {
  return normalizeLocale(navigator.language)
}

export const i18n = createI18n({
  legacy: false,
  locale: browserLocale(),
  fallbackLocale: FALLBACK_LOCALE,
  messages,
})

export function setLocale(value: string | null | undefined) {
  const locale = normalizeLocale(value)
  i18n.global.locale.value = locale
  document.documentElement.lang = locale.replace('_', '-')
}
