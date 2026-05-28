// Compact number formatter mirroring GT/AE2 ReadableNumberConverter (kMGTPE,
// divide by 1000, ≤1 decimal, round-down). Used everywhere a slot label has
// to fit a small cell in NEI-style recipe panels.
const POSTFIXES = ['k', 'M', 'G', 'T', 'P', 'E']
const BASE = 1000

function formatTwoDecimal(n: number): string {
  // Round-down to 1 decimal place, matching DecimalFormat ".#" with RoundingMode.DOWN.
  const truncated = Math.floor(n * 10) / 10
  if (Number.isInteger(truncated)) return String(truncated)
  return truncated.toFixed(1)
}

function compact(num: number, width: number): string {
  if (!Number.isFinite(num)) return String(num)
  const sign = num < 0 ? '-' : ''
  let n = Math.abs(Math.trunc(num))
  const raw = String(n)
  if (raw.length <= width) return sign + raw

  let base = n
  let last = base * BASE
  let exponent = -1
  let postfix = ''
  let numberSize = raw.length

  while (numberSize > width) {
    last = base
    base = Math.floor(base / BASE)
    exponent++
    numberSize = String(base).length + 1
    postfix = POSTFIXES[exponent] ?? POSTFIXES[POSTFIXES.length - 1]
  }

  const withPrecision = formatTwoDecimal(last / BASE) + postfix
  const withoutPrecision = String(base) + postfix
  const result = withPrecision.length <= width ? withPrecision : withoutPrecision
  return sign + result
}

/** Wide form (≤4 chars body), used for in-game NEI overlays. */
export function formatCompact(num: number): string {
  return compact(num, 4)
}

/** Slim form (≤3 chars body) for very tight slots. */
export function formatCompactSlim(num: number): string {
  return compact(num, 3)
}

/** Full number with thousands separators, for tooltips/hover cards. */
export function formatFull(num: number): string {
  if (!Number.isFinite(num)) return String(num)
  return num.toLocaleString('en-US')
}

/** Fluid amount (mL). Compact body + 'L' suffix to match in-game NEI labels. */
export function formatFluidCompact(num: number): string {
  return formatCompact(num) + 'L'
}

/** Fluid amount full, e.g. '72,000,000 mL'. */
export function formatFluidFull(num: number): string {
  return formatFull(num) + ' mL'
}
