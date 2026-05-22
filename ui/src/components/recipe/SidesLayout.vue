<script setup lang="ts">
import { computed } from 'vue'
import type { RecipeSlot } from '@/api/recipes.types'
import SlotCell from '../SlotCell.vue'
import SlotGrid from './SlotGrid.vue'

const props = withDefaults(
  defineProps<{
    slots: RecipeSlot[]
    category?: {
      itemInputWidth: number
      itemInputHeight: number
      itemOutputWidth: number
      itemOutputHeight: number
      shapeless: boolean
    } | null
    pickHint?: string
  }>(),
  { category: null, pickHint: undefined },
)

const emit = defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
}>()

function filterRole(role: string): RecipeSlot[] {
  return [...props.slots]
    .filter((s) => s.role === role)
    .sort((a, b) => a.slotIndex - b.slotIndex)
}

const itemInputs = computed(() => filterRole('item_input'))
const itemOutputs = computed(() => filterRole('item_output'))
const fluidInputs = computed(() => filterRole('fluid_input'))
const fluidOutputs = computed(() => filterRole('fluid_output'))

const hasInputs = computed(() => itemInputs.value.length + fluidInputs.value.length > 0)
const hasOutputs = computed(() => itemOutputs.value.length + fluidOutputs.value.length > 0)

const shapeless = computed(() => !!props.category?.shapeless)

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
    <div v-if="itemInputs.length" class="group">
      <SlotGrid
        :slots="itemInputs"
        :declared-w="category?.itemInputWidth"
        :declared-h="category?.itemInputHeight"
        :shapeless="shapeless"
        :pick-hint="pickHint"
        @pick="onPick"
        @lookup="onLookup"
      />
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
    <div v-if="itemOutputs.length" class="group">
      <SlotGrid
        :slots="itemOutputs"
        :declared-w="category?.itemOutputWidth"
        :declared-h="category?.itemOutputHeight"
        :shapeless="shapeless"
        :pick-hint="pickHint"
        @pick="onPick"
        @lookup="onLookup"
      />
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
</style>
