/**
 * GT 配方展示 spec 解析器。
 *
 * spec 文件按 dataset 加载：display.json 是语言无关规则，i18n/<locale>.json 提供文案。
 * 每条配方进来：按 handler 找 lines → 跑 kind/format/expr → 输出 DisplayItem[]。
 *
 * Schema 见 SCHEMA.md。
 */
import { Parser, type Expression } from 'expr-eval';
import type { MetadataValue, RecipeSlot, RecipeSlotCandidate } from '@/api/recipes.types';
import { mergeLocaleMessages } from '@/i18n';

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
  | 'top_field';

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
  | 'duration_ticks'
  | 'duration_auto_unit'
  | 'java_double'
  | 'bool_yes_no';

export type TranslateFn = (key: string, params?: Record<string, unknown>) => string;

export interface VoltageBlockSingle {
  labelKey: string;
  expr?: string;
  valueKeyExpr?: string;
  format?: FormatName;
  prefixKey?: string;
  suffixKey?: string;
  valueTemplateKey?: string;
  templateArgs?: Record<string, string>;
}

export interface SpecLine {
  kind: LineKind;
  // 取数据
  key?: string;
  list_index?: number;
  field?: string;
  // 渲染
  labelKey?: string | null;
  expr?: string;
  valueKeyExpr?: string;
  scale?: number;
  format?: FormatName;
  prefixKey?: string;
  suffixKey?: string;
  suffix_table?: string;
  suffix_table_lookup?: 'exact' | 'ceil';
  suffixTableFormatKey?: string;
  literalKey?: string;
  valueTemplateKey?: string;
  templateArgs?: Record<string, string>;
  color_code?: string;
  // 显示条件
  show_if_true?: boolean;
  show_if_present?: boolean;
  show_if?: string;
  // voltage_block 子结构
  single?: VoltageBlockSingle;
  split?: VoltageBlockSingle[];
}

export interface HandlerSpec {
  nameKey?: string;
  recipe_kind?: string;
  recipe_count?: number;
  lines: SpecLine[];
}

export interface DisplaySpec {
  schema: string;
  version: number;
  tables: Record<string, Record<string, string>>;
  handlers: Record<string, HandlerSpec>;
}

// ─────────────────────────────────────────────────────────────────────
// DisplayItem (output)
// ─────────────────────────────────────────────────────────────────────

export interface DisplayItem {
  label: string | null;
  value: string;
  colorCode?: string;
}

// ─────────────────────────────────────────────────────────────────────
// Format functions
// ─────────────────────────────────────────────────────────────────────

const COMMA_INT_FMT = new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 });
const COMMA_DEC1_FMT = new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1,
});

function makeFormats(t: TranslateFn): Record<FormatName, (v: unknown) => string> {
  return {
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
    duration_seconds: (v) => formatDurationSeconds(toNum(v), t),
    duration_ticks: (v) => tr(t, 'spec.gregtech.format.duration.ticks', { value: toNum(v) }),
    duration_auto_unit: (v) => formatDurationAuto(toNum(v), t),
    java_double: (v) => formatJavaDouble(toNum(v)),
    bool_yes_no: (v) => tr(t, v ? 'spec.gregtech.format.bool.yes' : 'spec.gregtech.format.bool.no'),
  };
}

function tr(t: TranslateFn, key: string, params?: Record<string, unknown>): string {
  const value = t(key, params);
  if (value === key) console.warn(`[displaySpec] missing i18n key=${key}`);
  return value;
}

function toNum(v: unknown): number {
  if (typeof v === 'number') return v;
  if (typeof v === 'bigint') return Number(v);
  if (typeof v === 'string') {
    const n = Number(v);
    return Number.isFinite(n) ? n : 0;
  }
  if (typeof v === 'boolean') return v ? 1 : 0;
  return 0;
}

function formatDurationSeconds(ticks: number, t: TranslateFn): string {
  // ticks % 20 == 0 → 整数；否则一位小数
  if (ticks < 20) return tr(t, 'spec.gregtech.format.duration.ticks', { value: ticks });
  const seconds =
    ticks % 20 === 0 ? String(ticks / 20) : (ticks / 20).toFixed(2).replace(/\.?0+$/, '');
  return tr(t, 'spec.gregtech.format.duration.seconds', { value: seconds });
}

