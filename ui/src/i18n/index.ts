import { createI18n } from 'vue-i18n'

type MessageTree = Record<string, unknown>

export const DEFAULT_LOCALE: string = 'zh_CN'

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: DEFAULT_LOCALE,
  fallbackLocale: DEFAULT_LOCALE,
  messages: {
    [DEFAULT_LOCALE]: {},
  },
  missing(_locale, key) {
    console.warn(`[i18n] missing key: ${key}`)
    return key
  },
})

export function setLocale(locale: string | null | undefined) {
  i18n.global.locale.value = locale || DEFAULT_LOCALE
}

export function mergeLocaleMessages(locale: string, messages: MessageTree) {
  i18n.global.mergeLocaleMessage(locale || DEFAULT_LOCALE, messages)
}
