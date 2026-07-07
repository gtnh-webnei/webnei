// 解析 Minecraft §格式码为样式段落。
// 规则依据 vanilla FontRenderer.renderStringAtPos：
//   §0-§9 §a-§f 颜色码 —— 设颜色并清除所有格式
//   §k 混淆 / §l 粗体 / §m 删除线 / §n 下划线 / §o 斜体
//   §r 重置颜色与格式
// 颜色码在游戏里会重置既有格式，这里严格照此实现。

const SECTION = '\u00a7'
const COLOR_CODES = '0123456789abcdef'
const FORMAT_CODE_PATTERN = /\u00a7[0-9a-fk-or]/gi

// 去除所有 §格式码，返回纯文本（与 EnumChatFormatting.getTextWithoutFormattingCodes 一致）。
export function stripMcFormat(input: string): string {
  return input.replace(FORMAT_CODE_PATTERN, '')
}

export interface FormattedSegment {
  text: string
  color: string | null
  bold: boolean
  italic: boolean
  underline: boolean
  strikethrough: boolean
  obfuscated: boolean
}

interface ActiveStyle {
  color: string | null
  bold: boolean
  italic: boolean
  underline: boolean
  strikethrough: boolean
  obfuscated: boolean
}

function resetStyle(): ActiveStyle {
  return {
    color: null,
    bold: false,
    italic: false,
    underline: false,
    strikethrough: false,
    obfuscated: false,
  }
}

export function parseMcFormat(input: string): FormattedSegment[] {
  const segments: FormattedSegment[] = []
  let style = resetStyle()
  let buffer = ''

  const flush = () => {
    if (buffer) {
      segments.push({ ...style, text: buffer })
      buffer = ''
    }
  }

  for (let i = 0; i < input.length; i++) {
    const char = input[i]
    if (char === SECTION && i + 1 < input.length) {
      const code = input[i + 1].toLowerCase()
      if (COLOR_CODES.includes(code)) {
        flush()
        style = resetStyle()
        style.color = `var(--mc-fmt-${code})`
        i++
        continue
      }
      switch (code) {
        case 'k':
          flush()
          style = { ...style, obfuscated: true }
          i++
          continue
        case 'l':
          flush()
          style = { ...style, bold: true }
          i++
          continue
        case 'm':
          flush()
          style = { ...style, strikethrough: true }
          i++
          continue
        case 'n':
          flush()
          style = { ...style, underline: true }
          i++
          continue
        case 'o':
          flush()
          style = { ...style, italic: true }
          i++
          continue
        case 'r':
          flush()
          style = resetStyle()
          i++
          continue
        default:
          break
      }
    }
    buffer += char
  }
  flush()
  return segments
}