function formatJavaDouble(value: number): string {
  if (!Number.isFinite(value)) return String(value);
  const normalized = Math.round(value * 1_000_000_000_000) / 1_000_000_000_000;
  const abs = Math.abs(normalized);
  if (abs >= 1e7 || (abs > 0 && abs < 1e-3)) {
    const [mantissaRaw, exponentRaw] = normalized.toExponential().split('e');
    const mantissa = mantissaRaw.includes('.') ? mantissaRaw : `${mantissaRaw}.0`;
    return `${mantissa}E${Number(exponentRaw)}`;
  }
  if (Number.isInteger(normalized)) return `${normalized}.0`;
  return String(normalized);
}

function formatDurationAuto(seconds: number, t: TranslateFn): string {
  // isotopedecay：自动选秒/分/时/天单位
  if (seconds < 60) {
    return tr(t, 'spec.gregtech.format.duration.autoSeconds', { value: seconds.toFixed(2) });
  }
  if (seconds < 3600) {
    return tr(t, 'spec.gregtech.format.duration.autoMinutes', { value: (seconds / 60).toFixed(2) });
  }
  if (seconds < 86400) {
    return tr(t, 'spec.gregtech.format.duration.autoHours', { value: (seconds / 3600).toFixed(2) });
  }
  return tr(t, 'spec.gregtech.format.duration.autoDays', { value: (seconds / 86400).toFixed(2) });
}

const VOLTAGE_TIER_LIMITS: Array<[number, string]> = [
  [8, 'ULV'],
  [32, 'LV'],
  [128, 'MV'],
  [512, 'HV'],
  [2_048, 'EV'],
  [8_192, 'IV'],
  [32_768, 'LuV'],
  [131_072, 'ZPM'],
  [524_288, 'UV'],
  [2_097_152, 'UHV'],
  [8_388_608, 'UEV'],
  [33_554_432, 'UIV'],
  [134_217_728, 'UMV'],
  [536_870_912, 'UXV'],
  [2_147_483_647, 'MAX'],
];

function voltageTierName(voltage: number): string {
  for (const [limit, tier] of VOLTAGE_TIER_LIMITS) {
    if (voltage <= limit) return tier;
  }
  return 'MAX+';
}

// ─────────────────────────────────────────────────────────────────────
// Recipe context
// ─────────────────────────────────────────────────────────────────────

export interface RecipeCtx {
  handler: string;
  recipe_kind: 'PROCESSING' | 'FUEL';
  voltage: number | null;
  voltage_tier: string | null;
  amperage: number | null;
  duration_ticks: number;
  special_value: number | null;
  fluid_inputs: Array<{ amount: number; fluid_id: string }>;
  item_inputs: Array<{ item_id: string; amount: number }>;
  special_items: Array<{ itemVariantId: string }>;
  metadata: Record<string, unknown>;
}

/**
 * 把核心 recipe.metadata + 槽位扁平化成 ctx。
 *
 * GregTech 不再是特例：voltage / voltage_tier / amperage / duration_ticks / special_value /
 * recipe_kind 都从通用 metadata 取（exporter 写入的 metadata key）；special_items 从 role=
 * special_item 的槽位取（取首个候选作代表值，供 spec 的 special_item line 引用）。
 */
export function buildCtx(
  handler: string,
  metadata: Record<string, MetadataValue>,
  slots: RecipeSlot[],
): RecipeCtx {
  const flatMeta: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(metadata)) {
    flatMeta[k] = unwrapMetadata(v);
  }

  const fluidInputs: Array<{ amount: number; fluid_id: string }> = [];
  const itemInputs: Array<{ item_id: string; amount: number }> = [];
  const specialItems: Array<{ itemVariantId: string }> = [];
  for (const slot of slots) {
    const primary = primarySlotCandidate(slot);
    if (slot.role === 'fluid_input') {
      fluidInputs.push({
        amount: primary?.amount ?? slot.amount ?? 0,
        fluid_id: primary?.fluidVariantId ?? slot.fluidVariantId ?? '',
      });
    } else if (slot.role === 'item_input') {
      itemInputs.push({
        item_id: primary?.itemVariantId ?? slot.itemVariantId ?? '',
        amount: primary?.amount ?? slot.amount ?? 0,
      });
    } else if (slot.role === 'special_item') {
      const id = primary?.itemVariantId ?? slot.itemVariantId ?? '';
      if (id) specialItems.push({ itemVariantId: id });
    }
  }

  const recipeKindRaw = flatMeta['recipe_kind'];
  return {
    handler,
    recipe_kind: recipeKindRaw === 'FUEL' ? 'FUEL' : 'PROCESSING',
    voltage: numOrNull(flatMeta['voltage']),
    voltage_tier: flatMeta['voltage_tier'] != null ? String(flatMeta['voltage_tier']) : null,
    amperage: numOrNull(flatMeta['amperage']),
    duration_ticks: numOrNull(flatMeta['duration_ticks']) ?? 0,
    special_value: numOrNull(flatMeta['special_value']),
    fluid_inputs: fluidInputs,
    item_inputs: itemInputs,
    special_items: specialItems,
    metadata: flatMeta,
  };
}

