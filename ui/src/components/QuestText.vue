<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ text: string }>()

const rendered = computed(() => renderBBCode(props.text ?? ''))

// BBCode 与 BetterQuesting FormattingTag 对齐(api2/.../FormattingTag.java):
//   note / warn / quest   颜色高亮
//   bold / italic / underline / strikethrough / obfuscated   文本样式
//   url 或 [url link=...]label[/url]   超链接
const ALLOWED_TAGS = new Set([
  'note', 'warn', 'quest',
  'bold', 'italic', 'underline', 'strikethrough', 'obfuscated',
  'url',
])

type Token =
  | { kind: 'text'; value: string }
  | { kind: 'open'; name: string; params: Record<string, string> }
  | { kind: 'close'; name: string }

const TOKEN_RE = /\[\/?[a-z0-9]+(?:\s+[a-z0-9]+=[^\s\]]+)*\]/gi
const OPEN_RE = /^\[([a-z0-9]+)((?:\s+[a-z0-9]+=[^\s\]]+)*)\]$/i
const CLOSE_RE = /^\[\/([a-z0-9]+)\]$/i

function escapeHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function safeUrl(raw: string): string {
  const trimmed = raw.trim()
  return /^(https?:|mailto:)/i.test(trimmed) ? trimmed : ''
}

function tokenize(text: string): Token[] {
  const tokens: Token[] = []
  let cursor = 0
  TOKEN_RE.lastIndex = 0
  let m: RegExpExecArray | null
  while ((m = TOKEN_RE.exec(text)) !== null) {
    if (m.index > cursor) {
      tokens.push({ kind: 'text', value: text.slice(cursor, m.index) })
    }
    const tok = m[0]
    const closeMatch = tok.match(CLOSE_RE)
    const openMatch = tok.match(OPEN_RE)
    if (closeMatch) {
      const name = closeMatch[1].toLowerCase()
      if (ALLOWED_TAGS.has(name)) {
        tokens.push({ kind: 'close', name })
      } else {
        tokens.push({ kind: 'text', value: tok })
      }
    } else if (openMatch) {
      const name = openMatch[1].toLowerCase()
      if (ALLOWED_TAGS.has(name)) {
        const params: Record<string, string> = {}
        const paramsStr = openMatch[2] ?? ''
        for (const it of paramsStr.matchAll(/\s+([a-z0-9]+)=([^\s\]]+)/gi)) {
          params[it[1].toLowerCase()] = it[2]
        }
        tokens.push({ kind: 'open', name, params })
      } else {
        tokens.push({ kind: 'text', value: tok })
      }
    } else {
      tokens.push({ kind: 'text', value: tok })
    }
    cursor = m.index + tok.length
  }
  if (cursor < text.length) {
    tokens.push({ kind: 'text', value: text.slice(cursor) })
  }
  return tokens
}

function wrap(name: string, params: Record<string, string>, inner: string): string {
  switch (name) {
    case 'note':         return `<span class="bb-note">${inner}</span>`
    case 'warn':         return `<span class="bb-warn">${inner}</span>`
    case 'quest':        return `<span class="bb-quest">${inner}</span>`
    case 'bold':         return `<strong>${inner}</strong>`
    case 'italic':       return `<em>${inner}</em>`
    case 'underline':    return `<u>${inner}</u>`
    case 'strikethrough': return `<s>${inner}</s>`
    case 'obfuscated':   return `<span class="bb-obf">${inner}</span>`
    case 'url': {
      // [url link=https://...]label[/url] 优先用 link 作 href;
      // [url]https://...[/url] 则用 inner 作 href。inner 已是转义后 HTML,需先解码再校验。
      const linkParam = params.link
      const fallback = decodeHtml(inner.replace(/<[^>]+>/g, ''))
      const href = safeUrl(linkParam ?? fallback)
      if (!href) return `<span class="bb-url">${inner}</span>`
      return `<a class="bb-url" href="${escapeHtml(href)}" target="_blank" rel="noopener noreferrer">${inner}</a>`
    }
    default: return inner
  }
}

function decodeHtml(s: string): string {
  return s
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
}

// 把 § 颜色码剥掉(MC 自身格式),BetterQuesting 渲染时也是这么做的
function stripMcCodes(s: string): string {
  return s.replace(/§[0-9a-fk-or]/gi, '')
}

// 递归下降:遇到 open 就压栈解析子内容,遇到 close 看是否匹配栈顶。不匹配就当字面量。
function renderBBCode(raw: string): string {
  // 段落规范化:CRLF/CR → LF;多个连续空行合并成「一段空行」(\n\n),
  // 否则原文里 \n\n\n\n 会直译成 <br><br><br><br> 视觉过空。
  const normalized = stripMcCodes(raw)
    .replace(/\r\n?/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
  const tokens = tokenize(normalized)
  return parse(tokens, 0, null).html.replace(/\n/g, '<br>')
}

function parse(tokens: Token[], start: number, parentTag: string | null): { html: string; next: number } {
  let html = ''
  let i = start
  while (i < tokens.length) {
    const t = tokens[i]
    if (t.kind === 'text') {
      html += escapeHtml(t.value)
      i++
    } else if (t.kind === 'close') {
      if (t.name === parentTag) {
        return { html, next: i + 1 }
      }
      // 不匹配的闭合 tag,按字面量输出。
      html += escapeHtml(`[/${t.name}]`)
      i++
    } else {
      const child = parse(tokens, i + 1, t.name)
      html += wrap(t.name, t.params, child.html)
      i = child.next
    }
  }
  return { html, next: i }
}
</script>

<template>
  <div
    class="quest-text"
    v-html="rendered"
  />
</template>

<style scoped>
.quest-text :deep(.bb-note)  { color: var(--el-color-primary); }
.quest-text :deep(.bb-warn)  { color: var(--el-color-danger); font-weight: 600; }
.quest-text :deep(.bb-quest) { color: var(--el-color-success); }
.quest-text :deep(.bb-url)   { color: var(--el-color-primary); text-decoration: underline; cursor: pointer; }
.quest-text :deep(.bb-obf)   { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; opacity: 0.6; }
.quest-text :deep(strong)    { font-weight: 700; }
.quest-text :deep(em)        { font-style: italic; }
.quest-text :deep(u)         { text-decoration: underline; }
.quest-text :deep(s)         { text-decoration: line-through; }
.quest-text :deep(a)         { word-break: break-word; }
</style>
