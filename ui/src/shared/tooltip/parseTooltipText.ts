import type { McTooltipLine } from './types'

const LINE_SEPARATOR = '\n'
const CUSTOM_RENDERER_BOUNDARY = '§x'
const SECTION_GAP_MARKER = '§h'
const SECTION_GAP_RESET_MARKER = '§h§r'
const RESET_FORMATTING = '§r'

function stripSectionGapMarker(rawLine: string): { text: string; sectionGapAfter: boolean } {
  if (rawLine.endsWith(SECTION_GAP_RESET_MARKER)) {
    return {
      text: `${rawLine.slice(0, -SECTION_GAP_RESET_MARKER.length)}${RESET_FORMATTING}`,
      sectionGapAfter: true,
    }
  }

  if (rawLine.endsWith(SECTION_GAP_MARKER)) {
    return {
      text: rawLine.slice(0, -SECTION_GAP_MARKER.length),
      sectionGapAfter: true,
    }
  }

  return { text: rawLine, sectionGapAfter: false }
}

export function parseTooltipText(text: string): McTooltipLine[] {
  return text.split(LINE_SEPARATOR).map((rawLine, index) => {
    const parsed = stripSectionGapMarker(rawLine)

    return {
      text: parsed.text,
      role: index === 0 ? 'title' : 'body',
      blank: rawLine === '',
      sectionGapAfter: index === 0 || parsed.sectionGapAfter,
      customRenderer: rawLine.startsWith(CUSTOM_RENDERER_BOUNDARY),
    }
  })
}