function numOrNull(v: unknown): number | null {
  if (v === null || v === undefined) return null;
  if (typeof v === 'number') return v;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

function unwrapMetadata(v: MetadataValue): unknown {
  switch (v.valueType) {
    case 'int':
    case 'long':
      return v.valueText !== null ? Number(v.valueText) : null;
    case 'float':
    case 'double':
      return v.valueText !== null ? Number(v.valueText) : null;
    case 'bool':
      return v.valueText === 'true';
    case 'json':
      return v.valueJson;
    default:
      return v.valueText;
  }
}

function primarySlotCandidate(slot: RecipeSlot): RecipeSlotCandidate | null {
  if (slot.itemVariantId || slot.fluidVariantId) {
    return {
      itemVariantId: slot.itemVariantId,
      fluidVariantId: slot.fluidVariantId,
      amount: slot.amount,
      displayName: slot.displayName,
      modId: slot.modId,
      modName: slot.modName,
      fluidGaseous: slot.fluidGaseous,
      fluidTemperature: slot.fluidTemperature,
      tooltipText: slot.tooltipText,
      assetUrl: slot.assetUrl,
    };
  }
  return slot.candidates[0] ?? null;
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
});

// 注册自定义函数，让 spec 表达式能用：
// - `str(value)` 强制转成字符串
// - `int(value)` 截断为整数（取代 toFixed(0)）
// - `comma(value)` 千分位
// - `lookup(table, key)` 查表
// - `fixed1(value)` 一位小数文本
// - `tier(value)` 按 EU/t 计算电压等级名
// - `floor / ceil / round / tanh` 数学
PARSER.functions.str = (v: unknown) => String(v);
PARSER.functions.int = (v: unknown) => Math.trunc(toNum(v));
PARSER.functions.comma = (v: unknown) => COMMA_INT_FMT.format(toNum(v));
PARSER.functions.lookup = (table: Record<string, string> | null | undefined, key: unknown) =>
  table ? (table[String(key)] ?? '') : '';
PARSER.functions.fixed1 = (v: unknown) => toNum(v).toFixed(1);
PARSER.functions.tier = (v: unknown) => voltageTierName(toNum(v));
PARSER.functions.floor = (v: unknown) => Math.floor(toNum(v));
PARSER.functions.ceil = (v: unknown) => Math.ceil(toNum(v));
PARSER.functions.round = (v: unknown) => Math.round(toNum(v));
PARSER.functions.tanh = (v: unknown) => Math.tanh(toNum(v));
// 位运算（expr-eval 不支持 & | >>> ^ 等运算符；用函数代替）
PARSER.functions.band = (a: unknown, b: unknown) => toNum(a) & toNum(b);
PARSER.functions.bor = (a: unknown, b: unknown) => toNum(a) | toNum(b);
PARSER.functions.shr = (a: unknown, b: unknown) => toNum(a) >>> toNum(b);
// 数组长度（expr-eval 不能直接 .length）
PARSER.functions.len = (v: unknown) => (Array.isArray(v) ? v.length : 0);

const exprCache = new Map<string, Expression>();

function compileExpr(src: string): Expression {
  let expr = exprCache.get(src);
  if (!expr) {
    expr = PARSER.parse(src);
    exprCache.set(src, expr);
  }
  return expr;
}

function evalExpr(src: string, bindings: Record<string, unknown>): unknown {
  try {
    // expr-eval 类型严格但运行时支持任意 object；强转。
    return compileExpr(src).evaluate(bindings as Record<string, never>);
  } catch (e) {
    console.warn('[displaySpec] expr eval failed:', src, e);
    return null;
  }
}

// ─────────────────────────────────────────────────────────────────────
// Renderer
// ─────────────────────────────────────────────────────────────────────

