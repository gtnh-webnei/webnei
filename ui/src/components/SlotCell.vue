<script setup lang="ts">
import { computed, ref } from 'vue'
import type { RecipeSlot, RecipeSlotCandidate } from '@/api/recipes.types'

const props = withDefaults(
  defineProps<{
    slot: RecipeSlot | null
    size?: number
    isFluid?: boolean
    placeholder?: boolean
    pickHint?: string
  }>(),
  {
    size: 52,
    isFluid: false,
    placeholder: false,
    pickHint: '左键 · 切换查看此物品 / 右键 · 合成来源 / 中键 · 用途去向',
  },
)

const emit = defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
}>()

const direct = computed<RecipeSlotCandidate | null>(() => {
  const s = props.slot
  if (!s) return null
  if (!s.itemVariantId && !s.fluidVariantId) return null
  return {
    itemVariantId: s.itemVariantId,
    fluidVariantId: s.fluidVariantId,
    amount: s.amount,
    displayName: s.displayName,
    modId: s.modId,
    assetUrl: s.assetUrl,
  }
})

const candidates = computed(() => props.slot?.candidates ?? [])
const hasMultiple = computed(() => !direct.value && candidates.value.length > 1)
const primary = computed<RecipeSlotCandidate | null>(() => {
  return direct.value ?? candidates.value[0] ?? null
})
const extraCount = computed(() => {
  if (direct.value) return 0
  return Math.max(0, candidates.value.length - 1)
})

const probability = computed(() => props.slot?.probability ?? 1)
const probPercent = computed(() => Math.round(probability.value * 100))
const showProb = computed(() => probability.value > 0 && probability.value < 1)

const amount = computed(() => primary.value?.amount ?? 0)
const showAmount = computed(() => amount.value >= 1)

const popoverVisible = ref(false)

function onClick(e: MouseEvent) {
  if (hasMultiple.value) {
    popoverVisible.value = !popoverVisible.value
    return
  }
  if (!primary.value) return
  emit('pick', {
    itemVariantId: primary.value.itemVariantId,
    fluidVariantId: primary.value.fluidVariantId,
  })
}

function onContextMenu(e: MouseEvent) {
  e.preventDefault()
  if (!primary.value) return
  emit('lookup', 'recipe', {
    itemVariantId: primary.value.itemVariantId,
    fluidVariantId: primary.value.fluidVariantId,
  })
}

function onAuxClick(e: MouseEvent) {
  if (e.button !== 1) return
  e.preventDefault()
  if (!primary.value) return
  emit('lookup', 'usage', {
    itemVariantId: primary.value.itemVariantId,
    fluidVariantId: primary.value.fluidVariantId,
  })
}

function pickCandidate(c: RecipeSlotCandidate) {
  emit('pick', {
    itemVariantId: c.itemVariantId,
    fluidVariantId: c.fluidVariantId,
  })
  popoverVisible.value = false
}

