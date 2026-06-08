<script setup lang="ts">
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  title: string;
  text: string;
  pre?: boolean;
  code?: boolean;
}>();
</script>

<template>
  <DetailSectionCard :title="title">
    <pre
      v-if="pre"
      class="detail-pre-text"
    >{{ text }}</pre>
    <code
      v-else-if="code"
      class="detail-code-text"
    >{{ text }}</code>
    <slot v-else>
      {{ text }}
    </slot>
  </DetailSectionCard>
</template>

<style scoped>
.detail-pre-text {
  background: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 12px;
  font-size: 12px;
  max-height: 360px;
  overflow: auto;
  margin: 0;
}
.detail-code-text {
  display: block;
  font-family: 'WebNEI GTNH Glyphs', Consolas, 'Courier New', monospace;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.45;
  color: var(--el-color-primary);
}
</style>
