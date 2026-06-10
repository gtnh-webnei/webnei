<script setup lang="ts">
import { computed, ref } from 'vue';
import { RecycleScroller } from 'vue-virtual-scroller';
import { useI18n } from 'vue-i18n';
import type { RecipeSlot, RecipeSlotCandidate } from '@/api/recipes.types';
import { formatCompact, formatFluidCompact, formatFluidFull, formatFull } from '@/utils/format';
import AppTooltip from './AppTooltip.vue';
import FluidTooltipContent from './FluidTooltipContent.vue';
import ItemTooltipContent from './ItemTooltipContent.vue';

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    slot: RecipeSlot | null;
    size?: number;
    isFluid?: boolean;
    placeholder?: boolean;
    pickHint?: string;
    showProbabilityBadge?: boolean;
  }>(),
  {
    size: 52,
    isFluid: false,
    placeholder: false,
    pickHint: '',
    showProbabilityBadge: true,
  },
);

const displayPickHint = computed(() => props.pickHint || t('common.pickHintSlot'));

const emit = defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void;
  (
    e: 'lookup',
    kind: 'recipe' | 'usage',
    payload: { itemVariantId: string | null; fluidVariantId: string | null },
  ): void;
}>();

const direct = computed<RecipeSlotCandidate | null>(() => {
  const s = props.slot;
  if (!s) return null;
  if (!s.itemVariantId && !s.fluidVariantId) return null;
  return {
    itemVariantId: s.itemVariantId,
    fluidVariantId: s.fluidVariantId,
    amount: s.amount,
    displayName: s.displayName,
    modId: s.modId,
    modName: s.modName,
    fluidGaseous: s.fluidGaseous,
    fluidTemperature: s.fluidTemperature,
    tooltipText: s.tooltipText,
    assetUrl: s.assetUrl,
  };
});

const candidates = computed(() => props.slot?.candidates ?? []);
const candidateRows = computed(() =>
  candidates.value.map((c, idx) => ({
    ...c,
    key: `${c.itemVariantId ?? ''}|${c.fluidVariantId ?? ''}|${idx}`,
  })),
);
const candidateListHeight = computed(() => Math.min(candidateRows.value.length * 46, 320));
const hasMultiple = computed(() => !direct.value && candidates.value.length > 1);
const primary = computed<RecipeSlotCandidate | null>(() => {
  return direct.value ?? candidates.value[0] ?? null;
});
const extraCount = computed(() => {
  if (direct.value) return 0;
  return Math.max(0, candidates.value.length - 1);
});

const probability = computed(() => props.slot?.probability ?? 1);
const probPercent = computed(() => Math.round(probability.value * 100));
const probPercentPrecise = computed(() => {
  const pct = probability.value * 100;
  if (pct >= 10) return pct.toFixed(0) + '%';
  if (pct >= 1) return pct.toFixed(1) + '%';
  if (pct >= 0.01) return pct.toFixed(2) + '%';
  if (pct > 0) return '<0.01%';
  return '0%';
});
const hasNonTrivialProb = computed(() => probability.value > 0 && probability.value < 1);
const showProb = computed(() => props.showProbabilityBadge && hasNonTrivialProb.value);

const amount = computed(() => primary.value?.amount ?? 0);
const showAmount = computed(() => amount.value >= 1);
const amountCompact = computed(() =>
  props.isFluid ? formatFluidCompact(amount.value) : formatCompact(amount.value),
);
const amountFull = computed(() =>
  props.isFluid ? formatFluidFull(amount.value) : formatFull(amount.value),
);
const primaryTooltipContext = computed(() => ({
  hint: displayPickHint.value,
  amountLabel: amount.value >= 1 ? amountFull.value : null,
  probabilityLabel:
    primary.value?.fluidVariantId || !hasNonTrivialProb.value ? null : probPercentPrecise.value,
}));
const popoverVisible = ref(false);

function onClick() {
  if (hasMultiple.value) {
    popoverVisible.value = !popoverVisible.value;
    return;
  }
  if (!primary.value) return;
  emit('pick', {
    itemVariantId: primary.value.itemVariantId,
    fluidVariantId: primary.value.fluidVariantId,
  });
}

function onContextMenu(e: MouseEvent) {
  e.preventDefault();
  if (!primary.value) return;
  emit('lookup', 'recipe', {
    itemVariantId: primary.value.itemVariantId,
    fluidVariantId: primary.value.fluidVariantId,
  });
}

