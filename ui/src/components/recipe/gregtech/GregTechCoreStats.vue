<script setup lang="ts">
import { computed } from 'vue'
import type { GregTechRecipeInfo, MetadataValue } from '@/api/recipes.types'

const props = defineProps<{
  info: GregTechRecipeInfo
}>()

function formatNumber(v: number): string {
  return v.toLocaleString('en-US')
}

function formatDuration(ticks: number): string {
  if (ticks <= 0) return '0 s'
  const totalSeconds = ticks / 20
  if (totalSeconds < 60) return totalSeconds.toFixed(2).replace(/\.?0+$/, '') + ' s'
  const totalMin = Math.floor(totalSeconds / 60)
  const sec = +(totalSeconds - totalMin * 60).toFixed(2)
  if (totalMin < 60) return sec === 0 ? `${totalMin} m` : `${totalMin} m ${sec} s`
  const hour = Math.floor(totalMin / 60)
  const min = totalMin - hour * 60
  if (hour < 24) return min === 0 ? `${hour} h` : `${hour} h ${min} m`
  const day = Math.floor(hour / 24)
  const h = hour - day * 24
  return h === 0 ? `${day} d` : `${day} d ${h} h`
}

function metaBool(key: string): boolean {
  const v: MetadataValue | undefined = props.info.metadata?.[key]
  return v?.valueText === 'true'
}

interface Row {
  key: string
  label: string
  value: string
  note?: string
  accent?: boolean
}

const rows = computed<Row[]>(() => {
  const out: Row[] = []
  const info = props.info

  if (info.voltage !== null) {
    out.push({ key: 'voltage', label: '耗电', value: `${formatNumber(info.voltage)} EU/t` })
  }
  if (info.amperage !== null) {
    out.push({ key: 'amperage', label: '电流', value: `${info.amperage} A` })
  }
  out.push({
    key: 'duration',
    label: '时长',
    value: `${formatNumber(info.durationTicks)} t`,
    note: formatDuration(info.durationTicks),
  })

  if (metaBool('cleanroom')) {
    out.push({ key: 'cleanroom', label: '洁净室', value: '需要', accent: true })
  }
  if (metaBool('low_gravity')) {
    out.push({ key: 'low_gravity', label: '低重力', value: '需要', accent: true })
  }
  if (metaBool('no_gas')) {
    out.push({ key: 'no_gas', label: '无气体', value: '是' })
  }
  if (metaBool('recycle')) {
    out.push({ key: 'recycle', label: '回收配方', value: '是' })
  }
  const bio = props.info.metadata?.['pcb_factory_bio_upgrade']
  if (bio?.valueText === 'BIO') {
    out.push({ key: 'bio', label: 'BIO 升级', value: '是' })
  }

  return out
})
</script>

<template>
  <dl class="stat-list">
    <template v-for="r in rows" :key="r.key">
      <dt class="stat-label">{{ r.label }}</dt>
      <dd class="stat-value" :class="{ accent: r.accent }">
        <span class="main">{{ r.value }}</span>
        <span v-if="r.note" class="note">{{ r.note }}</span>
      </dd>
    </template>
  </dl>
</template>

<style scoped>
.stat-list {
  display: grid;
  grid-template-columns: 80px 1fr;
  row-gap: 0;
  column-gap: 0;
  margin: 0;
}
dt, dd {
  display: flex;
  align-items: center;
  padding: 0;
  min-width: 0;
  line-height: 1.6;
}
dt {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  white-space: nowrap;
  padding-right: 12px;
}
dd {
  margin: 0;
  gap: 8px;
}
.main {
  font-size: 13px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}
.note {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.stat-value.accent .main {
  color: var(--el-color-warning);
}
</style>
