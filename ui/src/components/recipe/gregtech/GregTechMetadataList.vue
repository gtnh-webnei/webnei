<script setup lang="ts">
import { computed } from 'vue'
import type {
  MaterialRef,
  MetadataValue,
  QuantumComputerData,
  SolarFactoryWaferData,
} from '@/api/recipes.types'

const props = defineProps<{
  metadata: Record<string, MetadataValue>
}>()

// Already rendered upstream — skip in L3.
const SKIP_KEYS = new Set([
  'cleanroom',
  'low_gravity',
  'no_gas',
  'recycle',
  'pcb_factory_bio_upgrade',
  'fuel_value',
  'fuel_type',
])

interface Row {
  key: string
  label: string
  value: string
  tone: 'plain' | 'highlight' | 'ref'
  rawTooltip?: string
}

function formatNumber(v: number | string): string {
  const n = typeof v === 'string' ? Number(v) : v
  if (!Number.isFinite(n)) return String(v)
  return n.toLocaleString('en-US')
}

function parseJson<T>(v: MetadataValue): T | null {
  if (v.valueJson === null || v.valueJson === undefined) return null
  return v.valueJson as T
}

function scalarText(v: MetadataValue): string {
  if (v.valueText !== null) return v.valueText
  if (v.valueJson !== null && v.valueJson !== undefined) {
    try { return JSON.stringify(v.valueJson) } catch { return String(v.valueJson) }
  }
  return ''
}

function expand(key: string, v: MetadataValue): Row[] {
  switch (key) {
    case 'coil_heat':
      return [{ key, label: '热容', value: `${formatNumber(scalarText(v))} K`, tone: 'highlight' }]
    case 'additives':
      return [{ key, label: '添加剂', value: formatNumber(scalarText(v)), tone: 'plain' }]
    case 'no_gas_circuit_config':
      return [{ key, label: '无气体电路', value: scalarText(v), tone: 'plain' }]
    case 'compression_tier':
      return [{ key, label: '压缩等级', value: scalarText(v), tone: 'plain' }]
    case 'pcb_factory_tier':
      return [{ key, label: 'PCB 等级', value: scalarText(v), tone: 'plain' }]
    case 'nano_forge_tier':
      return [{ key, label: '纳米锻造等级', value: scalarText(v), tone: 'plain' }]
    case 'eu_multiplier':
      return [{ key, label: 'EU 倍率', value: scalarText(v), tone: 'plain' }]
    case 'fusion_threshold':
      return [{
        key,
        label: '聚变阈值',
        value: `${formatNumber(scalarText(v))} EU`,
        tone: 'highlight',
      }]
    case 'purification_plant_base_chance': {
      const n = Number(scalarText(v))
      const pct = Number.isFinite(n) ? `${(n * 100).toFixed(2).replace(/\.?0+$/, '')}%` : scalarText(v)
      return [{ key, label: '基础几率', value: pct, tone: 'plain' }]
    }
    case 'half-life':
      return [{ key, label: '半衰期', value: `${scalarText(v)}s`, tone: 'plain' }]
    case 'decay-type':
      return [{ key, label: '衰变类型', value: scalarText(v), tone: 'plain' }]
    case 'material':
    case 'pcb_nanite_material': {
      const ref = parseJson<MaterialRef>(v)
      const name = ref?.name ?? scalarText(v)
      return [{
        key,
        label: key === 'material' ? '材料' : '纳米材料',
        value: name,
        tone: 'ref',
        rawTooltip: ref ? JSON.stringify(ref) : undefined,
      }]
    }
    case 'solar_factory_wafer_data': {
      const d = parseJson<SolarFactoryWaferData>(v)
      if (!d) return []
      return [
        { key: `${key}.minimumWaferTier`, label: '晶圆等级', value: `≥ ${d.minimumWaferTier}`, tone: 'plain' },
        { key: `${key}.minimumWaferCount`, label: '晶圆数量', value: `≥ ${d.minimumWaferCount}`, tone: 'plain' },
        { key: `${key}.tierRequired`, label: '机器等级', value: `≥ ${d.tierRequired}`, tone: 'plain' },
      ]
    }
    case 'quantum_computer_data': {
      const d = parseJson<QuantumComputerData>(v)
      if (!d) return []
      const rows: Row[] = []
      if (d.maxHeat !== -1) rows.push({ key: `${key}.maxHeat`, label: '最大温度', value: `${formatNumber(d.maxHeat)} K`, tone: 'highlight' })
      if (d.computation !== -1) rows.push({ key: `${key}.computation`, label: '算力', value: formatNumber(d.computation), tone: 'plain' })
      if (d.subZero) rows.push({ key: `${key}.subZero`, label: '亚零度', value: '是', tone: 'plain' })
      if (d.coolConstant !== -1) rows.push({ key: `${key}.coolConstant`, label: '散热常数', value: formatNumber(d.coolConstant), tone: 'plain' })
      if (d.heatConstant !== -1) rows.push({ key: `${key}.heatConstant`, label: '热量常数', value: formatNumber(d.heatConstant), tone: 'plain' })
      return rows
    }
    default: {
      // Unknown key fallback — don't drop data but mark as raw.
      if (v.valueType === 'json') {
        return [{
          key,
          label: key,
          value: '(json)',
          tone: 'plain',
          rawTooltip: v.valueJson ? JSON.stringify(v.valueJson) : undefined,
        }]
      }
      const text = scalarText(v)
      if (text === '' || text === 'false') return []
      return [{ key, label: key, value: text, tone: 'plain' }]
    }
  }
}

const rows = computed<Row[]>(() => {
  const out: Row[] = []
  for (const [key, value] of Object.entries(props.metadata ?? {})) {
    if (SKIP_KEYS.has(key)) continue
    out.push(...expand(key, value))
  }
  return out
})
</script>

<template>
  <dl v-if="rows.length" class="meta-list">
    <template v-for="r in rows" :key="r.key">
      <dt class="meta-label">{{ r.label }}</dt>
      <dd class="meta-value" :class="`tone-${r.tone}`" :title="r.rawTooltip ?? r.value">
        {{ r.value }}
      </dd>
    </template>
  </dl>
</template>

<style scoped>
.meta-list {
  display: grid;
  grid-template-columns: 112px 1fr;
  row-gap: 4px;
  column-gap: 12px;
  margin: 0;
  font-size: 12px;
  align-items: baseline;
}
.meta-label {
  color: var(--el-text-color-secondary);
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta-value {
  margin: 0;
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}
.meta-value.tone-highlight {
  color: #b45309;
}
.meta-value.tone-ref {
  color: var(--el-color-primary);
  font-family: inherit;
}
</style>
