<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Recipe, RecipeCategory } from '@/api/recipes.types'
import { getQuestLineForQuest } from '@/api/quests'
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

const router = useRouter()
const datasetStore = useDatasetStore()
const displaySpecUrl = computed(() => datasetStore.active?.displaySpecUrl ?? null)
const displaySpecMessagesUrl = computed(() => datasetStore.active?.displaySpecMessagesUrl ?? null)

// Voltage tier badge, driven generically by metadata (GregTech recipes carry voltage_tier).
const voltageTier = computed(() => {
  const v = props.recipe.metadata?.voltage_tier
  return v?.valueText ?? null
})

// EOH 在游戏内 NEI 重写了 drawNEIOverlays，槽位上不画 chance —
// 概率只在 hover tooltip 显示（item 的"占比"语义）。
const showProbabilityBadge = computed(
  () => props.category?.categoryId !== 'gregtech:tt_eyeofharmony',
)

// Plain special items (special_item slots without placement) render in a footer grid.
const unhandledSpecialItems = computed(() =>
  props.recipe.slots
    .filter(s => s.role === 'special_item' && !s.placement && s.itemVariantId)
    .map(s => ({
      itemVariantId: s.itemVariantId as string,
      displayName: s.displayName,
      assetUrl: s.assetUrl,
    })),
)

const hasMetadata = computed(() => Object.keys(props.recipe.metadata ?? {}).length > 0)

async function openQuest(questId: string) {
  const datasetId = datasetStore.activeDatasetId
  if (!datasetId) return
  const line = await getQuestLineForQuest(datasetId, questId)
  await router.push({
    name: 'quest-line',
    params: { datasetId },
    query: { id: line.questLineId, questId },
  })
}
</script>

<template>
  <RecipePanelShell
    :recipe="recipe"
    :category="category"
    :declared-cols="declaredCols"
    :declared-rows="declaredRows"
  >
    <template
      v-if="voltageTier"
      #header-tag
    >
      <GregTechTierBadge :tier="voltageTier" />
    </template>

    <SidesLayout
      :slots="recipe.slots"
      :category="category"
      :pick-hint="pickHint"
      :show-probability-badge="showProbabilityBadge"
      @pick="(p) => emit('pick', p)"
      @lookup="(k, p) => emit('lookup', k, p)"
    />

    <template
      v-if="hasMetadata || unhandledSpecialItems.length"
      #footer
    >
      <section class="info-panel">
        <div class="info-stack">
          <GregTechMetadataStrip
            :metadata="recipe.metadata"
            :slots="recipe.slots"
            :spec-url="displaySpecUrl"
            :spec-messages-url="displaySpecMessagesUrl"
            :handler-id="category?.handlerId"
            @open-quest="openQuest"
          />
          <GregTechSpecialItems
            v-if="unhandledSpecialItems.length"
            :items="unhandledSpecialItems"
          />
        </div>
      </section>
    </template>
  </RecipePanelShell>
</template>

<style scoped>
.info-panel {
  display: flex;
  flex-direction: column;
  margin-top: 4px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-bg-color);
  overflow: hidden;
}
.info-stack {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 12px;
  background: var(--el-bg-color);
}
</style>
