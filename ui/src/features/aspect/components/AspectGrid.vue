<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import AspectCard from './AspectCard.vue'
import type { AspectListEntry } from '../types'

const props = defineProps<{
  items: AspectListEntry[]
}>()

const { t } = useI18n()
const primalItems = computed(() => props.items.filter((item) => item.primal))
const compoundItems = computed(() => props.items.filter((item) => !item.primal))
</script>

<template>
  <div class="aspect-grid-sections">
    <section
      v-if="primalItems.length"
      class="aspect-grid-section"
    >
      <header class="aspect-grid-heading">
        <h2>{{ t('aspect.primalSection') }}</h2>
        <span>{{ t('catalog.resultCount', { count: primalItems.length }) }}</span>
      </header>
      <div class="aspect-grid">
        <AspectCard
          v-for="entry in primalItems"
          :key="entry.id"
          :entry="entry"
        />
      </div>
    </section>

    <section
      v-if="compoundItems.length"
      class="aspect-grid-section"
    >
      <header class="aspect-grid-heading">
        <h2>{{ t('aspect.compoundSection') }}</h2>
        <span>{{ t('catalog.resultCount', { count: compoundItems.length }) }}</span>
      </header>
      <div class="aspect-grid">
        <AspectCard
          v-for="entry in compoundItems"
          :key="entry.id"
          :entry="entry"
        />
      </div>
    </section>
  </div>
</template>
