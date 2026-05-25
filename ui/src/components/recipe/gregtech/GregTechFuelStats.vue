<script setup lang="ts">
import { computed } from 'vue'
import type { GregTechRecipeInfo } from '@/api/recipes.types'

const props = defineProps<{
  info: GregTechRecipeInfo
}>()

const FUEL_TYPE_LABEL: Record<number, string> = {
  0: '柴油燃料',
  1: '燃气轮机燃料',
  2: '热力燃料',
  3: '半流体燃料',
  4: '等离子燃料',
  5: '魔力燃料',
}

function formatNumber(v: number): string {
  return v.toLocaleString('en-US')
}

const profile = computed(() => props.info.fuelProfile)
const isLargeBoiler = computed(() => profile.value?.machineKind === 'large_boiler')

const fuelTypeText = computed(() => {
  const v = props.info.metadata?.['fuel_type']
  if (!v?.valueText) return null
  const n = Number(v.valueText)
  return FUEL_TYPE_LABEL[n] ?? `类型 ${n}`
})

const machineName = computed(() => profile.value?.displayName ?? null)

interface Row {
  key: string
  label: string
  value: string
  note?: string
  tooltip?: string
}

const rows = computed<Row[]>(() => {
  const out: Row[] = []
  if (machineName.value) {
    out.push({ key: 'machine', label: '机器', value: machineName.value })
  }
  if (fuelTypeText.value) {
    out.push({ key: 'fuel_type', label: '燃料类型', value: fuelTypeText.value })
  }
  if (!isLargeBoiler.value && props.info.fuelValue !== null) {
    const v = props.info.fuelValue
    out.push({ key: 'energy_density', label: '能量密度', value: `${formatNumber(v)} EU/L` })
    out.push({ key: 'per_bucket', label: '每桶产能', value: `${formatNumber(v * 1000)} EU` })
  }
  const p = profile.value
  if (p?.baseEfficiencyPercent !== null && p?.baseEfficiencyPercent !== undefined) {
    out.push({
      key: 'efficiency',
      label: '默认效率',
      value: `${p.baseEfficiencyPercent}%`,
      tooltip: p.tierEfficiencyFormula ?? undefined,
    })
  }
  return out
})

const additionalLines = computed(() =>
  props.info.additionalInfo
    ? props.info.additionalInfo.split(/\r?\n/).map((l) => l.trim()).filter((l) => l.length > 0)
    : [],
)
</script>

<template>
  <div class="fuel-stats">
    <dl class="stat-list">
      <template v-for="r in rows" :key="r.key">
        <dt class="stat-label">{{ r.label }}</dt>
        <dd class="stat-value">
          <span class="main">{{ r.value }}</span>
          <el-tooltip v-if="r.tooltip" :content="r.tooltip" placement="top" :show-after="150">
            <span class="info-icon">ⓘ</span>
          </el-tooltip>
        </dd>
      </template>
    </dl>

    <div v-if="!isLargeBoiler" class="hint">
      <span class="info-icon">ⓘ</span>
      <span>实际发电量 = 能量密度 × 机器效率 × 流体消耗速率</span>
    </div>

    <div v-if="isLargeBoiler && additionalLines.length" class="boiler-block">
      <div v-for="(line, idx) in additionalLines" :key="idx" class="boiler-line">{{ line }}</div>
    </div>
  </div>
</template>

<style scoped>
.fuel-stats {
  display: flex;
  flex-direction: column;
}
.stat-list {
  display: grid;
  grid-template-columns: 80px 1fr;
  margin: 0;
}
.stat-label,
.stat-value {
  display: flex;
  align-items: center;
  padding: 0;
  min-width: 0;
  line-height: 1.6;
}
.stat-label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  white-space: nowrap;
  padding-right: 12px;
}
.stat-value {
  margin: 0;
  gap: 6px;
}
.main {
  font-size: 13px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}
.info-icon {
  color: var(--el-text-color-secondary);
  cursor: help;
  font-size: 12px;
}
.hint {
  display: flex;
  gap: 6px;
  align-items: baseline;
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.boiler-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
  font-size: 13px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: var(--el-text-color-primary);
}
.boiler-line {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
