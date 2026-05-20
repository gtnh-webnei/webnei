<script setup lang="ts">
import { computed } from 'vue'
import type { Recipe, RecipeCategory, RecipeSlot } from '@/api/recipes.types'
import SlotCell from './SlotCell.vue'
import GregTechBadge from './GregTechBadge.vue'

const props = defineProps<{
  recipe: Recipe
  category?: RecipeCategory | null
  pickHint?: string
}>()

const emit = defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
}>()

const itemInputs = computed(() => filterRole('item_input'))
const itemOutputs = computed(() => filterRole('item_output'))
const fluidInputs = computed(() => filterRole('fluid_input'))
const fluidOutputs = computed(() => filterRole('fluid_output'))

function filterRole(role: string): RecipeSlot[] {
  return [...props.recipe.slots]
    .filter((s) => s.role === role)
    .sort((a, b) => a.slotIndex - b.slotIndex)
}

const shapeless = computed(() => !!props.category?.shapeless)

const MAX_AUTO_COLS = 6
const DEFAULT_CELL = 52
const MED_CELL = 36
const SMALL_CELL = 28

function fallbackCols(slots: RecipeSlot[]): number {
  if (slots.length <= 1) return 1
  if (slots.length <= 4) return 2
  if (slots.length <= 9) return 3
  if (slots.length <= 16) return 4
  if (slots.length <= 25) return 5
  return MAX_AUTO_COLS
}

function pickCellSize(totalCells: number): number {
  if (totalCells >= 64) return SMALL_CELL
  if (totalCells >= 36) return MED_CELL
  return DEFAULT_CELL
}

interface GridSpec {
  cols: number
  rows: number
  cells: (RecipeSlot | null)[]
  declared: boolean
  cellSize: number
}

function buildItemGrid(
  slots: RecipeSlot[],
  declaredW: number | undefined,
  declaredH: number | undefined,
): GridSpec {
  const w = declaredW ?? 0
  const h = declaredH ?? 0
  if (!shapeless.value && w > 0 && h > 0) {
    // Shaped: keep slot_index positions, pad to W*H with null placeholders.
    const cells: (RecipeSlot | null)[] = new Array(w * h).fill(null)
    for (const s of slots) {
      if (s.slotIndex >= 0 && s.slotIndex < cells.length) {
        cells[s.slotIndex] = s
      } else {
        cells.push(s)
      }
    }
    return {
      cols: w,
      rows: h,
      cells,
      declared: true,
      cellSize: pickCellSize(w * h),
    }
  }
  // Shapeless / no declared dims: pack tightly.
  const cols = fallbackCols(slots)
  const rows = Math.max(1, Math.ceil(slots.length / cols))
  return {
    cols,
    rows,
    cells: slots,
    declared: false,
    cellSize: pickCellSize(slots.length),
  }
}

const itemInputGrid = computed(() => {
  if (isInscriber.value) {
    const remap: Record<number, number> = { 0: 0, 1: 3, 2: 4 }
    const remapped = itemInputs.value.map((s) => ({
      ...s,
      slotIndex: remap[s.slotIndex] ?? s.slotIndex,
    }))
    return buildItemGrid(remapped, 2, 3)
  }
  return buildItemGrid(
    itemInputs.value,
    props.category?.itemInputWidth,
    props.category?.itemInputHeight,
  )
})
const itemOutputGrid = computed(() =>
  buildItemGrid(itemOutputs.value, props.category?.itemOutputWidth, props.category?.itemOutputHeight),
)
const hasInputs = computed(() => itemInputs.value.length + fluidInputs.value.length > 0)
const hasOutputs = computed(() => itemOutputs.value.length + fluidOutputs.value.length > 0)

const isInscriber = computed(
  () => props.recipe.categoryId === 'appliedenergistics2:inscriber',
)

const descriptionLines = computed(() =>
  props.recipe.description
    ? props.recipe.description.split(/\r?\n/).map((l) => l.trim()).filter((l) => l.length > 0)
    : [],
)

const pickHint = computed(() => props.pickHint ?? '左键 · 切换查看此物品 / 右键 · 合成来源 / 中键 · 用途去向')

function onPick(payload: { itemVariantId: string | null; fluidVariantId: string | null }) {
  emit('pick', payload)
}
function onLookup(
  kind: 'recipe' | 'usage',
  payload: { itemVariantId: string | null; fluidVariantId: string | null },
) {
  emit('lookup', kind, payload)
}
</script>

