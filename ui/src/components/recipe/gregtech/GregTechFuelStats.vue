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

const headerTitle = computed(() => {
  const name = profile.value?.displayName ?? '发电配方'
  const type = fuelTypeText.value
  return type ? `${name} · ${type}` : name
})

const energyDensityText = computed(() => {
  if (isLargeBoiler.value) return null
  const v = props.info.fuelValue
  return v !== null ? `${formatNumber(v)} EU/L` : null
})

const perBucketText = computed(() => {
  if (isLargeBoiler.value) return null
  const v = props.info.fuelValue
  return v !== null ? `${formatNumber(v * 1000)} EU` : null
})

const efficiencyText = computed(() => {
  const p = profile.value
  if (!p) return null
  if (p.baseEfficiencyPercent !== null) return `${p.baseEfficiencyPercent}%`
  return null
})

const efficiencyTooltip = computed(() => profile.value?.tierEfficiencyFormula ?? null)

const additionalLines = computed(() =>
  props.info.additionalInfo
    ? props.info.additionalInfo.split(/\r?\n/).map((l) => l.trim()).filter((l) => l.length > 0)
    : [],
)
</script>

<template>
  <div class="fuel-card">
    <div class="fuel-header">
      <span class="bolt">⚡</span>
      <span class="machine-name">{{ headerTitle }}</span>
    </div>

    <div v-if="!isLargeBoiler" class="fuel-grid">
      <div class="row" v-if="energyDensityText">
        <span class="row-label">能量密度</span>
        <span class="row-value mono">{{ energyDensityText }}</span>
      </div>
      <div class="row" v-if="perBucketText">
        <span class="row-label">每桶产能</span>
        <span class="row-value mono">{{ perBucketText }}</span>
      </div>
      <div class="row" v-if="efficiencyText">
        <span class="row-label">默认效率</span>
        <span class="row-value">
          {{ efficiencyText }}
          <el-tooltip
            v-if="efficiencyTooltip"
            :content="efficiencyTooltip"
            placement="top"
            :show-after="150"
          >
            <span class="info-icon">ⓘ</span>
          </el-tooltip>
        </span>
      </div>
    </div>

    <div v-if="!isLargeBoiler" class="fuel-note">
      <span class="info-icon">ⓘ</span>
      <span>实际发电量 = 能量密度 × 机器效率 × 流体消耗速率</span>
    </div>

    <div v-if="isLargeBoiler && additionalLines.length" class="fuel-boiler-block">
      <div v-for="(line, idx) in additionalLines" :key="idx" class="boiler-line">{{ line }}</div>
    </div>
  </div>
</template>

<style scoped>
.fuel-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(234, 88, 12, 0.06);
  border: 1px solid rgba(234, 88, 12, 0.35);
  border-radius: 6px;
}
.fuel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 13px;
  color: #ea580c;
  border-bottom: 1px solid rgba(234, 88, 12, 0.25);
  padding-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
}
.bolt {
  font-size: 14px;
}
.machine-name {
  overflow: hidden;
  text-overflow: ellipsis;
}
.fuel-grid {
  display: grid;
  grid-template-columns: 96px 1fr;
  row-gap: 4px;
  column-gap: 12px;
  font-size: 13px;
  align-items: baseline;
}
.row {
  display: contents;
}
.row-label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.row-value {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  color: var(--el-text-color-primary);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.row-value.mono {
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.fuel-note {
  display: flex;
  gap: 6px;
  align-items: baseline;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  border-top: 1px solid rgba(234, 88, 12, 0.18);
  padding-top: 6px;
}
.info-icon {
  color: #ea580c;
  cursor: help;
  font-size: 12px;
}
.fuel-boiler-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: var(--el-text-color-primary);
}
.boiler-line {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
