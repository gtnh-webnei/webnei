<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import type { RecipeSlot } from '@/api/recipes.types'
import SlotCell from '../SlotCell.vue'

const props = withDefaults(
  defineProps<{
    slots: RecipeSlot[]
    declaredW?: number | null
    declaredH?: number | null
    shapeless?: boolean
    pickHint?: string
    showProbabilityBadge?: boolean
  }>(),
  {
    declaredW: null,
    declaredH: null,
    shapeless: false,
    pickHint: undefined,
    showProbabilityBadge: true,
  },
)

defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
}>()

const MAX_COLS = 9
const GAP = 4
const MIN_CELL = 24
const MAX_CELL = 52

function fallbackCols(slots: RecipeSlot[]): number {
  return Math.min(Math.max(1, slots.length), MAX_COLS)
}

const root = ref<HTMLElement | null>(null)
const containerWidth = ref(0)

let ro: ResizeObserver | null = null
onMounted(() => {
  if (!root.value) return
  containerWidth.value = root.value.clientWidth
  ro = new ResizeObserver((entries) => {
    for (const e of entries) containerWidth.value = e.contentRect.width
  })
  ro.observe(root.value)
})
onBeforeUnmount(() => {
  ro?.disconnect()
  ro = null
})

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
  let cols: number
  let rows: number
  let cells: (RecipeSlot | null)[]
  let declared: boolean
  // declaredW > MAX_COLS means the exporter only knows the flat slot upper bound
  // (e.g. EOH fluid_output=18 max), not real per-recipe layout. Treat as undeclared
  // and pack actual slots into <= 9 columns instead of leaving phantom placeholders.
  const usable = !props.shapeless && w > 0 && h > 0 && w <= MAX_COLS
  if (usable) {
    const total = w * h
    cells = new Array(total).fill(null)
    for (const s of props.slots) {
      if (s.slotIndex >= 0 && s.slotIndex < cells.length) {
        cells[s.slotIndex] = s
      } else {
        cells.push(s)
      }
    }
    cols = w
    rows = Math.max(h, Math.ceil(cells.length / Math.max(1, w)))
    declared = true
  } else {
    cols = fallbackCols(props.slots)
    rows = Math.max(1, Math.ceil(props.slots.length / cols))
    cells = props.slots
    declared = false
  }
  // Single-cell size based on a 9-column reference so cell size stays consistent
  // across recipes regardless of how many columns this particular grid uses.
  const cw = containerWidth.value
  let cellSize = MAX_CELL
  if (cw > 0) {
    const ref = (cw - GAP * (MAX_COLS - 1)) / MAX_COLS
    cellSize = Math.max(MIN_CELL, Math.min(MAX_CELL, Math.floor(ref)))
  }
  return { cols, rows, cells, declared, cellSize }
})

defineExpose({ grid })
</script>

<template>
  <div
    ref="root"
    class="slot-grid"
    :style="{
      gridTemplateColumns: `repeat(${grid.cols}, ${grid.cellSize}px)`,
      gap: GAP + 'px',
    }"
  >
    <template v-for="(s, idx) in grid.cells" :key="idx">
      <slot name="cell" :slot-data="s" :index="idx" :cell-size="grid.cellSize" :declared="grid.declared">
        <SlotCell
          :slot="s"
          :size="grid.cellSize"
          :placeholder="grid.declared && !s"
          :pick-hint="pickHint"
          :show-probability-badge="showProbabilityBadge"
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
  justify-content: start;
}
</style>
