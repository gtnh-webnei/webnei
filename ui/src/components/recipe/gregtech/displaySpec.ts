/**
 * GT 配方展示 spec 解析器。
 *
 * spec 文件按 dataset.language 加载（zh_CN.json / en_US.json / ...）。
 * 每条配方进来：按 handler 找 lines → 跑 kind/format/expr → 输出 DisplayItem[]。
 *
 * Schema 见 SCHEMA.md。
 */
import { Parser, type Expression } from 'expr-eval'
import type {
  GregTechRecipeInfo,
  GregTechSpecialItem,
  MetadataValue,
  RecipeSlot,
} from '@/api/recipes.types'

// ─────────────────────────────────────────────────────────────────────
// Spec types
// ─────────────────────────────────────────────────────────────────────

export type LineKind =
  | 'total_eu'
  | 'voltage_block'
  | 'duration'
  | 'fuel_heat'
  | 'large_boiler_table'
  | 'metadata'
  | 'metadata_json'
  | 'flag'
  | 'text'
  | 'special_item'
  | 'fluid_input'
  | 'top_field'

export type FormatName =
  | 'direct'
  | 'as_string'
  | 'comma_int'
  | 'comma_long'
  | 'comma_double_1'
  | 'percent_int_x100'
  | 'percent_double_x100'
  | 'decimal_0'
  | 'decimal_1'
  | 'decimal_2'
  | 'scientific'
  | 'duration_seconds'
  | 'duration_seconds_en'
  | 'duration_ticks'
  | 'duration_auto_unit'
  | 'bool_yes_no'
  | 'bool_yes_no_cn'

export interface VoltageBlockSingle {
  label: string
  expr?: string
  format?: FormatName
  prefix?: string
  suffix?: string
}

export interface SpecLine {
  kind: LineKind
  // 取数据
  key?: string
  list_index?: number
  field?: string
  // 渲染
  label?: string | null
  expr?: string
  scale?: number
  format?: FormatName
  prefix?: string
  suffix?: string
  suffix_table?: string
  suffix_table_format?: string
  literal?: string
  color_code?: string
  // 显示条件
  show_if_true?: boolean
  show_if_present?: boolean
  show_if?: string
  // voltage_block 子结构
  single?: VoltageBlockSingle
  split?: VoltageBlockSingle[]
}

export interface HandlerSpec {
  name_cn?: string
  recipe_kind?: string
  recipe_count?: number
  lines: SpecLine[]
}

export interface DisplaySpec {
  version: number
  language: string
  tables: Record<string, Record<string, string>>
  handlers: Record<string, HandlerSpec>
}

// ─────────────────────────────────────────────────────────────────────
// DisplayItem (output)
// ─────────────────────────────────────────────────────────────────────

export interface DisplayItem {
  label: string | null
  value: string
  colorCode?: string
}

// ─────────────────────────────────────────────────────────────────────
// Format functions
// ─────────────────────────────────────────────────────────────────────

const COMMA_INT_FMT = new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 })
const COMMA_DEC1_FMT = new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
})

const FORMATS: Record<FormatName, (v: unknown) => string> = {
  direct: (v) => String(v),
  as_string: (v) => String(v),
  comma_int: (v) => COMMA_INT_FMT.format(toNum(v)),
  comma_long: (v) => COMMA_INT_FMT.format(toNum(v)),
  comma_double_1: (v) => COMMA_DEC1_FMT.format(toNum(v)),
  percent_int_x100: (v) => `${toNum(v)}%`,
  percent_double_x100: (v) => `${Math.round(toNum(v) * 100)}%`,
  decimal_0: (v) => Math.round(toNum(v)).toString(),
  decimal_1: (v) => toNum(v).toFixed(1),
  decimal_2: (v) => toNum(v).toFixed(2),
  scientific: (v) => toNum(v).toExponential(2),
  duration_seconds: (v) => formatDurationSeconds(toNum(v), '秒'),
  duration_seconds_en: (v) => formatDurationSeconds(toNum(v), 'secs'),
  duration_ticks: (v) => `${toNum(v)} tick`,
  duration_auto_unit: (v) => formatDurationAuto(toNum(v)),
  bool_yes_no: (v) => (v ? 'Yes' : 'No'),
  bool_yes_no_cn: (v) => (v ? '是' : '否'),
}

function toNum(v: unknown): number {
  if (typeof v === 'number') return v
  if (typeof v === 'bigint') return Number(v)
  if (typeof v === 'string') {
    const n = Number(v)
    return Number.isFinite(n) ? n : 0
  }
  if (typeof v === 'boolean') return v ? 1 : 0
  return 0
}

