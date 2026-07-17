import { parseTooltipText } from './parseTooltipText'
import { selectTooltipSnapshot } from './selectTooltipSnapshot'
import type { McTooltipData, McTooltipLine, TooltipKeyState, TooltipRole } from './types'

export type ResolvedMcTooltipData = Exclude<McTooltipData, { kind: 'custom' }>
export type Translate = (key: string, named?: Record<string, unknown>) => string

function line(
  text: string,
  role: TooltipRole = 'body',
  sectionGapAfter = false,
): McTooltipLine {
  return {
    text,
    role,
    blank: false,
    sectionGapAfter,
    customRenderer: false,
  }
}

export function resolveMcTooltipLines(
  data: ResolvedMcTooltipData,
  keyState: TooltipKeyState,
  t: Translate,
): McTooltipLine[] {
  if (data.kind === 'item') {
    const snapshot = selectTooltipSnapshot(data.value.tooltipSnapshots, keyState)
    const lines = parseTooltipText(snapshot.text)

    if (data.value.modName) {
      lines.push(line(`§9§o${data.value.modName}§r`, 'context'))
    }

    return lines
  }

  const lines = [line(`§f${data.value.displayName}`, 'title', true)]

  if (data.value.chemicalExpression) {
    lines.push(line(`§e${data.value.chemicalExpression}§r`))
  }

  lines.push(line(`§c${t('tooltip.temperature', { kelvin: data.value.temperature })}§7`))

  const phase = t(data.value.gaseous ? 'tooltip.gas' : 'tooltip.liquid')
  lines.push(line(`§a${t('tooltip.phase', { phase })}§7`))

  if (data.value.modName) {
    lines.push(line(`§9§o${data.value.modName}§r`, 'context'))
  }

  return lines
}
