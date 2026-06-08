<script setup lang="ts">
defineProps<{
  title: string;
  count: string | number;
}>();
</script>

<template>
  <div class="extras-block">
    <div class="extras-block-title">
      {{ title }}
      <span class="extras-count">{{ count }}</span>
    </div>
    <slot />
  </div>
</template>

<style scoped>
.extras-block {
  margin-top: 14px;
}
.extras-block-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.extras-count {
  font-weight: 400;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-variant-numeric: tabular-nums;
}
</style>
