<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AspectChip from './AspectChip.vue'
import type { AspectBrief } from '../types'

defineProps<{
  items: AspectBrief[]
}>()

const { t } = useI18n()
</script>

<template>
  <div
    v-if="items.length"
    class="aspect-used-list"
  >
    <AspectChip
      v-for="entry in items"
      :key="entry.id"
      :entry="entry"
      :to="{ name: 'aspect-detail', params: { aspectId: entry.id } }"
    />
  </div>
  <p
    v-else
    class="aspect-section-empty"
  >
    {{ t('aspect.usedByEmpty') }}
  </p>
</template>
