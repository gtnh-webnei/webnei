<script setup lang="ts">
import { computed } from 'vue'
import type { Recipe, RecipeCategory } from '@/api/recipes.types'
import { useDatasetStore } from '@/stores/dataset'
import RecipePanelShell from '../RecipePanelShell.vue'
import SidesLayout from '../SidesLayout.vue'
import GregTechTierBadge from '../gregtech/GregTechTierBadge.vue'
import GregTechSpecialItems from '../gregtech/GregTechSpecialItems.vue'
import GregTechMetadataStrip from '../gregtech/GregTechMetadataStrip.vue'

const props = defineProps<{
  recipe: Recipe
  category?: RecipeCategory | null
  pickHint?: string
}>()

const emit = defineEmits<{
  (e: 'pick', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
  (e: 'lookup', kind: 'recipe' | 'usage', payload: { itemVariantId: string | null; fluidVariantId: string | null }): void
}>()

const declaredCols = computed(() => props.category?.itemInputWidth ?? null)
const declaredRows = computed(() => props.category?.itemInputHeight ?? null)
const gt = computed(() => props.recipe.gregtech)

const datasetStore = useDatasetStore()
const displaySpecUrl = computed(() => datasetStore.active?.displaySpecUrl ?? null)

// EOH 在游戏内 NEI 重写了 drawNEIOverlays，槽位上不画 chance —
// 概率只在 hover tooltip 显示（item 的"占比"语义）。
const showProbabilityBadge = computed(
  () => props.category?.categoryId !== 'gregtech:tt_eyeofharmony',
)
</script>

<template>
  <RecipePanelShell
    :recipe="recipe"
    :category="category"
    :declared-cols="declaredCols"
    :declared-rows="declaredRows"
  >
    <template v-if="gt?.voltageTier" #header-tag>
      <GregTechTierBadge :tier="gt.voltageTier" />
    </template>

    <SidesLayout
      :slots="recipe.slots"
      :category="category"
      :pick-hint="pickHint"
      :show-probability-badge="showProbabilityBadge"
      @pick="(p) => emit('pick', p)"
      @lookup="(k, p) => emit('lookup', k, p)"
    />

    <template v-if="gt" #footer>
      <section class="gt-info-panel">
        <div class="gt-stack">
          <GregTechMetadataStrip
            :gt="gt"
            :slots="recipe.slots"
            :spec-url="displaySpecUrl"
            :handler-id="category?.handlerId"
          />
          <GregTechSpecialItems v-if="gt.specialItems?.length" :items="gt.specialItems" />
        </div>
      </section>
    </template>
  </RecipePanelShell>
</template>

<style scoped>
.gt-info-panel {
  display: flex;
  flex-direction: column;
  margin-top: 4px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-bg-color);
  overflow: hidden;
}
.gt-stack {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 12px;
  background: var(--el-bg-color);
}
</style>