function onAuxClick(e: MouseEvent) {
  if (e.button !== 1) return;
  e.preventDefault();
  if (!primary.value) return;
  emit('lookup', 'usage', {
    itemVariantId: primary.value.itemVariantId,
    fluidVariantId: primary.value.fluidVariantId,
  });
}

function pickCandidate(c: RecipeSlotCandidate) {
  emit('pick', {
    itemVariantId: c.itemVariantId,
    fluidVariantId: c.fluidVariantId,
  });
  popoverVisible.value = false;
}

function lookupCandidate(kind: 'recipe' | 'usage', c: RecipeSlotCandidate, e?: MouseEvent) {
  if (e) e.preventDefault();
  emit('lookup', kind, {
    itemVariantId: c.itemVariantId,
    fluidVariantId: c.fluidVariantId,
  });
  popoverVisible.value = false;
}

function hasCandidateHover(c: RecipeSlotCandidate) {
  return !!c.tooltipText || !!c.fluidVariantId;
}
function candidateAmountLabel(c: RecipeSlotCandidate) {
  if (c.amount < 1) return null;
  return c.fluidVariantId ? formatFluidFull(c.amount) : formatFull(c.amount);
}
</script>

<template>
  <!-- Multi-candidate: popover only, no tooltip wrapper (avoids nested reference) -->
  <el-popover
    v-if="hasMultiple"
    v-model:visible="popoverVisible"
    placement="top"
    trigger="manual"
    :width="300"
  >
    <template #reference>
      <div
        class="cell"
        :class="{ fluid: isFluid, clickable: true }"
        :style="{ width: size + 'px', height: size + 'px', '--cell-size': size + 'px' }"
        @click="onClick"
        @contextmenu="onContextMenu"
        @auxclick="onAuxClick"
      >
        <img
          v-if="primary?.assetUrl"
          :src="primary.assetUrl"
          :alt="primary.displayName ?? ''"
          loading="lazy"
          @error="($event.target as HTMLImageElement).style.display = 'none'"
        >
        <span
          v-if="!primary?.assetUrl && isFluid"
          class="cell-fallback"
        >
          {{ t('fluid.liquid') }}
        </span>
        <span
          v-if="showAmount"
          class="amount"
        >{{ amountCompact }}</span>
        <span class="badge">+{{ extraCount }}</span>
        <span
          v-if="showProb"
          class="prob"
        >{{ probPercent }}%</span>
      </div>
    </template>

    <div class="popover-list">
      <div class="popover-summary">
        <div class="popover-title">
          {{ t('recipe.candidates', { count: candidates.length }) }}
        </div>
        <div class="popover-hint">
          {{ displayPickHint }}
        </div>
      </div>
      <RecycleScroller
        v-slot="{ item: c }"
        class="popover-items"
        :items="candidateRows"
        :item-size="46"
        :style="{ height: candidateListHeight + 'px' }"
        key-field="key"
      >
        <AppTooltip
          :disabled="!hasCandidateHover(c)"
          placement="right"
        >
          <template #content>
            <FluidTooltipContent
              v-if="c.fluidVariantId"
              :fluid="c"
              :context="{ amountLabel: candidateAmountLabel(c) }"
            />
            <ItemTooltipContent
              v-else
              :item="c"
              :context="{ amountLabel: candidateAmountLabel(c) }"
            />
          </template>
          <div
            class="popover-item"
            @click="pickCandidate(c)"
            @contextmenu="lookupCandidate('recipe', c, $event)"
            @auxclick="(e: MouseEvent) => e.button === 1 && lookupCandidate('usage', c, e)"
          >
            <img
              v-if="c.assetUrl"
              :key="c.assetUrl"
              :src="c.assetUrl"
              :alt="c.displayName ?? ''"
              loading="lazy"
            >
            <div class="popover-text">
              <div class="popover-name">
                {{ c.displayName ?? c.itemVariantId ?? c.fluidVariantId }}
              </div>
              <div class="popover-sub">
                <el-tag
                  v-if="c.modName"
                  size="small"
                  type="info"
                  effect="plain"
                  round
                  class="mod-tag"
                >
                  {{ c.modName }}
                </el-tag>
                <span
                  v-if="c.amount >= 1"
                  class="qty"
                >×{{ isFluid ? formatFluidFull(c.amount) : formatFull(c.amount) }}</span>
              </div>
            </div>
          </div>
        </AppTooltip>
      </RecycleScroller>
    </div>
  </el-popover>

  <!-- Single candidate / placeholder: tooltip only -->
  <AppTooltip
    v-else
    :disabled="!primary"
  >
    <template #content>
      <FluidTooltipContent
        v-if="primary?.fluidVariantId"
        :fluid="primary"
        :context="primaryTooltipContext"
      />
      <ItemTooltipContent
        v-else-if="primary"
        :item="primary"
        :context="primaryTooltipContext"
      />
    </template>
    <div
      class="cell"
      :class="{
        fluid: isFluid,
        clickable: !!primary,
        placeholder: placeholder && !primary,
      }"
      :style="{ width: size + 'px', height: size + 'px', '--cell-size': size + 'px' }"
      @click="onClick"
      @contextmenu="onContextMenu"
      @auxclick="onAuxClick"
    >
      <img
        v-if="primary?.assetUrl"
        :src="primary.assetUrl"
        :alt="primary.displayName ?? ''"
        loading="lazy"
      >
      <span
        v-if="showAmount"
        class="amount"
      >{{ amountCompact }}</span>
      <span
        v-if="showProb"
        class="prob"
      >{{ probPercent }}%</span>
    </div>
  </AppTooltip>
