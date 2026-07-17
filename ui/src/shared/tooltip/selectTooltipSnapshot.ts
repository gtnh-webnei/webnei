import type { TooltipKeyState, TooltipSnapshot, TooltipType } from './types'

const STANDARD_KEY_STATE: TooltipKeyState = 'none'

export function selectTooltipSnapshot(
  snapshots: readonly TooltipSnapshot[],
  keyState: TooltipKeyState,
): TooltipSnapshot {
  const matches = snapshots.filter((snapshot) => snapshot.keyState === keyState)

  if (matches.length !== 1) {
    throw new Error(`Expected exactly one tooltip snapshot for key state "${keyState}"`)
  }

  const snapshot = matches[0]
  const expectedType: TooltipType = keyState === STANDARD_KEY_STATE ? 'standard' : 'key'

  if (snapshot.tooltipType !== expectedType) {
    throw new Error(
      `Expected tooltip type "${expectedType}" for key state "${keyState}", received "${snapshot.tooltipType}"`,
    )
  }

  return snapshot
}
