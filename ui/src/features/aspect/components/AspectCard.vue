<script setup lang="ts">
import { computed, type CSSProperties } from 'vue'
import { useI18n } from 'vue-i18n'
import McCard from '@shared/ui/McCard.vue'
import AspectSigil from './AspectSigil.vue'
import type { AspectListEntry } from '../types'

const props = defineProps<{
  entry: AspectListEntry
}>()

const { t } = useI18n()
const cardStyle = computed<CSSProperties>(() => ({
  '--aspect-color': `#${(props.entry.color & 0xffffff).toString(16).padStart(6, '0')}`,
}))
</script>

<template>
  <McCard
    tag="router-link"
    class="aspect-card"
    :style="cardStyle"
    :to="{ name: 'aspect-detail', params: { aspectId: entry.id } }"
  >
    <AspectSigil
      :icon="entry.icon"
      :name="entry.displayName"
      :color="entry.color"
      :size="46"
    />
    <span class="aspect-card-copy">
      <span class="aspect-card-title-row">
        <strong>{{ entry.displayName }}</strong>
      </span>
      <span class="aspect-card-description">{{ entry.description }}</span>
      <span class="aspect-card-footer">
        <span class="aspect-card-meta">
          {{ t('aspect.itemCount', { count: entry.associatedItemCount }) }}
        </span>
        <span
          v-if="!entry.primal && entry.components.length === 2"
          class="aspect-card-components"
          role="img"
          :aria-label="t('aspect.componentFormula', {
            left: entry.components[0].displayName,
            right: entry.components[1].displayName,
          })"
        >
          <AspectSigil
            :icon="entry.components[0].icon"
            :name="entry.components[0].displayName"
            :color="entry.components[0].color"
            :size="14"
            aria-hidden="true"
          />
          <span aria-hidden="true">+</span>
          <AspectSigil
            :icon="entry.components[1].icon"
            :name="entry.components[1].displayName"
            :color="entry.components[1].color"
            :size="14"
            aria-hidden="true"
          />
        </span>
      </span>
    </span>
  </McCard>
</template>