</template>

<style scoped>
.cell {
  position: relative;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  transition:
    border-color 0.15s,
    transform 0.05s;
}
.cell.clickable {
  cursor: pointer;
}
.cell.clickable:hover {
  border-color: var(--el-color-primary);
}
.cell.clickable:active {
  transform: scale(0.97);
}
.cell.fluid {
  background: rgba(33, 130, 230, 0.12);
  border-color: rgba(33, 130, 230, 0.35);
}
.cell-fallback {
  font-size: 16px;
  font-weight: 700;
  color: rgba(33, 130, 230, 0.85);
  pointer-events: none;
}
.cell.placeholder {
  background: repeating-linear-gradient(
    45deg,
    var(--el-fill-color-light) 0 6px,
    var(--el-fill-color) 6px 12px
  );
  border-style: dashed;
  border-color: var(--el-border-color-lighter);
  opacity: 0.55;
}
.cell img {
  width: 78%;
  height: 78%;
  object-fit: contain;
  image-rendering: pixelated;
  pointer-events: none;
}
.amount {
  position: absolute;
  right: 2px;
  bottom: 1px;
  font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
  font-size: clamp(8px, calc(var(--cell-size, 52px) * 0.24), 11px);
  font-weight: 600;
  color: #fff;
  text-shadow:
    0 0 2px rgba(0, 0, 0, 0.8),
    0 1px 1px rgb(0, 0, 0);
  pointer-events: none;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  letter-spacing: -0.02em;
}
.badge {
  position: absolute;
  left: 3px;
  top: 1px;
  font-size: clamp(7px, calc(var(--cell-size, 52px) * 0.22), 10px);
  font-weight: 700;
  color: #fff;
  background: var(--el-color-primary);
  padding: 1px 4px;
  border-radius: 8px;
  pointer-events: none;
  line-height: 1;
}
.prob {
  position: absolute;
  right: 2px;
  top: 1px;
  font-size: clamp(7px, calc(var(--cell-size, 52px) * 0.22), 10px);
  color: #fff;
  background: rgba(245, 108, 108, 0.85);
  padding: 1px 3px;
  border-radius: 4px;
  pointer-events: none;
  line-height: 1.2;
}
.popover-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.popover-summary {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.popover-title {
  font-size: 13px;
  font-weight: 600;
}
.popover-hint {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
.popover-items {
  width: 100%;
  max-height: 320px;
  overflow-y: auto;
}
.popover-item {
  display: flex;
  gap: 8px;
  align-items: center;
  height: 44px;
  margin-bottom: 2px;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
}
.popover-item:hover {
  background: var(--el-fill-color-light);
}
.popover-item img {
  width: 32px;
  height: 32px;
  object-fit: contain;
  image-rendering: pixelated;
  flex-shrink: 0;
}
.popover-text {
  flex: 1;
  min-width: 0;
}
.popover-name {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.popover-sub {
  display: flex;
  gap: 6px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}
.popover-sub .qty {
  color: var(--el-color-primary);
  font-weight: 600;
}
.mod-tag {
  max-width: 100%;
  min-width: 0;
}
.mod-tag :deep(.el-tag__content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