export interface RenderOptions {
  spec: DisplaySpec;
  ctx: RecipeCtx;
  t: TranslateFn;
}

/** 主入口：拿一条 recipe + spec → DisplayItem[]。 */
export function renderRecipe({ spec, ctx, t }: RenderOptions): DisplayItem[] {
  const handlerSpec = spec.handlers[ctx.handler];
  if (!handlerSpec) {
    console.warn(`[displaySpec] no spec for handler=${ctx.handler}`);
    return [];
  }

  const out: DisplayItem[] = [];
  const formats = makeFormats(t);
  for (const line of handlerSpec.lines) {
    try {
      out.push(...renderLine(line, ctx, spec.tables, t, formats));
    } catch (e) {
      console.warn('[displaySpec] line render failed:', line, e);
    }
  }
  return out;
}

function renderLine(
  line: SpecLine,
  ctx: RecipeCtx,
  tables: Record<string, Record<string, string>>,
  t: TranslateFn,
  formats: Record<FormatName, (v: unknown) => string>,
): DisplayItem[] {
  // 特殊 kind 提前处理：voltage_block / large_boiler_table 不走标准 raw value 流程
  if (line.kind === 'voltage_block') {
    return renderVoltageBlock(line, ctx, tables, t, formats);
  }
  if (line.kind === 'large_boiler_table') {
    return renderLargeBoilerTable(ctx, t);
  }

  // 取 raw value
  const raw = takeValue(line, ctx);
  if (raw === undefined) return []; // kind 决定不渲染

  // show_if_present: metadata 不存在
  if (line.show_if_present !== false && raw === null) return [];

  // show_if_true: bool 字段必须 true
  if (line.show_if_true === true && raw !== true) return [];

  // show_if: expr
  if (line.show_if) {
    const ok = evalExpr(line.show_if, { value: raw, ctx, tables });
    if (!ok) return [];
  }

  let value: unknown = raw;
  let valueText: string;
  if (line.valueKeyExpr) {
    const keyOrValue = evalExpr(line.valueKeyExpr, { value: raw, ctx, tables });
    valueText = renderMaybeI18nKey(keyOrValue, t);
  } else if (line.expr) {
    value = evalExpr(line.expr, { value: raw, ctx, tables });
    if (value === null || value === undefined) return [];

    if (line.scale !== undefined && typeof value === 'number') {
      value = value * line.scale;
    }
    valueText = line.format
      ? formatValue(line.format, value, formats)
      : renderMaybeI18nKey(value, t);
  } else if (line.literalKey !== undefined) {
    valueText = tr(t, line.literalKey);
  } else if (line.format) {
    if (line.scale !== undefined && typeof value === 'number') {
      value = value * line.scale;
    }
    valueText = formatValue(line.format, value, formats);
  } else {
    valueText = String(value);
  }

  valueText = applyValueTemplate(line, raw, value, valueText, ctx, tables, t);

  // prefix / suffix
  if (line.prefixKey) valueText = tr(t, line.prefixKey) + valueText;
  if (line.suffixKey) valueText = valueText + tr(t, line.suffixKey);

  // suffix_table（默认按 raw 当 key 查表；ceil 模式用于“热容上限档位”等数值区间表）
  if (line.suffix_table) {
    const tbl = tables[line.suffix_table];
    if (tbl) {
      const looked = lookupSuffixTable(tbl, raw, line.suffix_table_lookup);
      if (looked !== undefined) {
        const tmpl = line.suffixTableFormatKey ? tr(t, line.suffixTableFormatKey) : '{0}';
        valueText = valueText + tmpl.replace('{0}', tr(t, looked));
      }
    }
  }

  return [
    {
      label: resolveLabel(line.labelKey, t),
      value: valueText,
      colorCode: line.color_code,
    },
  ];
}

function formatValue(
  format: FormatName,
  value: unknown,
  formats: Record<FormatName, (v: unknown) => string>,
): string {
  const fn = formats[format];
  if (!fn) {
    console.warn(`[displaySpec] unknown format=${format}`);
    return '';
  }
  return fn(value);
}

function renderMaybeI18nKey(value: unknown, t: TranslateFn): string {
  if (typeof value !== 'string') return String(value ?? '');
  if (!value.startsWith('spec.gregtech.')) return value;
  return tr(t, value);
}