function formatDurationSeconds(ticks: number, unit: string): string {
  // ticks % 20 == 0 → 整数；否则一位小数
  if (ticks < 20) return `${ticks} tick`
  if (ticks % 20 === 0) return `${ticks / 20} ${unit}`
  return `${(ticks / 20).toFixed(2).replace(/\.?0+$/, '')} ${unit}`
}

function formatDurationAuto(seconds: number): string {
  // isotopedecay：自动选秒/分/时/天单位
  if (seconds < 60) return `${seconds.toFixed(2)}秒`
  if (seconds < 3600) return `${(seconds / 60).toFixed(2)}分`
  if (seconds < 86400) return `${(seconds / 3600).toFixed(2)}时`
  return `${(seconds / 86400).toFixed(2)}天`
}

// ─────────────────────────────────────────────────────────────────────
// Recipe context
// ─────────────────────────────────────────────────────────────────────

export interface RecipeCtx {
  handler: string
  recipe_kind: 'PROCESSING' | 'FUEL'
  voltage: number | null
  voltage_tier: string | null
  amperage: number | null
  duration_ticks: number
  special_value: number | null
  fluid_inputs: Array<{ amount: number; fluid_id: string }>
  item_inputs: Array<{ item_id: string; amount: number }>
  special_items: GregTechSpecialItem[]
  metadata: Record<string, unknown>
}

/** 把 GregTechRecipeInfo + 槽位扁平化成 ctx。 */
export function buildCtx(
  handler: string,
  gt: GregTechRecipeInfo,
  slots: RecipeSlot[],
): RecipeCtx {
  const flatMeta: Record<string, unknown> = {}
  for (const [k, v] of Object.entries(gt.metadata)) {
    flatMeta[k] = unwrapMetadata(v)
  }

  const fluidInputs: Array<{ amount: number; fluid_id: string }> = []
  const itemInputs: Array<{ item_id: string; amount: number }> = []
  for (const slot of slots) {
    if (slot.role === 'fluid_input') {
      fluidInputs.push({
        amount: slot.amount ?? 0,
        fluid_id: slot.fluidVariantId ?? '',
      })
    } else if (slot.role === 'item_input') {
      itemInputs.push({
        item_id: slot.itemVariantId ?? '',
        amount: slot.amount ?? 0,
      })
    }
  }

  return {
    handler,
    recipe_kind: gt.recipeKind,
    voltage: gt.voltage,
    voltage_tier: gt.voltageTier,
    amperage: gt.amperage,
    duration_ticks: gt.durationTicks,
    special_value: gt.specialValue,
    fluid_inputs: fluidInputs,
    item_inputs: itemInputs,
    special_items: gt.specialItems ?? [],
    metadata: flatMeta,
  }
}

function unwrapMetadata(v: MetadataValue): unknown {
  switch (v.valueType) {
    case 'int':
    case 'long':
      return v.valueText !== null ? Number(v.valueText) : null
    case 'float':
    case 'double':
      return v.valueText !== null ? Number(v.valueText) : null
    case 'bool':
      return v.valueText === 'true'
    case 'json':
      return v.valueJson
    default:
      return v.valueText
  }
}

// ─────────────────────────────────────────────────────────────────────
// Expression evaluation
// ─────────────────────────────────────────────────────────────────────

const PARSER = new Parser({
  operators: {
    // 打开字符串拼接 `||`：`'a' || 'b'` => 'ab'。
    // 避开 `+` 与数字加法冲突，spec 里所有字符串拼接走 `||`。
    concatenate: true,
  },
})

// 注册自定义函数，让 spec 表达式能用：
// - `str(value)` 强制转成字符串
// - `int(value)` 截断为整数（取代 toFixed(0)）
// - `comma(value)` 千分位
// - `lookup(table, key)` 查表
// - `floor / ceil / round` 数学
PARSER.functions.str = (v: unknown) => String(v)
PARSER.functions.int = (v: unknown) => Math.trunc(toNum(v))
PARSER.functions.comma = (v: unknown) => COMMA_INT_FMT.format(toNum(v))
PARSER.functions.lookup = (
  table: Record<string, string> | null | undefined,
  key: unknown,
) => (table ? table[String(key)] ?? '' : '')
PARSER.functions.floor = (v: unknown) => Math.floor(toNum(v))
PARSER.functions.ceil = (v: unknown) => Math.ceil(toNum(v))
PARSER.functions.round = (v: unknown) => Math.round(toNum(v))
// 位运算（expr-eval 不支持 & | >>> ^ 等运算符；用函数代替）
PARSER.functions.band = (a: unknown, b: unknown) => toNum(a) & toNum(b)
PARSER.functions.bor = (a: unknown, b: unknown) => toNum(a) | toNum(b)
PARSER.functions.shr = (a: unknown, b: unknown) => toNum(a) >>> toNum(b)
// 数组长度（expr-eval 不能直接 .length）
PARSER.functions.len = (v: unknown) => (Array.isArray(v) ? v.length : 0)

