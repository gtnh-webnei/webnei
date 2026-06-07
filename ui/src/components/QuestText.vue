<script setup lang="ts">
import { Fragment, computed, defineComponent, h, type VNodeChild } from 'vue';

const props = defineProps<{ text: string }>();

const rendered = computed(() => renderBBCode(props.text ?? ''));

const QuestTextContent = defineComponent({
  name: 'QuestTextContent',
  setup() {
    return () => h(Fragment, rendered.value);
  },
});

// BBCode 与 BetterQuesting FormattingTag 对齐(api2/.../FormattingTag.java):
//   note / warn / quest   颜色高亮
//   bold / italic / underline / strikethrough / obfuscated   文本样式
//   url 或 [url link=...]label[/url]   超链接
const ALLOWED_TAGS = new Set([
  'note',
  'warn',
  'quest',
  'bold',
  'italic',
  'underline',
  'strikethrough',
  'obfuscated',
  'url',
]);

type Token =
  | { kind: 'text'; value: string }
  | { kind: 'open'; name: string; params: Record<string, string> }
  | { kind: 'close'; name: string };

type ParseResult = { nodes: VNodeChild[]; text: string; next: number };

const TOKEN_RE = /\[\/?[a-z0-9]+(?:\s+[a-z0-9]+=[^\s\]]+)*\]/gi;
const OPEN_RE = /^\[([a-z0-9]+)((?:\s+[a-z0-9]+=[^\s\]]+)*)\]$/i;
const CLOSE_RE = /^\[\/([a-z0-9]+)\]$/i;

function safeUrl(raw: string): string {
  const trimmed = raw.trim();
  return /^(https?:|mailto:)/i.test(trimmed) ? trimmed : '';
}

function tokenize(text: string): Token[] {
  const tokens: Token[] = [];
  let cursor = 0;
  TOKEN_RE.lastIndex = 0;
  let m: RegExpExecArray | null;
  while ((m = TOKEN_RE.exec(text)) !== null) {
    if (m.index > cursor) {
      tokens.push({ kind: 'text', value: text.slice(cursor, m.index) });
    }
    const tok = m[0];
    const closeMatch = tok.match(CLOSE_RE);
    const openMatch = tok.match(OPEN_RE);
    if (closeMatch) {
      const name = closeMatch[1].toLowerCase();
      if (ALLOWED_TAGS.has(name)) {
        tokens.push({ kind: 'close', name });
      } else {
        tokens.push({ kind: 'text', value: tok });
      }
    } else if (openMatch) {
      const name = openMatch[1].toLowerCase();
      if (ALLOWED_TAGS.has(name)) {
        const params: Record<string, string> = {};
        const paramsStr = openMatch[2] ?? '';
        for (const it of paramsStr.matchAll(/\s+([a-z0-9]+)=([^\s\]]+)/gi)) {
          params[it[1].toLowerCase()] = it[2];
        }
        tokens.push({ kind: 'open', name, params });
      } else {
        tokens.push({ kind: 'text', value: tok });
      }
    } else {
      tokens.push({ kind: 'text', value: tok });
    }
    cursor = m.index + tok.length;
  }
  if (cursor < text.length) {
    tokens.push({ kind: 'text', value: text.slice(cursor) });
  }
  return tokens;
}

function textNodes(text: string): VNodeChild[] {
  const lines = text.split('\n');
  return lines.flatMap((line, index) => (index === 0 ? [line] : [h('br'), line]));
}

function wrap(
  name: string,
  params: Record<string, string>,
  children: VNodeChild[],
  text: string,
): VNodeChild[] {
  switch (name) {
    case 'note':
      return [h('span', { class: 'bb-note' }, children)];
    case 'warn':
      return [h('span', { class: 'bb-warn' }, children)];
    case 'quest':
      return [h('span', { class: 'bb-quest' }, children)];
    case 'bold':
      return [h('strong', children)];
    case 'italic':
      return [h('em', children)];
    case 'underline':
      return [h('u', children)];
    case 'strikethrough':
      return [h('s', children)];
    case 'obfuscated':
      return [h('span', { class: 'bb-obf' }, children)];
    case 'url': {
      const href = safeUrl(params.link ?? text);
      if (!href) return [h('span', { class: 'bb-url' }, children)];
      return [
        h('a', { class: 'bb-url', href, target: '_blank', rel: 'noopener noreferrer' }, children),
      ];
    }
    default:
      return children;
  }
}

// 把 § 颜色码剥掉(MC 自身格式),BetterQuesting 渲染时也是这么做的
function stripMcCodes(s: string): string {
  return s.replace(/§[0-9a-fk-or]/gi, '');
}

// 递归下降:遇到 open 就压栈解析子内容,遇到 close 看是否匹配栈顶。不匹配就当字面量。
function renderBBCode(raw: string): VNodeChild[] {
  // 段落规范化:CRLF/CR → LF;多个连续空行合并成「一段空行」(\n\n),
  // 否则原文里 \n\n\n\n 会直译成多次换行,视觉过空。
  const normalized = stripMcCodes(raw)
    .replace(/\r\n?/g, '\n')
    .replace(/\n{3,}/g, '\n\n');
  const tokens = tokenize(normalized);
  return parse(tokens, 0, null).nodes;
}

function parse(tokens: Token[], start: number, parentTag: string | null): ParseResult {
  const nodes: VNodeChild[] = [];
  let text = '';
  let i = start;
  while (i < tokens.length) {
    const token = tokens[i];
    if (token.kind === 'text') {
      nodes.push(...textNodes(token.value));
      text += token.value;
      i++;
    } else if (token.kind === 'close') {
      if (token.name === parentTag) {
        return { nodes, text, next: i + 1 };
      }
      const literal = `[/${token.name}]`;
      nodes.push(...textNodes(literal));
      text += literal;
      i++;
    } else {
      const child = parse(tokens, i + 1, token.name);
      nodes.push(...wrap(token.name, token.params, child.nodes, child.text));
      text += child.text;
      i = child.next;
    }
  }
  return { nodes, text, next: i };
}
</script>

<template>
  <div class="quest-text">
    <QuestTextContent />
  </div>
</template>

<style scoped>
.quest-text :deep(.bb-note) {
  color: var(--el-color-primary);
}
.quest-text :deep(.bb-warn) {
  color: var(--el-color-danger);
  font-weight: 600;
}
.quest-text :deep(.bb-quest) {
  color: var(--el-color-success);
}
.quest-text :deep(.bb-url) {
  color: var(--el-color-primary);
  text-decoration: underline;
  cursor: pointer;
}
.quest-text :deep(.bb-obf) {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  opacity: 0.6;
}
.quest-text :deep(strong) {
  font-weight: 700;
}
.quest-text :deep(em) {
  font-style: italic;
}
.quest-text :deep(u) {
  text-decoration: underline;
}
.quest-text :deep(s) {
  text-decoration: line-through;
}
.quest-text :deep(a) {
  word-break: break-word;
}
</style>
