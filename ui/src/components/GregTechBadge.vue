<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { GregTechRecipeInfo, GregTechSpecialItem } from '@/api/recipes.types'

const props = defineProps<{
  info: GregTechRecipeInfo
}>()

const router = useRouter()
const route = useRoute()

// 电压等级 — 淡色调,只用作单个 chip 的 fg/bg,不再做大色块
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

const tierColor = computed(() => TIER_COLORS[props.info.voltageTier] ?? TIER_COLORS.LV)

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

const additionalLines = computed(() =>
  props.info.additionalInfo
    ? props.info.additionalInfo.split('\n').filter((s) => s.length > 0)
    : [],
)

const datasetId = computed(() => String(route.params.datasetId ?? ''))

function goSpecial(item: GregTechSpecialItem) {
  if (!datasetId.value) return
  router.replace({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: item.itemVariantId, kind: 'detail' },
  })
}
</script>

<template>
  <div class="gt-badge">
    <div class="chips">
      <span
        class="chip tier"
        :style="{
          color: tierColor.fg,
          background: tierColor.bg,
          borderColor: tierColor.fg,
        }"
      >
        {{ info.voltageTier }}
      </span>
      <span class="chip">
        <span class="chip-label">耗电</span>
        <span class="chip-value mono">{{ formatNumber(info.voltage) }} EU/t</span>
      </span>
      <span class="chip">
        <span class="chip-label">电流</span>
        <span class="chip-value mono">{{ info.amperage }} A</span>
      </span>
      <el-tooltip :content="durationTooltip" placement="top" :show-after="200">
        <span class="chip help">
          <span class="chip-label">时长</span>
          <span class="chip-value mono">{{ info.durationTicks }} t</span>
        </span>
      </el-tooltip>
      <span v-if="info.requiresCleanroom" class="chip warning">
        <span class="chip-value">洁净室</span>
      </span>
      <span v-for="(line, idx) in additionalLines" :key="`add-${idx}`" class="chip note">
        {{ line }}
      </span>
    </div>

    <div v-if="info.specialItems?.length" class="specials">
      <span class="specials-label">特殊物品</span>
      <button
        v-for="item in info.specialItems"
        :key="item.itemVariantId"
        type="button"
        class="special-chip"
        :title="item.displayName ?? item.itemVariantId"
        @click="goSpecial(item)"
      >
        <img v-if="item.assetUrl" :src="item.assetUrl" :alt="item.displayName ?? ''" loading="lazy" />
        <span class="special-name">{{ item.displayName ?? item.itemVariantId }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.gt-badge {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}
.chip {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  height: 22px;
  padding: 0 9px;
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 11px;
  font-size: 12px;
  line-height: 22px;
  white-space: nowrap;
  color: var(--el-text-color-regular);
}
.chip.tier {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-weight: 700;
  letter-spacing: 0.5px;
  min-width: 38px;
  justify-content: center;
}
.chip.help {
  cursor: help;
}
.chip.warning {
  color: var(--el-color-warning);
  background: rgba(230, 162, 60, 0.12);
  border-color: rgba(230, 162, 60, 0.45);
}
.chip.note {
  background: transparent;
  border-color: transparent;
  border-left: 2px solid var(--el-border-color);
  border-radius: 0;
  padding: 0 0 0 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.chip-label {
  color: var(--el-text-color-secondary);
  font-size: 11px;
}
.chip-value {
  font-weight: 600;
  font-size: 12px;
}
.chip-value.mono {
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.specials {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}
.specials-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.special-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 22px;
  padding: 0 8px 0 4px;
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  cursor: pointer;
  font: inherit;
  color: inherit;
  font-size: 12px;
  transition: border-color 0.15s, background 0.15s;
}
.special-chip:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.special-chip img {
  width: 16px;
  height: 16px;
  object-fit: contain;
  image-rendering: pixelated;
  flex-shrink: 0;
}
.special-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