const exprCache = new Map<string, Expression>()

function compileExpr(src: string): Expression {
  let expr = exprCache.get(src)
  if (!expr) {
    expr = PARSER.parse(src)
    exprCache.set(src, expr)
  }
  return expr
}

function evalExpr(
  src: string,
  bindings: Record<string, unknown>,
): unknown {
  try {
    // expr-eval 类型严格但运行时支持任意 object；强转。
    return compileExpr(src).evaluate(bindings as Record<string, never>)
  } catch (e) {
    console.warn('[displaySpec] expr eval failed:', src, e)
    return null
  }
}

// ─────────────────────────────────────────────────────────────────────
// Renderer
// ─────────────────────────────────────────────────────────────────────

export interface RenderOptions {
  spec: DisplaySpec
  ctx: RecipeCtx
}

/** 主入口：拿一条 recipe + spec → DisplayItem[]。 */
export function renderRecipe({ spec, ctx }: RenderOptions): DisplayItem[] {
  const handlerSpec = spec.handlers[ctx.handler]
  if (!handlerSpec) {
    console.warn(`[displaySpec] no spec for handler=${ctx.handler}`)
    return []
  }

  const out: DisplayItem[] = []
  for (const line of handlerSpec.lines) {
    try {
      out.push(...renderLine(line, ctx, spec.tables))
    } catch (e) {
      console.warn('[displaySpec] line render failed:', line, e)
    }
  }
  return out
}

function renderLine(
  line: SpecLine,
  ctx: RecipeCtx,
  tables: Record<string, Record<string, string>>,
): DisplayItem[] {
  // 特殊 kind 提前处理：voltage_block / large_boiler_table 不走标准 raw value 流程
  if (line.kind === 'voltage_block') {
    return renderVoltageBlock(line, ctx, tables)
  }
  if (line.kind === 'large_boiler_table') {
    return renderLargeBoilerTable(ctx)
  }

  // 取 raw value
  const raw = takeValue(line, ctx)
  if (raw === undefined) return [] // kind 决定不渲染

  // show_if_present: metadata 不存在
  if (line.show_if_present !== false && raw === null) return []

  // show_if_true: bool 字段必须 true
  if (line.show_if_true === true && raw !== true) return []

  // show_if: expr
  if (line.show_if) {
    const ok = evalExpr(line.show_if, { value: raw, ctx, tables })
    if (!ok) return []
  }

  // expr
  let value: unknown = raw
  if (line.expr) {
    value = evalExpr(line.expr, { value: raw, ctx, tables })
    if (value === null || value === undefined) return []
  }

  // scale
  if (line.scale !== undefined && typeof value === 'number') {
    value = value * line.scale
  }

  // 字面（flag / text）
  let valueText = ''
  if (line.literal !== undefined) {
    valueText = line.literal
  } else if (line.format) {
    const fn = FORMATS[line.format]
    if (!fn) {
      console.warn(`[displaySpec] unknown format=${line.format}`)
      return []
    }
    valueText = fn(value)
  } else {
    valueText = String(value)
  }

  // prefix / suffix
  if (line.prefix) valueText = line.prefix + valueText
  if (line.suffix) valueText = valueText + line.suffix

  // suffix_table（按 raw 当 key 查表，不是 expr 后的 value）
  if (line.suffix_table) {
    const tbl = tables[line.suffix_table]
    if (tbl) {
      const key = String(raw)
      const looked = tbl[key]
      if (looked !== undefined) {
        const tmpl = line.suffix_table_format ?? ' ({0})'
        valueText = valueText + tmpl.replace('{0}', looked)
      }
    }
  }

  return [
    {
      label: line.label === undefined ? null : line.label,
      value: valueText,
      colorCode: line.color_code,
    },
  ]
}

