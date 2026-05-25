<script setup lang="ts">
import { computed } from 'vue'
import type { Recipe, RecipeCategory } from '@/api/recipes.types'
import RecipePanelShell from '../RecipePanelShell.vue'
import SidesLayout from '../SidesLayout.vue'
import GregTechTierBadge from '../gregtech/GregTechTierBadge.vue'
import GregTechCoreStats from '../gregtech/GregTechCoreStats.vue'
import GregTechFuelStats from '../gregtech/GregTechFuelStats.vue'
import GregTechSpecialItems from '../gregtech/GregTechSpecialItems.vue'
import GregTechMetadataList from '../gregtech/GregTechMetadataList.vue'
import GregTechNotes from '../gregtech/GregTechNotes.vue'

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

const isFuel = computed(() => gt.value?.recipeKind === 'FUEL')
const isLargeBoilerFuel = computed(
  () => isFuel.value && gt.value?.fuelProfile?.machineKind === 'large_boiler',
)
const showNotes = computed(() => {
  if (!gt.value?.additionalInfo) return false
  return !isLargeBoilerFuel.value
})
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
      @pick="(p) => emit('pick', p)"
      @lookup="(k, p) => emit('lookup', k, p)"
    />

    <template v-if="gt" #footer>
      <section class="gt-info-panel">
        <header class="gt-info-header">
          <span class="dot" />
          <span class="title">{{ isFuel ? '燃料信息' : '配方信息' }}</span>
        </header>
        <div class="gt-stack">
          <GregTechFuelStats v-if="isFuel" :info="gt" />
          <GregTechCoreStats v-else :info="gt" />
          <GregTechSpecialItems v-if="gt.specialItems?.length" :items="gt.specialItems" />
          <GregTechMetadataList :metadata="gt.metadata" />
          <GregTechNotes v-if="showNotes" :text="gt.additionalInfo" />
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
.gt-info-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px 4px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  color: var(--el-text-color-secondary);
  background: var(--el-bg-color);
}
.gt-info-header .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--el-color-primary);
}
.gt-stack {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 12px 10px;
  background: var(--el-bg-color);
}
.gt-stack :deep(.stat-list),
.gt-stack :deep(.meta-list),
.gt-stack :deep(.fuel-stats) {
  border: none;
}
</style>