<template>
  <el-card class="recipe-panel" shadow="never">
    <template #header>
      <div class="header">
        <div class="title-block">
          <div class="title">{{ recipe.categoryDisplayName }}</div>
          <div class="sub">
            <el-tag size="small" type="info" effect="plain" round>
              {{ recipe.sourcePlugin }}
            </el-tag>
            <el-tag v-if="shapeless" size="small" type="warning" effect="dark" round>
              无序合成<span v-if="itemInputs.length"> · {{ itemInputs.length }} 格</span>
            </el-tag>
            <el-tag v-else-if="itemInputGrid.declared" size="small" type="success" effect="plain" round>
              有序 {{ itemInputGrid.cols }}×{{ itemInputGrid.rows }}
            </el-tag>
            <code class="recipe-id" :title="recipe.recipeId">{{ recipe.recipeId }}</code>
          </div>
        </div>
      </div>
    </template>

    <div class="body">
      <GregTechBadge v-if="recipe.gregtech" :info="recipe.gregtech" />

      <div v-if="recipe.description" class="description-block">
        <div v-for="(line, idx) in descriptionLines" :key="idx" class="description-line">
          {{ line }}
        </div>
      </div>

      <section v-if="hasInputs" class="side inputs">
        <div class="side-header">
          <div class="side-label">输入</div>
          <div class="side-meta">
            <span v-if="itemInputs.length" class="meta-chip">
              <span class="dot item" />物品 {{ itemInputs.length }}
            </span>
            <span v-if="fluidInputs.length" class="meta-chip">
              <span class="dot fluid" />流体 {{ fluidInputs.length }}
            </span>
          </div>
        </div>
        <div v-if="itemInputGrid.cells.length" class="group">
          <div
            class="slot-grid"
            :class="{ 'slot-grid-inscriber': isInscriber }"
            :style="{ gridTemplateColumns: `repeat(${itemInputGrid.cols}, ${itemInputGrid.cellSize}px)` }"
          >
            <template v-for="(s, idx) in itemInputGrid.cells" :key="`ii-${idx}`">
              <div
                v-if="isInscriber && idx === 1"
                class="inscriber-arrow"
                aria-hidden="true"
              >
                <svg viewBox="0 0 52 52" width="100%" height="100%">
                  <path
                    d="M 4 26 H 26 V 48"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="4"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M 20 42 L 26 50 L 32 42"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="4"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
              <div
                v-else-if="isInscriber && idx === 5"
                class="inscriber-arrow"
                aria-hidden="true"
              >
                <svg viewBox="0 0 52 52" width="100%" height="100%">
                  <path
                    d="M 4 26 H 26 V 4"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="4"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M 20 10 L 26 2 L 32 10"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="4"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
              <div
                v-else-if="isInscriber && idx === 2"
                class="inscriber-empty"
                aria-hidden="true"
              />
              <SlotCell
                v-else
                :slot="s"
                :size="itemInputGrid.cellSize"
                :placeholder="itemInputGrid.declared && !s"
                :pick-hint="pickHint"
                @pick="onPick"
                @lookup="onLookup"
              />
            </template>
          </div>
        </div>
        <div v-if="fluidInputs.length" class="group">
          <div class="fluid-list">
            <SlotCell
              v-for="s in fluidInputs"
              :key="`fi-${s.slotIndex}`"
              :slot="s"
              is-fluid
              :pick-hint="pickHint"
              @pick="onPick"
              @lookup="onLookup"
            />
          </div>
        </div>
      </section>

      <div v-if="hasInputs && hasOutputs" class="divider">
        <span class="divider-line" />
        <span class="divider-label">产出</span>
        <span class="divider-line" />
      </div>

      <section v-if="hasOutputs" class="side outputs">
        <div class="side-header">
          <div class="side-label">输出</div>
          <div class="side-meta">
            <span v-if="itemOutputs.length" class="meta-chip">
              <span class="dot item" />物品 {{ itemOutputs.length }}
            </span>
            <span v-if="fluidOutputs.length" class="meta-chip">
              <span class="dot fluid" />流体 {{ fluidOutputs.length }}
            </span>
          </div>
        </div>
        <div v-if="itemOutputGrid.cells.length" class="group">
          <div
            class="slot-grid"
            :style="{ gridTemplateColumns: `repeat(${itemOutputGrid.cols}, ${itemOutputGrid.cellSize}px)` }"
          >
            <SlotCell
              v-for="(s, idx) in itemOutputGrid.cells"
              :key="`io-${idx}`"
              :slot="s"
              :size="itemOutputGrid.cellSize"
              :placeholder="itemOutputGrid.declared && !s"
              :pick-hint="pickHint"
              @pick="onPick"
              @lookup="onLookup"
            />
          </div>
        </div>
        <div v-if="fluidOutputs.length" class="group">
          <div class="fluid-list">
            <SlotCell
              v-for="s in fluidOutputs"
              :key="`fo-${s.slotIndex}`"
              :slot="s"
              is-fluid
              :pick-hint="pickHint"
              @pick="onPick"
              @lookup="onLookup"
            />
          </div>
        </div>
      </section>
    </div>
  </el-card>
</template>

<style scoped>
.recipe-panel {
  height: 100%;
}
.header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.title-block {
  min-width: 0;
}
.title {
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sub {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  flex-wrap: wrap;
}
.recipe-id {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  word-break: break-all;
  flex-basis: 100%;
}
.body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.side {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}
.side.empty {
  opacity: 0.6;
}
.side-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.side-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.side-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  border-radius: 10px;
  padding: 1px 8px 1px 6px;
}
.meta-chip .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}
.meta-chip .dot.item {
  background: var(--el-color-info);
}
.meta-chip .dot.fluid {
  background: rgba(33, 130, 230, 0.85);
}
.group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.slot-grid {
  display: grid;
  gap: 4px;
  justify-content: start;
}
.fluid-list {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.divider {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 4px 0;
}
.divider-line {
  flex: 1;
  height: 1px;
  background: var(--el-border-color);
}
.divider-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  letter-spacing: 0.5px;
  background: var(--el-fill-color-light);
  padding: 2px 10px;
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter);
}
.divider-label::before {
  content: '↓ ';
  color: var(--el-color-primary);
  font-weight: 700;
}
.placeholder-text {
  color: var(--el-text-color-disabled);
  font-size: 12px;
  padding: 4px 0;
}
.description-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-left: 3px solid var(--el-color-primary);
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
}
.description-line {
  white-space: pre-wrap;
  word-break: break-word;
}
.inscriber-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  pointer-events: none;
}
.inscriber-empty {
  display: block;
}
</style>
