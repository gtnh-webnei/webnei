<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue';
import { listCategoryApplicableItems, listCategoryVoltageTiers } from '@/api/recipes';
import { useEntityNavigation } from '@/composables/useEntityNavigation';
import type { CategoryApplicableItem, CategoryVoltageTier, LookupKind } from '@/api/recipes.types';
import AppTooltip from '@/components/AppTooltip.vue';

const props = withDefaults(
  defineProps<{
    datasetId: string;
    categoryId: string;
    activeTier: string | null;
    hideApplicableItems?: boolean;
    target?: string | null;
    kind?: LookupKind | null;
    query?: string;
  }>(),
  { hideApplicableItems: false, target: null, kind: null, query: '' },
);

const { t } = useI18n();

const emit = defineEmits<{
  (e: 'update:activeTier', tier: string | null): void;
}>();

const router = useRouter();
const entityNavigation = useEntityNavigation(
  router,
  computed(() => props.datasetId),
);

const applicableItems = ref<CategoryApplicableItem[]>([]);
const applicableItemsExpanded = ref(false);
const applicableItemsOverflow = ref(false);
const visibleColumnCount = ref(1);
const collapsedRowCount = ref(1);
const listEl = ref<HTMLElement | null>(null);
const tiers = ref<CategoryVoltageTier[]>([]);

const tiersTotal = computed(() => tiers.value.reduce((sum, t) => sum + t.recipeCount, 0));
const collapsedVisibleCount = computed(() => {
  if (!applicableItemsOverflow.value) return applicableItems.value.length;
  return Math.max(1, visibleColumnCount.value * collapsedRowCount.value - 1);
});
const visibleApplicableItems = computed(() =>
  applicableItemsExpanded.value
    ? applicableItems.value
    : applicableItems.value.slice(0, collapsedVisibleCount.value),
);
const canToggleApplicableItems = computed(
  () => applicableItemsOverflow.value || applicableItemsExpanded.value,
);

function measureLayout() {
  const el = listEl.value;
  if (!el) {
    applicableItemsOverflow.value = false;
    visibleColumnCount.value = 1;
    collapsedRowCount.value = 1;
    return;
  }
  const style = getComputedStyle(el);
  const cellSize = Number.parseFloat(style.getPropertyValue('--applicable-cell-size')) || 44;
  const gap = Number.parseFloat(style.columnGap || style.gap) || 6;
  const columns = Math.max(1, Math.floor((el.clientWidth + gap) / (cellSize + gap)));
  const rows = Math.max(1, Math.round((el.clientHeight + gap) / (cellSize + gap)));
  visibleColumnCount.value = columns;
  collapsedRowCount.value = rows;
  applicableItemsOverflow.value = applicableItems.value.length > columns * rows;
}

let resizeObserver: ResizeObserver | null = null;
function observeList(el: HTMLElement | null) {
  resizeObserver?.disconnect();
  if (!el) return;
  resizeObserver = new ResizeObserver(() => measureLayout());
  resizeObserver.observe(el);
}
watch(listEl, (el) => observeList(el));
onBeforeUnmount(() => resizeObserver?.disconnect());

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

async function fetchApplicableItems() {
  if (props.hideApplicableItems || !props.datasetId || !props.categoryId) {
    applicableItems.value = [];
    return;
  }
  try {
    applicableItems.value = await listCategoryApplicableItems(props.datasetId, props.categoryId);
  } catch {
    applicableItems.value = [];
  }
  applicableItemsExpanded.value = false;
  await nextTick();
  measureLayout();
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
      q: props.query,
    });
  } catch {
    tiers.value = [];
  }
}

function selectTier(tier: string | null) {
  if (tier === props.activeTier) return;
  emit('update:activeTier', tier);
}

function openApplicableItem(m: CategoryApplicableItem) {
  entityNavigation.pick(m.itemVariantId);
}

watch(
  () => [props.datasetId, props.categoryId, props.hideApplicableItems],
  () => {
    applicableItemsExpanded.value = false;
    fetchApplicableItems();
  },
  { immediate: true },
);

