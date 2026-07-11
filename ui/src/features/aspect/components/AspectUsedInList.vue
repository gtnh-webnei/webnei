<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AspectSigil from './AspectSigil.vue'
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
    <router-link
      v-for="entry in items"
      :key="entry.id"
      class="aspect-used-link"
      :to="{ name: 'aspect-detail', params: { aspectId: entry.id } }"
    >
      <AspectSigil
        :icon="entry.icon"
        :name="entry.displayName"
        :color="entry.color"
        :size="36"
      />
      <strong>{{ entry.displayName }}</strong>
    </router-link>
  </div>
  <p
    v-else
    class="aspect-section-empty"
  >
    {{ t('aspect.usedByEmpty') }}
  </p>
</template>
