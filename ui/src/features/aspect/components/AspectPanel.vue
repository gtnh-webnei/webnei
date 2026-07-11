<script setup lang="ts">
import McPanel from '@shared/ui/McPanel.vue'

defineProps<{
  title?: string
}>()
</script>

<template>
  <McPanel class="aspect-panel">
    <header
      v-if="title"
      class="aspect-panel-heading"
    >
      <span
        class="aspect-panel-rule"
        aria-hidden="true"
      />
      <h2>{{ title }}</h2>
      <span
        class="aspect-panel-rule"
        aria-hidden="true"
      />
    </header>
    <slot />
  </McPanel>
</template>
