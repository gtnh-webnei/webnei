<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AspectSigil from './AspectSigil.vue'
import type { AspectDetail } from '../types'

defineProps<{
  aspect: AspectDetail
}>()

const { t } = useI18n()
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
    <div class="aspect-equation-term is-current">
      <AspectSigil
        :icon="aspect.icon"
        :name="aspect.displayName"
        :color="aspect.color"
        :size="58"
      />
      <span>{{ aspect.displayName }}</span>
    </div>
    <span
      class="aspect-equation-symbol"
      aria-hidden="true"
    >=</span>
    <router-link
      class="aspect-equation-term"
      :to="{ name: 'aspect-detail', params: { aspectId: aspect.components[0].id } }"
    >
      <AspectSigil
        :icon="aspect.components[0].icon"
        :name="aspect.components[0].displayName"
        :color="aspect.components[0].color"
        :size="46"
      />
      <span>{{ aspect.components[0].displayName }}</span>
    </router-link>
    <span
      class="aspect-equation-symbol"
      aria-hidden="true"
    >+</span>
    <router-link
      class="aspect-equation-term"
      :to="{ name: 'aspect-detail', params: { aspectId: aspect.components[1].id } }"
    >
      <AspectSigil
        :icon="aspect.components[1].icon"
        :name="aspect.components[1].displayName"
        :color="aspect.components[1].color"
        :size="46"
      />
      <span>{{ aspect.components[1].displayName }}</span>
    </router-link>
  </div>
  <p
    v-else
    class="aspect-primal-empty"
  >
    {{ t('aspect.primalCompositionEmpty') }}
  </p>
</template>