/** 决定 line 取什么 raw value；返回 undefined 表示这条 kind 不该渲染。 */
function takeValue(line: SpecLine, ctx: RecipeCtx): unknown {
  switch (line.kind) {
    case 'total_eu': {
      if (ctx.voltage === null || ctx.amperage === null) return undefined
      return ctx.voltage * ctx.duration_ticks * ctx.amperage
    }
    case 'duration': {
      if (ctx.duration_ticks <= 0) return undefined
      return ctx.duration_ticks
    }
    case 'fuel_heat': {
      if (ctx.special_value === null) return undefined
      return ctx.special_value
    }
    case 'metadata':
    case 'metadata_json':
    case 'flag': {
      if (!line.key) return undefined
      const v = ctx.metadata[line.key]
      if (v === undefined) return null // 触发 show_if_present
      return v
    }
    case 'text':
      return null // text 用 literal，不取 raw
    case 'special_item': {
      const idx = line.list_index ?? 0
      const item = ctx.special_items[idx]
      return item?.itemVariantId ?? null
    }
    case 'fluid_input': {
      const idx = line.list_index ?? 0
      const fi = ctx.fluid_inputs[idx]
      return fi ?? null
    }
    case 'top_field': {
      if (!line.field) return undefined
      const v = (ctx as unknown as Record<string, unknown>)[line.field]
      return v === undefined ? null : v
    }
    case 'voltage_block':
    case 'large_boiler_table':
      return null // 这两种走专门分支
  }
}

function renderVoltageBlock(
  line: SpecLine,
  ctx: RecipeCtx,
  tables: Record<string, Record<string, string>>,
): DisplayItem[] {
  if (ctx.voltage === null) return []
  // spec 提供哪个用哪个；amp>1 时如果两者都给了优先 split。
  let segments: VoltageBlockSingle[] = []
  if (line.split && (ctx.amperage ?? 1) > 1) {
    segments = line.split
  } else if (line.split && !line.single) {
    // 只给了 split：handler 强制三行（lanth_targetchamber 等）
    segments = line.split
  } else if (line.single) {
    segments = [line.single]
  }
  const out: DisplayItem[] = []
  for (const seg of segments) {
    if (!seg) continue
    const bindings = { value: ctx.voltage, ctx, tables }
    let text: string
    if (seg.expr) {
      const v = evalExpr(seg.expr, bindings)
      text = seg.format ? FORMATS[seg.format](v) : String(v)
    } else {
      text = String(ctx.voltage)
    }
    if (seg.prefix) text = seg.prefix + text
    if (seg.suffix) text = text + seg.suffix
    out.push({ label: seg.label, value: text })
  }
  return out
}

function renderLargeBoilerTable(ctx: RecipeCtx): DisplayItem[] {
  // 大型锅炉燃烧时间表：special_value / 40（diesel）or / 10（dense liquid），
  // 4 个锅炉系数：青铜 ×2 / 钢制 ×1 / 钛制 ×0.3 / 钨钢 ×0.15
  // 简化：用 diesel 路径；阈值禁用判断保持原样
  if (ctx.special_value === null) return []
  const base = ctx.special_value / 40
  const round05 = (v: number) => Math.round(v * 20) / 20

  function cell(mult: number): string {
    const v = round05(base * mult)
    return v < 0.05 ? '禁止使用' : v.toFixed(2).replace(/\.?0+$/, '')
  }

  return [
    { label: '燃烧时间（秒）', value: '' },
    { label: '青铜锅炉', value: cell(2) },
    { label: '钢制锅炉', value: cell(1) },
    { label: '钛制锅炉', value: cell(0.3) },
    { label: '钨钢锅炉', value: cell(0.15) },
  ]
}

// ─────────────────────────────────────────────────────────────────────
// Spec loading & cache
// ─────────────────────────────────────────────────────────────────────

const specCache = new Map<string, Promise<DisplaySpec | null>>()

/**
 * 按 spec URL 加载 spec；同 URL 只加载一次。
 *
 * URL 由后端从 dataset 资源根 + 'spec/<lang>.json' 拼出（AssetUrlBuilder），形如：
 *   /assets/gtnh/2.8.4/official/spec/zh_CN.json
 * 跟图片/语言文件等其它资源一样，由 vite middleware (dev) / nginx (prod) 直接 serve。
 */
export function loadDisplaySpec(specUrl: string | null | undefined): Promise<DisplaySpec | null> {
  if (!specUrl) return Promise.resolve(null)
  let p = specCache.get(specUrl)
  if (!p) {
    p = fetchSpec(specUrl).catch((e) => {
      console.warn(`[displaySpec] load failed url=${specUrl}`, e)
      return null
    })
    specCache.set(specUrl, p)
  }
  return p
}

async function fetchSpec(url: string): Promise<DisplaySpec> {
  const res = await fetch(url)
  if (!res.ok) throw new Error(`spec fetch failed: ${url} → ${res.status}`)
  return (await res.json()) as DisplaySpec
}

/** 从 categoryId/handlerId 拆出 handler 短名（去掉 'gregtech:' 前缀）。 */
export function extractHandlerName(handlerId: string | null | undefined): string {
  if (!handlerId) return ''
  const colon = handlerId.indexOf(':')
  return colon >= 0 ? handlerId.slice(colon + 1) : handlerId
}