watch(
  () => [props.datasetId, props.categoryId, props.target, props.kind, props.query],
  () => fetchTiers(),
  { immediate: true },
);
</script>

<template>
  <div v-if="applicableItems.length > 0 || tiers.length > 0" class="header-rows">
    <section v-if="!hideApplicableItems && applicableItems.length > 0" class="row">
      <div class="row-label">
        <span>{{ t('category.applicableItems') }}</span>
        <span class="row-count">{{ applicableItems.length }}</span>
      </div>
      <div class="applicable-block">
        <div class="applicable-list-wrap">
          <div ref="listEl" class="applicable-items" :class="{ expanded: applicableItemsExpanded }">
            <AppTooltip
              v-for="m in visibleApplicableItems"
              :key="m.itemVariantId"
              :content="m.displayName ?? m.itemVariantId"
            >
              <button type="button" class="applicable-item-cell" @click="openApplicableItem(m)">
                <img
                  v-if="m.assetUrl"
                  :src="m.assetUrl"
                  :alt="m.displayName ?? m.itemVariantId"
                  class="applicable-item-icon"
                />
                <span v-else class="applicable-item-icon placeholder" />
              </button>
            </AppTooltip>
            <button
              v-if="canToggleApplicableItems"
              type="button"
              class="applicable-item-cell applicable-toggle-cell"
              @click="applicableItemsExpanded = !applicableItemsExpanded"
            >
              <el-icon><ArrowUp v-if="applicableItemsExpanded" /><ArrowDown v-else /></el-icon>
            </button>
          </div>
        </div>
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
          v-for="tier in tiers"
          :key="tier.tier"
          type="button"
          class="chip tier-chip"
          :class="{ active: activeTier === tier.tier }"
          :style="{
            '--tier-fg': tierColor(tier.tier).fg,
            '--tier-bg': tierColor(tier.tier).bg,
          }"
          @click="selectTier(tier.tier)"
        >
          <span class="chip-name">{{ tier.tier }}</span>
          <span class="chip-badge">{{ tier.recipeCount.toLocaleString() }}</span>
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
.applicable-block {
  min-width: 0;
}
.applicable-list-wrap {
  position: relative;
  min-width: 0;
}
.applicable-items {
  --applicable-cell-size: 44px;
  display: grid;
  grid-template-columns: repeat(auto-fill, var(--applicable-cell-size));
  grid-auto-rows: var(--applicable-cell-size);
  gap: 6px;
  max-height: var(--applicable-cell-size);
  overflow: hidden;
  padding: 0;
}
.applicable-items.expanded {
  max-height: 194px;
  overflow-y: auto;
  scrollbar-width: thin;
}
.applicable-item-cell {
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
.applicable-item-cell:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.applicable-toggle-cell {
  color: var(--el-text-color-secondary);
  background: var(--el-bg-color);
  border-color: var(--el-border-color-lighter);
}
.applicable-toggle-cell:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.applicable-toggle-cell .el-icon {
  font-size: 16px;
  font-weight: 700;
}
.applicable-item-icon {
  width: 40px;
  height: 40px;
  image-rendering: pixelated;
}
.applicable-item-icon.placeholder {
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

@media (max-width: 640px) {
  .header-rows {
    padding: 8px;
  }
  .row {
    grid-template-columns: 1fr;
    align-items: stretch;
    gap: 6px;
  }
  .applicable-items {
    --applicable-cell-size: 40px;
  }
  .applicable-items.expanded {
    max-height: 178px;
  }
  .applicable-toggle-cell .el-icon {
    font-size: 18px;
  }
  .tier-chips {
    display: block;
    overflow-x: hidden;
    overflow-y: visible;
    padding: 2px 2px 0;
  }
  .tier-chips .chip {
    margin: 0 6px 6px 0;
    vertical-align: top;
  }
  .applicable-item-cell {
    width: 40px;
    height: 40px;
  }
  .applicable-item-icon {
    width: 36px;
    height: 36px;
  }
}
</style>
