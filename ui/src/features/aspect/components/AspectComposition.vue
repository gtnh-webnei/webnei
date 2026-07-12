<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AspectSigil from './AspectSigil.vue'
import type { AspectDetail } from '../types'

defineProps<{
  aspect: AspectDetail
}>()

const { t } = useI18n()

const SIGIL_SIZE = 36
</script>

<template>
  <div
    v-if="aspect.components.length === 2"
    class="aspect-equation"
    :aria-label="t('aspect.componentFormula', {
      left: aspect.components[0].displayName,
      right: aspect.components[1].displayName,
    })"
  >
    <div class="aspect-equation-term is-current aspect-equation-current">
      <AspectSigil
        :icon="aspect.icon"
        :name="aspect.displayName"
        :color="aspect.color"
        :size="SIGIL_SIZE"
      />
      <span>{{ aspect.displayName }}</span>
    </div>

    <span
      class="aspect-equation-symbol aspect-equation-equals"
      aria-hidden="true"
    >=</span>

    <router-link
      class="aspect-equation-term aspect-equation-left"
      :to="{ name: 'aspect-detail', params: { aspectId: aspect.components[0].id } }"
    >
      <AspectSigil
        :icon="aspect.components[0].icon"
        :name="aspect.components[0].displayName"
        :color="aspect.components[0].color"
        :size="SIGIL_SIZE"
      />
      <span>{{ aspect.components[0].displayName }}</span>
    </router-link>

    <span
      class="aspect-equation-symbol aspect-equation-plus"
      aria-hidden="true"
    >+</span>

    <router-link
      class="aspect-equation-term aspect-equation-right"
      :to="{ name: 'aspect-detail', params: { aspectId: aspect.components[1].id } }"
    >
      <AspectSigil
        :icon="aspect.components[1].icon"
        :name="aspect.components[1].displayName"
        :color="aspect.components[1].color"
        :size="SIGIL_SIZE"
      />
      <span>{{ aspect.components[1].displayName }}</span>
    </router-link>
  </div>
  <p
    v-else
    class="aspect-section-empty"
  >
    {{ t('aspect.primalCompositionEmpty') }}
  </p>
</template>
