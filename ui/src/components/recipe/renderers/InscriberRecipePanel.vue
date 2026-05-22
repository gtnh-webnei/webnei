<script setup lang="ts">
import { computed } from 'vue'
import type { Recipe, RecipeCategory, RecipeSlot } from '@/api/recipes.types'
import RecipePanelShell from '../RecipePanelShell.vue'
import SlotCell from '../../SlotCell.vue'
import SlotGrid from '../SlotGrid.vue'
import SidesLayout from '../SidesLayout.vue'

const props = defineProps<{
  recipe: Recipe
  category?: RecipeCategory | null
  pickHint?: string
}>()

const emit = defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
}>()

// Inscriber declares 3 input slots in roles 0/1/2, but the 2x3 visual grid
// puts them at positions 0, 3, 4 with arrows at 1, 5 and empty at 2.
const SLOT_REMAP: Record<number, number> = { 0: 0, 1: 3, 2: 4 }

const itemInputs = computed<RecipeSlot[]>(() =>
  props.recipe.slots
    .filter((s) => s.role === 'item_input')
    .map((s) => ({ ...s, slotIndex: SLOT_REMAP[s.slotIndex] ?? s.slotIndex })),
)

const itemOutputs = computed<RecipeSlot[]>(() =>
  props.recipe.slots
    .filter((s) => s.role === 'item_output')
    .sort((a, b) => a.slotIndex - b.slotIndex),
)

const outputCategory = computed(() =>
  props.category
    ? {
        itemInputWidth: 0,
        itemInputHeight: 0,
        itemOutputWidth: props.category.itemOutputWidth,
        itemOutputHeight: props.category.itemOutputHeight,
        shapeless: props.category.shapeless,
      }
    : null,
)

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
  <RecipePanelShell
    :recipe="recipe"
    :category="category"
    :declared-cols="2"
    :declared-rows="3"
  >
    <section class="side inputs">
      <div class="side-header">
        <div class="side-label">输入</div>
        <span v-if="itemInputs.length" class="meta-chip">
          <span class="dot item" />物品 {{ itemInputs.length }}
        </span>
      </div>
      <div class="group">
        <SlotGrid
          class="slot-grid-inscriber"
          :slots="itemInputs"
          :declared-w="2"
          :declared-h="3"
          :pick-hint="pickHint"
          @pick="onPick"
          @lookup="onLookup"
        >
          <template #cell="{ slotData, index, cellSize, declared }">
            <div
              v-if="index === 1"
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
              v-else-if="index === 5"
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
            <div v-else-if="index === 2" class="inscriber-empty" aria-hidden="true" />
            <SlotCell
              v-else
              :slot="slotData"
              :size="cellSize"
              :placeholder="declared && !slotData"
              :pick-hint="pickHint"
              @pick="onPick"
              @lookup="onLookup"
            />
          </template>
        </SlotGrid>
      </div>
    </section>

    <div v-if="itemOutputs.length" class="divider">
      <span class="divider-line" />
      <span class="divider-label">产出</span>
      <span class="divider-line" />
    </div>

    <SidesLayout
      v-if="itemOutputs.length"
      :slots="itemOutputs"
      :category="outputCategory"
      :pick-hint="pickHint"
      @pick="onPick"
      @lookup="onLookup"
    />
  </RecipePanelShell>
</template>

<style scoped>
.side {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
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
  background: var(--el-color-info);
}
.group {
  display: flex;
  flex-direction: column;
  gap: 4px;
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