function resolveLabel(labelKey: string | null | undefined, t: TranslateFn): string | null {
  if (labelKey === undefined || labelKey === null) return null;
  return tr(t, labelKey);
}

function applyValueTemplate(
  spec: Pick<SpecLine, 'valueTemplateKey' | 'templateArgs'>,
  raw: unknown,
  result: unknown,
  valueText: string,
  ctx: RecipeCtx,
  tables: Record<string, Record<string, string>>,
  t: TranslateFn,
): string {
  if (!spec.valueTemplateKey) return valueText;
  const params: Record<string, unknown> = {
    value: valueText,
    raw,
    result,
  };
  for (const [name, expr] of Object.entries(spec.templateArgs ?? {})) {
    params[name] = evalExpr(expr, { value: raw, result, ctx, tables });
  }
  return tr(t, spec.valueTemplateKey, params);
}

function lookupSuffixTable(
  table: Record<string, string>,
  raw: unknown,
  mode: SpecLine['suffix_table_lookup'] = 'exact',
): string | undefined {
  if (mode !== 'ceil') return table[String(raw)];

  const rawNum = toNum(raw);
  if (!Number.isFinite(rawNum)) return undefined;

  let bestKey: string | undefined;
  let bestNum = Infinity;
  for (const key of Object.keys(table)) {
    const n = Number(key);
    if (!Number.isFinite(n)) continue;
    if (n >= rawNum && n < bestNum) {
      bestNum = n;
      bestKey = key;
    }
  }
  return bestKey === undefined ? table['*'] : table[bestKey];
}

/** 决定 line 取什么 raw value；返回 undefined 表示这条 kind 不该渲染。 */
function takeValue(line: SpecLine, ctx: RecipeCtx): unknown {
  switch (line.kind) {
    case 'total_eu': {
      if (ctx.voltage === null || ctx.amperage === null) return undefined;
      return ctx.voltage * ctx.duration_ticks * ctx.amperage;
    }
    case 'duration': {
      if (ctx.duration_ticks <= 0) return undefined;
      return ctx.duration_ticks;
    }
    case 'fuel_heat': {
      if (ctx.special_value === null) return undefined;
      return ctx.special_value;
    }
    case 'metadata':
    case 'metadata_json':
    case 'flag': {
      if (!line.key) return undefined;
      const v = ctx.metadata[line.key];
      if (v === undefined) return null; // 触发 show_if_present
      return v;
    }
    case 'text':
      return ''; // text 用 literalKey，不依赖外部数据存在性
    case 'special_item': {
      const idx = line.list_index ?? 0;
      const item = ctx.special_items[idx];
      return item?.itemVariantId ?? null;
    }
    case 'fluid_input': {
      const idx = line.list_index ?? 0;
      const fi = ctx.fluid_inputs[idx];
      return fi ?? null;
    }
    case 'top_field': {
      if (!line.field) return undefined;
      const v = (ctx as unknown as Record<string, unknown>)[line.field];
      return v === undefined ? null : v;
    }
    case 'voltage_block':
    case 'large_boiler_table':
      return null; // 这两种走专门分支
  }
}

function renderVoltageBlock(
  line: SpecLine,
  ctx: RecipeCtx,
  tables: Record<string, Record<string, string>>,
  t: TranslateFn,
  formats: Record<FormatName, (v: unknown) => string>,
): DisplayItem[] {
  if (ctx.voltage === null) return [];
  // spec 提供哪个用哪个；amp>1 时如果两者都给了优先 split。
  let segments: VoltageBlockSingle[] = [];
  if (line.split && (ctx.amperage ?? 1) > 1) {
    segments = line.split;
  } else if (line.split && !line.single) {
    // 只给了 split：handler 强制三行（lanth_targetchamber 等）
    segments = line.split;
  } else if (line.single) {
    segments = [line.single];
  }
  const out: DisplayItem[] = [];
  for (const seg of segments) {
    if (!seg) continue;
    const bindings = { value: ctx.voltage, ctx, tables };
    let text: string;
    let result: unknown = ctx.voltage;
    if (seg.valueKeyExpr) {
      const keyOrValue = evalExpr(seg.valueKeyExpr, bindings);
      text = renderMaybeI18nKey(keyOrValue, t);
    } else if (seg.expr) {
      const v = evalExpr(seg.expr, bindings);
      result = v;
      text = seg.format ? formatValue(seg.format, v, formats) : renderMaybeI18nKey(v, t);
    } else {
      text = String(ctx.voltage);
    }
    text = applyValueTemplate(seg, ctx.voltage, result, text, ctx, tables, t);
    if (seg.prefixKey) text = tr(t, seg.prefixKey) + text;
    if (seg.suffixKey) text = text + tr(t, seg.suffixKey);
    out.push({ label: resolveLabel(seg.labelKey, t), value: text });
  }
  return out;
}

