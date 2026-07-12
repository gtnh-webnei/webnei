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
  <ul
    v-if="items.length"
    class="aspect-used-list"
  >
    <li
      v-for="entry in items"
      :key="entry.id"
      class="aspect-used-item"
    >
      <AspectChip
        :entry="entry"
        :to="{ name: 'aspect-detail', params: { aspectId: entry.id } }"
      />
    </li>
  </ul>
  <p
    v-else
    class="aspect-section-empty"
  >
    {{ t('aspect.usedByEmpty') }}
  </p>
</template>
