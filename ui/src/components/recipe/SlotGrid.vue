<script setup lang="ts">
import { computed } from 'vue'
import type { RecipeSlot } from '@/api/recipes.types'
import SlotCell from '../SlotCell.vue'

const props = withDefaults(
  defineProps<{
    slots: RecipeSlot[]
    declaredW?: number | null
    declaredH?: number | null
    shapeless?: boolean
    pickHint?: string
  }>(),
  { declaredW: null, declaredH: null, shapeless: false, pickHint: undefined },
)

defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
}>()

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

const grid = computed<GridSpec>(() => {
  const w = props.declaredW ?? 0
  const h = props.declaredH ?? 0
  if (!props.shapeless && w > 0 && h > 0) {
    const cells: (RecipeSlot | null)[] = new Array(w * h).fill(null)
    for (const s of props.slots) {
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
  const cols = fallbackCols(props.slots)
  const rows = Math.max(1, Math.ceil(props.slots.length / cols))
  return {
    cols,
    rows,
    cells: props.slots,
    declared: false,
    cellSize: pickCellSize(props.slots.length),
  }
})

defineExpose({ grid })
</script>

<template>
  <div
    class="slot-grid"
    :style="{ gridTemplateColumns: `repeat(${grid.cols}, ${grid.cellSize}px)` }"
  >
    <template v-for="(s, idx) in grid.cells" :key="idx">
      <slot name="cell" :slot-data="s" :index="idx" :cell-size="grid.cellSize" :declared="grid.declared">
        <SlotCell
          :slot="s"
          :size="grid.cellSize"
          :placeholder="grid.declared && !s"
          :pick-hint="pickHint"
          @pick="$emit('pick', $event)"
          @lookup="(kind, payload) => $emit('lookup', kind, payload)"
        />
      </slot>
    </template>
  </div>
</template>

<style scoped>
.slot-grid {
  display: grid;
  gap: 4px;
  justify-content: start;
}
</style>
