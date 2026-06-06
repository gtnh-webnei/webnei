<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineProps<{
  tier: string;
}>();

const TIER_COLORS: Record<string, { fg: string; bg: string }> = {
  ULV: { fg: '#64748b', bg: 'rgba(100, 116, 139, 0.12)' },
  LV: { fg: '#b45309', bg: 'rgba(180, 83, 9, 0.12)' },
  MV: { fg: '#ea580c', bg: 'rgba(234, 88, 12, 0.12)' },
  HV: { fg: '#a16207', bg: 'rgba(161, 98, 7, 0.14)' },
  EV: { fg: '#9a6c2a', bg: 'rgba(154, 108, 42, 0.12)' },
  IV: { fg: '#475569', bg: 'rgba(71, 85, 105, 0.14)' },
  LuV: { fg: '#be185d', bg: 'rgba(190, 24, 93, 0.12)' },
  ZPM: { fg: '#7e22ce', bg: 'rgba(126, 34, 206, 0.12)' },
  UV: { fg: '#0369a1', bg: 'rgba(3, 105, 161, 0.12)' },
  UHV: { fg: '#b91c1c', bg: 'rgba(185, 28, 28, 0.12)' },
  UEV: { fg: '#047857', bg: 'rgba(4, 120, 87, 0.12)' },
  UIV: { fg: '#b45309', bg: 'rgba(180, 83, 9, 0.14)' },
  UMV: { fg: '#0e7490', bg: 'rgba(14, 116, 144, 0.12)' },
  UXV: { fg: '#a21caf', bg: 'rgba(162, 28, 175, 0.12)' },
  MAX: { fg: '#991b1b', bg: 'rgba(153, 27, 27, 0.16)' },
};

const color = computed(() => TIER_COLORS[props.tier] ?? TIER_COLORS.LV);
</script>

<template>
  <div class="tier-badge" :style="{ color: color.fg, background: color.bg, borderColor: color.fg }">
    <span class="label">{{ t('recipe.tier') }}</span>
    <span class="tier">{{ tier }}</span>
  </div>
</template>

<style scoped>
.tier-badge {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 4px 10px 5px;
  border: 1px solid;
  border-radius: 6px;
  line-height: 1;
  user-select: none;
}
.label {
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  opacity: 0.75;
  margin-bottom: 3px;
}
.tier {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-weight: 800;
  font-size: 16px;
  letter-spacing: 0.5px;
}
</style>