function renderLargeBoilerTable(ctx: RecipeCtx, t: TranslateFn): DisplayItem[] {
  // 大型锅炉燃烧时间表：special_value / 40（diesel）or / 10（dense liquid），
  // 4 个锅炉系数：青铜 ×2 / 钢制 ×1 / 钛制 ×0.3 / 钨钢 ×0.15
  // 简化：用 diesel 路径；阈值禁用判断保持原样
  if (ctx.special_value === null) return [];
  const base = ctx.special_value / 40;
  const round05 = (v: number) => Math.round(v * 20) / 20;

  function cell(mult: number): string {
    const v = round05(base * mult);
    return v < 0.05
      ? tr(t, 'spec.gregtech.largeBoiler.disabled')
      : v.toFixed(2).replace(/\.?0+$/, '');
  }

  return [
    { label: tr(t, 'spec.gregtech.largeBoiler.header'), value: '' },
    { label: tr(t, 'spec.gregtech.largeBoiler.bronze'), value: cell(2) },
    { label: tr(t, 'spec.gregtech.largeBoiler.steel'), value: cell(1) },
    { label: tr(t, 'spec.gregtech.largeBoiler.titanium'), value: cell(0.3) },
    { label: tr(t, 'spec.gregtech.largeBoiler.tungstensteel'), value: cell(0.15) },
  ];
}

// ─────────────────────────────────────────────────────────────────────
// Spec loading & cache
// ─────────────────────────────────────────────────────────────────────

const specCache = new Map<string, Promise<DisplaySpec | null>>();
const messagesCache = new Map<string, Promise<void>>();

/**
 * 按 spec URL 加载 spec；同 URL 只加载一次。
 *
 * URL 由后端从 dataset 资源根 + 'spec/display.json' 拼出（AssetUrlBuilder），形如：
 *   /assets/gtnh/2.8.4/official/spec/display.json
 * 跟图片/语言文件等其它资源一样，由 vite middleware (dev) / nginx (prod) 直接 serve。
 */
export function loadDisplaySpec(specUrl: string | null | undefined): Promise<DisplaySpec | null> {
  if (!specUrl) return Promise.resolve(null);
  let p = specCache.get(specUrl);
  if (!p) {
    p = fetchSpec(specUrl).catch((e) => {
      console.warn(`[displaySpec] load failed url=${specUrl}`, e);
      return null;
    });
    specCache.set(specUrl, p);
  }
  return p;
}

export function loadDisplaySpecMessages(
  messagesUrl: string | null | undefined,
  locale: string,
): Promise<void> {
  if (!messagesUrl) return Promise.resolve();
  const cacheKey = `${locale}:${messagesUrl}`;
  let p = messagesCache.get(cacheKey);
  if (!p) {
    p = fetchMessages(messagesUrl)
      .then((messages) => mergeLocaleMessages(locale, messages))
      .catch((e) => {
        console.warn(`[displaySpec] messages load failed url=${messagesUrl}`, e);
      });
    messagesCache.set(cacheKey, p);
  }
  return p;
}

async function fetchSpec(url: string): Promise<DisplaySpec> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`spec fetch failed: ${url} → ${res.status}`);
  const spec = (await res.json()) as DisplaySpec;
  if (spec.schema !== 'recipe-display-spec' || spec.version !== 2) {
    console.warn('[displaySpec] unexpected spec schema/version:', spec.schema, spec.version);
  }
  return spec;
}

async function fetchMessages(url: string): Promise<Record<string, unknown>> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`spec messages fetch failed: ${url} → ${res.status}`);
  return (await res.json()) as Record<string, unknown>;
}

/** 从 categoryId/handlerId 拆出 handler 短名（去掉 'gregtech:' 前缀）。 */
export function extractHandlerName(handlerId: string | null | undefined): string {
  if (!handlerId) return '';
  const colon = handlerId.indexOf(':');
  return colon >= 0 ? handlerId.slice(colon + 1) : handlerId;
}
