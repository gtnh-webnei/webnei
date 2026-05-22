<script setup lang="ts">
import { computed } from 'vue'
import type { Recipe, RecipeCategory } from '@/api/recipes.types'
import RecipePanelShell from '../RecipePanelShell.vue'
import SidesLayout from '../SidesLayout.vue'
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
// large_boiler 的 additional_info 已经在 FuelStats 里渲染了，避免重复
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
    <template v-if="gt" #badge>
      <div class="gt-stack">
        <GregTechFuelStats v-if="isFuel" :info="gt" />
        <GregTechCoreStats v-else :info="gt" />
        <GregTechSpecialItems v-if="gt.specialItems?.length" :items="gt.specialItems" />
        <GregTechMetadataList :metadata="gt.metadata" />
        <GregTechNotes v-if="showNotes" :text="gt.additionalInfo" />
      </div>
    </template>

    <SidesLayout
      :slots="recipe.slots"
      :category="category"
      :pick-hint="pickHint"
      @pick="(p) => emit('pick', p)"
      @lookup="(k, p) => emit('lookup', k, p)"
    />
  </RecipePanelShell>
</template>

<style scoped>
.gt-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