function lookupCandidate(kind: 'recipe' | 'usage', c: RecipeSlotCandidate, e?: MouseEvent) {
  if (e) e.preventDefault()
  emit('lookup', kind, {
    itemVariantId: c.itemVariantId,
    fluidVariantId: c.fluidVariantId,
  })
  popoverVisible.value = false
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
        :style="{ width: size + 'px', height: size + 'px' }"
        @click="onClick"
        @contextmenu="onContextMenu"
        @auxclick="onAuxClick"
      >
        <img
          v-if="primary?.assetUrl"
          :src="primary.assetUrl"
          :alt="primary.displayName ?? ''"
          loading="lazy"
          @error="($event.target as HTMLImageElement).style.display='none'"
        />
        <span v-if="!primary?.assetUrl && isFluid" class="cell-fallback">
          {{ '液' }}
        </span>
        <span v-if="showAmount" class="amount">{{ amount }}</span>
        <span class="badge">+{{ extraCount }}</span>
        <span v-if="showProb" class="prob">{{ probPercent }}%</span>
      </div>
    </template>

    <div class="popover-list">
      <div class="popover-summary">
        <div class="popover-title">{{ candidates.length }} 个候选</div>
        <div class="popover-hint">{{ pickHint }}</div>
      </div>
      <div class="popover-items">
        <div
          v-for="(c, idx) in candidates"
          :key="`${c.itemVariantId ?? ''}|${c.fluidVariantId ?? ''}|${idx}`"
          class="popover-item"
          @click="pickCandidate(c)"
          @contextmenu="lookupCandidate('recipe', c, $event)"
          @auxclick="(e: MouseEvent) => e.button === 1 && lookupCandidate('usage', c, e)"
        >
          <img
            v-if="c.assetUrl"
            :src="c.assetUrl"
            :alt="c.displayName ?? ''"
            loading="lazy"
          />
          <div class="popover-text">
            <div class="popover-name">{{ c.displayName ?? c.itemVariantId ?? c.fluidVariantId }}</div>
            <div class="popover-sub">
              <span v-if="c.modId" class="mod">{{ c.modId }}</span>
              <span v-if="c.amount >= 1" class="qty">×{{ c.amount }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-popover>

  <!-- Single candidate / placeholder: tooltip only -->
  <el-tooltip
    v-else
    placement="top"
    :show-after="120"
    :hide-after="80"
    :disabled="!primary"
    effect="light"
  >
    <template #content>
      <div v-if="primary" class="hover-card">
        <div class="hover-name">
          {{ primary.displayName ?? primary.itemVariantId ?? primary.fluidVariantId }}
        </div>
        <div class="hover-meta">
          <el-tag v-if="primary.modId" size="small" type="info" effect="plain" round>
            {{ primary.modId }}
          </el-tag>
          <el-tag v-if="isFluid" size="small" type="primary" effect="plain" round>流体</el-tag>
          <span v-if="amount >= 1" class="hover-amount">×{{ amount }}</span>
          <span v-if="showProb" class="hover-prob">{{ probPercent }}%</span>
        </div>
        <code class="hover-id">
          {{ primary.itemVariantId ?? primary.fluidVariantId }}
        </code>
        <div class="hover-keys">{{ pickHint }}</div>
      </div>
    </template>
    <div
      class="cell"
      :class="{
        fluid: isFluid,
        clickable: !!primary,
        placeholder: placeholder && !primary,
      }"
      :style="{ width: size + 'px', height: size + 'px' }"
      @click="onClick"
      @contextmenu="onContextMenu"
      @auxclick="onAuxClick"
    >
      <img
        v-if="primary?.assetUrl"
        :src="primary.assetUrl"
        :alt="primary.displayName ?? ''"
        loading="lazy"
      />
      <span v-if="showAmount" class="amount">{{ amount }}</span>
      <span v-if="showProb" class="prob">{{ probPercent }}%</span>
    </div>
  </el-tooltip>
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
  transition: border-color 0.15s, transform 0.05s;
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
  right: 3px;
  bottom: 1px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  text-shadow:
    1px 1px 0 rgba(0, 0, 0, 0.9),
    -1px 1px 0 rgba(0, 0, 0, 0.9),
    1px -1px 0 rgba(0, 0, 0, 0.9),
    -1px -1px 0 rgba(0, 0, 0, 0.9);
  pointer-events: none;
  font-variant-numeric: tabular-nums;
  line-height: 1;
}
.badge {
  position: absolute;
  left: 3px;
  top: 1px;
  font-size: 10px;
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
  font-size: 10px;
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
  max-height: 320px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.popover-item {
  display: flex;
  gap: 8px;
  align-items: center;
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
.hover-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 280px;
}
.hover-name {
  font-size: 14px;
  font-weight: 600;
  word-break: break-word;
}
.hover-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}
.hover-amount {
  color: var(--el-color-primary);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.hover-prob {
  color: var(--el-color-danger);
  font-weight: 600;
}
.hover-id {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  word-break: break-all;
  background: var(--el-fill-color-light);
  padding: 2px 4px;
  border-radius: 3px;
}
.hover-candidates {
  font-size: 12px;
  color: var(--el-color-primary);
}
.hover-keys {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 6px;
}
</style>
