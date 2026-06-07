import { createI18n } from 'vue-i18n';
import zh_CN from './zh_CN';
import en_US from './en_US';
import type { MessageSchema } from './messages';

type MessageTree = Record<string, unknown>;

export const DEFAULT_LOCALE: string = 'zh_CN';

export const i18n = createI18n<[MessageSchema], typeof DEFAULT_LOCALE>({
  legacy: false,
  globalInjection: true,
  locale: DEFAULT_LOCALE,
  fallbackLocale: DEFAULT_LOCALE,
  messages: {
    zh_CN,
    en_US,
  },
  missing(_locale: string, key: string) {
    console.warn(`[i18n] missing key: ${key}`);
    return key;
  },
});

export function setLocale(locale: string | null | undefined) {
  (i18n.global.locale as unknown as { value: string }).value = locale || DEFAULT_LOCALE;
}

export function mergeLocaleMessages(locale: string, messages: MessageTree) {
  i18n.global.mergeLocaleMessage(locale || DEFAULT_LOCALE, messages);
}
