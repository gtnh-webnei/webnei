<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { listCategoryMachines, listCategoryVoltageTiers } from '@/api/recipes';
import type { CategoryMachine, CategoryVoltageTier, LookupKind } from '@/api/recipes.types';

const props = withDefaults(
  defineProps<{
    datasetId: string;
    categoryId: string;
    activeTier: string | null;
    hideMachines?: boolean;
    target?: string | null;
    kind?: LookupKind | null;
  }>(),
  { hideMachines: false, target: null, kind: null },
);

const { t } = useI18n();

const emit = defineEmits<{
  (e: 'update:activeTier', tier: string | null): void;
}>();

const router = useRouter();

const machines = ref<CategoryMachine[]>([]);
const tiers = ref<CategoryVoltageTier[]>([]);

const tiersTotal = computed(() => tiers.value.reduce((sum, t) => sum + t.recipeCount, 0));

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
function tierColor(tier: string) {
  return TIER_COLORS[tier] ?? TIER_COLORS.LV;
}

async function fetchMachines() {
  if (props.hideMachines || !props.datasetId || !props.categoryId) {
    machines.value = [];
    return;
  }
  try {
    machines.value = await listCategoryMachines(props.datasetId, props.categoryId);
  } catch {
    machines.value = [];
  }
}

async function fetchTiers() {
  if (!props.datasetId || !props.categoryId) {
    tiers.value = [];
    return;
  }
  try {
    tiers.value = await listCategoryVoltageTiers(props.datasetId, props.categoryId, {
      target: props.target ?? undefined,
      kind: props.kind ?? undefined,
    });
  } catch {
    tiers.value = [];
  }
}

function selectTier(tier: string | null) {
  if (tier === props.activeTier) return;
  emit('update:activeTier', tier);
}

function openMachine(m: CategoryMachine) {
  router.push({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: m.itemVariantId, kind: 'detail' },
  });
}

watch(
  () => [props.datasetId, props.categoryId, props.hideMachines, props.target, props.kind],
  () => {
    fetchMachines();
    fetchTiers();
  },
  { immediate: true },
);
</script>

<template>
  <div v-if="machines.length > 0 || tiers.length > 0" class="header-rows">
    <section v-if="!hideMachines && machines.length > 0" class="row">
      <div class="row-label">
        <span>{{ t('category.applicableMachines') }}</span>
        <span class="row-count">{{ machines.length }}</span>
      </div>
      <div class="machines">
        <el-tooltip
          v-for="m in machines"
          :key="m.itemVariantId"
          :content="m.displayName ?? m.itemVariantId"
          placement="top"
          :show-after="100"
        >
          <button type="button" class="machine-cell" @click="openMachine(m)">
            <img
              v-if="m.assetUrl"
              :src="m.assetUrl"
              :alt="m.displayName ?? m.itemVariantId"
              class="machine-icon"
            />
            <span v-else class="machine-icon placeholder" />
          </button>
        </el-tooltip>
      </div>
    </section>

    <section v-if="tiers.length > 0" class="row">
      <div class="row-label">
        <span>{{ t('category.voltage') }}</span>
        <span class="row-count">{{ tiers.length }}</span>
      </div>
      <div class="tier-chips">
        <button
          type="button"
          class="chip neutral"
          :class="{ active: activeTier === null }"
          @click="selectTier(null)"
        >
          <span class="chip-name">{{ t('category.allTiers') }}</span>
          <span class="chip-badge">{{ tiersTotal.toLocaleString() }}</span>
        </button>
        <button
          v-for="t in tiers"
          :key="t.tier"
          type="button"
          class="chip tier-chip"
          :class="{ active: activeTier === t.tier }"
          :style="{
            '--tier-fg': tierColor(t.tier).fg,
            '--tier-bg': tierColor(t.tier).bg,
          }"
          @click="selectTier(t.tier)"
        >
          <span class="chip-name">{{ t.tier }}</span>
          <span class="chip-badge">{{ t.recipeCount.toLocaleString() }}</span>
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.header-rows {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}
.row {
  display: grid;
  grid-template-columns: 88px 1fr;
  align-items: center;
  gap: 12px;
  min-height: 44px;
}
.row-label {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}
.row-count {
  font-size: 11px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  font-variant-numeric: tabular-nums;
}
.machines {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 44px;
  gap: 6px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 2px 2px 4px;
  scrollbar-width: thin;
}
.machine-cell {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 5px;
  background: var(--el-bg-color);
  cursor: pointer;
  padding: 2px;
  transition:
    border-color 0.12s,
    background-color 0.12s;
}
.machine-cell:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.machine-icon {
  width: 40px;
  height: 40px;
  image-rendering: pixelated;
}
.machine-icon.placeholder {
  background: var(--el-fill-color-darker);
  border-radius: 3px;
}
.tier-chips {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 6px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 2px 2px 4px;
  scrollbar-width: thin;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 12px;
  border: 1px solid;
  font-size: 12px;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-weight: 600;
  transition:
    background-color 0.12s,
    border-color 0.12s,
    color 0.12s;
}
.chip.neutral {
  background: var(--el-bg-color);
  border-color: var(--el-border-color);
  color: var(--el-text-color-regular);
}
.chip.neutral:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}
.chip.neutral.active {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: var(--el-color-white);
}
.chip.tier-chip {
  background: var(--tier-bg);
  border-color: var(--tier-fg);
  color: var(--tier-fg);
}
.chip.tier-chip:hover {
  background: var(--tier-fg);
  color: #fff;
}
.chip.tier-chip.active {
  background: var(--tier-fg);
  border-color: var(--tier-fg);
  color: #fff;
}
.chip-name {
  font-weight: 700;
}
.chip-badge {
  font-size: 11px;
  font-weight: 500;
  opacity: 0.85;
}
.chip.neutral.active .chip-badge,
.chip.tier-chip.active .chip-badge,
.chip.tier-chip:hover .chip-badge {
  opacity: 1;
}
</style>
