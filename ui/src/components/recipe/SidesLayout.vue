<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { RecipeSlot } from '@/api/recipes.types';
import SlotCell from '../SlotCell.vue';
import SlotGrid from './SlotGrid.vue';

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    slots: RecipeSlot[];
    category?: {
      categoryId: string;
      itemInputWidth: number;
      itemInputHeight: number;
      itemOutputWidth: number;
      itemOutputHeight: number;
      fluidInputWidth: number;
      fluidInputHeight: number;
      fluidOutputWidth: number;
      fluidOutputHeight: number;
      shapeless: boolean;
    } | null;
    pickHint?: string;
    showProbabilityBadge?: boolean;
  }>(),
  { category: null, pickHint: undefined, showProbabilityBadge: true },
);

const emit = defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void;
  (
    e: 'lookup',
    kind: 'recipe' | 'usage',
    payload: { itemVariantId: string | null; fluidVariantId: string | null },
  ): void;
}>();

function filterRole(role: string): RecipeSlot[] {
  return [...props.slots].filter((s) => s.role === role).sort((a, b) => a.slotIndex - b.slotIndex);
}

const itemInputs = computed(() => filterRole('item_input'));
const itemOutputs = computed(() => filterRole('item_output'));
const fluidInputs = computed(() => filterRole('fluid_input'));
const fluidOutputs = computed(() => filterRole('fluid_output'));

interface SlotGroup {
  key: string;
  order: number;
  slots: RecipeSlot[];
}

function slotGroups(slots: RecipeSlot[]): SlotGroup[] {
  if (!slots.some((s) => !!s.slotGroupKey)) {
    return [{ key: '', order: 0, slots }];
  }
  const map = new Map<string, SlotGroup>();
  for (const slot of slots) {
    const key = slot.slotGroupKey || '';
    const group = map.get(key) ?? {
      key,
      order: slot.slotGroupOrder ?? 0,
      slots: [],
    };
    group.order = slot.slotGroupOrder ?? group.order;
    group.slots.push(slot);
    map.set(key, group);
  }
  return [...map.values()].sort((a, b) => a.order - b.order || a.key.localeCompare(b.key));
}

const itemInputGroups = computed(() => slotGroups(itemInputs.value));
const itemOutputGroups = computed(() => slotGroups(itemOutputs.value));
const fluidInputGroups = computed(() => slotGroups(fluidInputs.value));
const fluidOutputGroups = computed(() => slotGroups(fluidOutputs.value));

// Special items: filter by placement field
const specialInputs = computed(() =>
  props.slots
    .filter((s) => s.role === 'special_item' && s.placement === 'special_input')
    .sort((a, b) => a.slotIndex - b.slotIndex),
);
const specialOutputs = computed(() =>
  props.slots
    .filter((s) => s.role === 'special_item' && s.placement === 'special_output')
    .sort((a, b) => a.slotIndex - b.slotIndex),
);

const hasInputs = computed(() => itemInputs.value.length + fluidInputs.value.length > 0);
const hasOutputs = computed(() => itemOutputs.value.length + fluidOutputs.value.length > 0);

const shapeless = computed(() => !!props.category?.shapeless);

const inputDims = computed<{ w: number | null; h: number | null }>(() => ({
  w: props.category?.itemInputWidth ?? null,
  h: props.category?.itemInputHeight ?? null,
}));

const fluidInputDims = computed<{ w: number | null; h: number | null }>(() => ({
  w: props.category?.fluidInputWidth ?? null,
  h: props.category?.fluidInputHeight ?? null,
}));
const fluidOutputDims = computed<{ w: number | null; h: number | null }>(() => ({
  w: props.category?.fluidOutputWidth ?? null,
  h: props.category?.fluidOutputHeight ?? null,
}));

function onPick(payload: { itemVariantId: string | null; fluidVariantId: string | null }) {
  emit('pick', payload);
}
function onLookup(
  kind: 'recipe' | 'usage',
  payload: { itemVariantId: string | null; fluidVariantId: string | null },
) {
  emit('lookup', kind, payload);
}
</script>

