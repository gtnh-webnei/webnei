<script setup lang="ts">
import { computed } from 'vue'
import type { GregTechRecipeInfo, MetadataValue } from '@/api/recipes.types'

const props = defineProps<{
  info: GregTechRecipeInfo
}>()

const TIER_COLORS: Record<string, { fg: string; bg: string }> = {
  ULV: { fg: '#64748b', bg: 'rgba(100, 116, 139, 0.12)' },
  LV:  { fg: '#b45309', bg: 'rgba(180, 83, 9, 0.12)'   },
  MV:  { fg: '#ea580c', bg: 'rgba(234, 88, 12, 0.12)'  },
  HV:  { fg: '#a16207', bg: 'rgba(161, 98, 7, 0.14)'   },
  EV:  { fg: '#9a6c2a', bg: 'rgba(154, 108, 42, 0.12)' },
  IV:  { fg: '#475569', bg: 'rgba(71, 85, 105, 0.14)'  },
  LuV: { fg: '#be185d', bg: 'rgba(190, 24, 93, 0.12)'  },
  ZPM: { fg: '#7e22ce', bg: 'rgba(126, 34, 206, 0.12)' },
  UV:  { fg: '#0369a1', bg: 'rgba(3, 105, 161, 0.12)'  },
  UHV: { fg: '#b91c1c', bg: 'rgba(185, 28, 28, 0.12)'  },
  UEV: { fg: '#047857', bg: 'rgba(4, 120, 87, 0.12)'   },
  UIV: { fg: '#b45309', bg: 'rgba(180, 83, 9, 0.14)'   },
  UMV: { fg: '#0e7490', bg: 'rgba(14, 116, 144, 0.12)' },
  UXV: { fg: '#a21caf', bg: 'rgba(162, 28, 175, 0.12)' },
  MAX: { fg: '#991b1b', bg: 'rgba(153, 27, 27, 0.16)'  },
}

const tierColor = computed(() => {
  const tier = props.info.voltageTier
  if (!tier) return null
  return TIER_COLORS[tier] ?? TIER_COLORS.LV
})

function formatNumber(v: number): string {
  return v.toLocaleString('en-US')
}

function formatDuration(ticks: number): string {
  if (ticks <= 0) return '0s'
  const totalSeconds = ticks / 20
  if (totalSeconds < 60) return totalSeconds.toFixed(2).replace(/\.?0+$/, '') + 's'
  const totalMin = Math.floor(totalSeconds / 60)
  const sec = +(totalSeconds - totalMin * 60).toFixed(2)
  if (totalMin < 60) {
    return sec === 0 ? `${totalMin}m` : `${totalMin}m ${sec}s`
  }
  const hour = Math.floor(totalMin / 60)
  const min = totalMin - hour * 60
  if (hour < 24) {
    return min === 0 ? `${hour}h` : `${hour}h ${min}m`
  }
  const day = Math.floor(hour / 24)
  const h = hour - day * 24
  return h === 0 ? `${day}d` : `${day}d ${h}h`
}

const durationTooltip = computed(() => {
  const t = props.info.durationTicks
  return `${formatNumber(t)} ticks · ${formatDuration(t)}`
})

const voltageText = computed(() =>
  props.info.voltage !== null ? `${formatNumber(props.info.voltage)} EU/t` : '—',
)
const amperageText = computed(() =>
  props.info.amperage !== null ? `${props.info.amperage} A` : '—',
)

function metaBool(key: string): boolean {
  const v: MetadataValue | undefined = props.info.metadata?.[key]
  return v?.valueText === 'true'
}

const showNoGas = computed(() => metaBool('no_gas'))
const showRecycle = computed(() => metaBool('recycle'))
const showBioUpgrade = computed(() => {
  const v = props.info.metadata?.['pcb_factory_bio_upgrade']
  return !!v && (v.valueText === 'BIO')
})
</script>

<template>
  <div class="core-stats">
    <div class="cells">
      <div class="cell tier-cell">
        <span
          v-if="info.voltageTier && tierColor"
          class="tier"
          :style="{
            color: tierColor.fg,
            background: tierColor.bg,
            borderColor: tierColor.fg,
          }"
        >{{ info.voltageTier }}</span>
        <span v-else class="cell-dim">—</span>
      </div>
      <div class="cell">
        <span class="cell-label">耗电</span>
        <span class="cell-value mono">{{ voltageText }}</span>
      </div>
      <div class="cell">
        <span class="cell-label">电流</span>
        <span class="cell-value mono">{{ amperageText }}</span>
      </div>
      <el-tooltip :content="durationTooltip" placement="top" :show-after="200">
        <div class="cell help">
          <span class="cell-label">时长</span>
          <span class="cell-value mono">{{ info.durationTicks }} t</span>
        </div>
      </el-tooltip>
    </div>

    <div class="status-icons">
      <span v-if="info.requiresCleanroom" class="status warning" title="洁净室">🧹 洁净室</span>
      <span v-if="info.requiresLowGravity" class="status low-gravity" title="低重力">⬇ 低重力</span>
      <span v-if="showNoGas" class="status info" title="无气体">🚫 无气体</span>
      <span v-if="showRecycle" class="status info" title="回收配方">♻ 回收</span>
      <span v-if="showBioUpgrade" class="status success" title="BIO 升级">🧬 BIO</span>
    </div>
  </div>
</template>

<style scoped>
.core-stats {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 12px;
}
.cells {
  display: grid;
  grid-template-columns: 38px 92px 60px 92px;
  gap: 6px;
  align-items: center;
}
.cell {
  display: flex;
  flex-direction: column;
  height: 32px;
  padding: 2px 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-fill-color);
  overflow: hidden;
  white-space: nowrap;
  justify-content: center;
}
.cell.help { cursor: help; }
.cell-label {
  color: var(--el-text-color-secondary);
  font-size: 10px;
  line-height: 1.2;
}
.cell-value {
  font-weight: 600;
  font-size: 12px;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cell-value.mono {
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.cell-dim {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  width: 100%;
}
.tier-cell {
  padding: 0;
  border: none;
  background: transparent;
  align-items: stretch;
}
.tier {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  border: 1px solid;
  border-radius: 6px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.5px;
}
.status-icons {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  overflow: hidden;
  white-space: nowrap;
}
.status {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 9px;
  border-radius: 11px;
  font-size: 12px;
  line-height: 22px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color);
  white-space: nowrap;
}
.status.warning {
  color: var(--el-color-warning);
  background: rgba(230, 162, 60, 0.12);
  border-color: rgba(230, 162, 60, 0.45);
}
.status.low-gravity {
  color: #7e22ce;
  background: rgba(126, 34, 206, 0.10);
  border-color: rgba(126, 34, 206, 0.40);
}
.status.info {
  color: var(--el-color-info);
  border-color: rgba(144, 147, 153, 0.4);
}
.status.success {
  color: var(--el-color-success);
  background: rgba(103, 194, 58, 0.10);
  border-color: rgba(103, 194, 58, 0.40);
}
</style>