<template>
  <section v-if="specialInputs.length" class="side special-inputs">
    <div class="side-header">
      <div class="side-label">
        {{ t('recipe.specialInput') }}
      </div>
      <div class="side-meta">
        <span class="meta-chip special">
          <span class="dot special" />{{ specialInputs.length }}
        </span>
      </div>
    </div>
    <div class="group">
      <SlotGrid
        :slots="specialInputs"
        :declared-w="null"
        :declared-h="null"
        :shapeless="false"
        :pick-hint="pickHint"
        :show-probability-badge="showProbabilityBadge"
        @pick="onPick"
        @lookup="onLookup"
      />
    </div>
  </section>

  <section v-if="hasInputs" class="side inputs">
    <div class="side-header">
      <div class="side-label">
        {{ t('recipe.input') }}
      </div>
      <div class="side-meta">
        <span v-if="itemInputs.length" class="meta-chip">
          <span class="dot item" />{{ t('recipe.itemCountLabel', { count: itemInputs.length }) }}
        </span>
        <span v-if="fluidInputs.length" class="meta-chip">
          <span class="dot fluid" />{{ t('recipe.fluidCountLabel', { count: fluidInputs.length }) }}
        </span>
      </div>
    </div>
    <div v-if="itemInputs.length" class="group-stack">
      <div v-for="group in itemInputGroups" :key="group.key" class="group">
        <SlotGrid
          :slots="group.slots"
          :declared-w="inputDims.w"
          :declared-h="inputDims.h"
          :shapeless="shapeless"
          :pick-hint="pickHint"
          :show-probability-badge="showProbabilityBadge"
          @pick="onPick"
          @lookup="onLookup"
        />
      </div>
    </div>
    <div v-if="fluidInputs.length" class="group-stack">
      <SlotGrid
        v-for="group in fluidInputGroups"
        :key="group.key"
        :slots="group.slots"
        :declared-w="fluidInputDims.w"
        :declared-h="fluidInputDims.h"
        :shapeless="shapeless"
        :pick-hint="pickHint"
        :show-probability-badge="showProbabilityBadge"
        @pick="onPick"
        @lookup="onLookup"
      >
        <template #cell="{ slotData, cellSize, declared }">
          <SlotCell
            v-bind="{ slot: slotData, size: cellSize }"
            is-fluid
            :placeholder="declared && !slotData"
            :pick-hint="pickHint"
            :show-probability-badge="showProbabilityBadge"
            @pick="onPick"
            @lookup="onLookup"
          />
        </template>
      </SlotGrid>
    </div>
  </section>

  <div v-if="hasInputs && hasOutputs" class="divider">
    <span class="divider-line" />
    <span class="divider-label">{{ t('recipe.outputDivider') }}</span>
    <span class="divider-line" />
  </div>

  <section v-if="hasOutputs" class="side outputs">
    <div class="side-header">
      <div class="side-label">
        {{ t('recipe.output') }}
      </div>
      <div class="side-meta">
        <span v-if="itemOutputs.length" class="meta-chip">
          <span class="dot item" />{{ t('recipe.itemCountLabel', { count: itemOutputs.length }) }}
        </span>
        <span v-if="fluidOutputs.length" class="meta-chip">
          <span class="dot fluid" />{{
            t('recipe.fluidCountLabel', { count: fluidOutputs.length })
          }}
        </span>
      </div>
    </div>
    <div v-if="itemOutputs.length" class="group-stack">
      <div v-for="group in itemOutputGroups" :key="group.key" class="group">
        <SlotGrid
          :slots="group.slots"
          :declared-w="category?.itemOutputWidth"
          :declared-h="category?.itemOutputHeight"
          :shapeless="shapeless"
          :pick-hint="pickHint"
          :show-probability-badge="showProbabilityBadge"
          @pick="onPick"
          @lookup="onLookup"
        />
      </div>
    </div>
    <div v-if="fluidOutputs.length" class="group-stack">
      <SlotGrid
        v-for="group in fluidOutputGroups"
        :key="group.key"
        :slots="group.slots"
        :declared-w="fluidOutputDims.w"
        :declared-h="fluidOutputDims.h"
        :shapeless="shapeless"
        :pick-hint="pickHint"
        :show-probability-badge="showProbabilityBadge"
        @pick="onPick"
        @lookup="onLookup"
      >
        <template #cell="{ slotData, cellSize, declared }">
          <SlotCell
            v-bind="{ slot: slotData, size: cellSize }"
            is-fluid
            :placeholder="declared && !slotData"
            :pick-hint="pickHint"
            :show-probability-badge="showProbabilityBadge"
            @pick="onPick"
            @lookup="onLookup"
          />
        </template>
      </SlotGrid>
    </div>
  </section>

  <section v-if="specialOutputs.length" class="side special-outputs">
    <div class="side-header">
      <div class="side-label">
        {{ t('recipe.specialOutput') }}
      </div>
      <div class="side-meta">
        <span class="meta-chip special">
          <span class="dot special" />{{ specialOutputs.length }}
        </span>
      </div>
    </div>
    <div class="group">
      <SlotGrid
        :slots="specialOutputs"
        :declared-w="null"
        :declared-h="null"
        :shapeless="false"
        :pick-hint="pickHint"
        :show-probability-badge="showProbabilityBadge"
        @pick="onPick"
        @lookup="onLookup"
      />
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
.meta-chip.special {
  background: rgba(230, 162, 60, 0.1);
  color: rgba(230, 162, 60, 1);
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
.meta-chip .dot.special {
  background: rgba(230, 162, 60, 0.85);
}
.group-stack,
.group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.group-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
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
