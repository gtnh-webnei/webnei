<script setup lang="ts">
defineProps<{
  title: string;
}>();
</script>

<template>
  <el-card
    shadow="never"
    class="detail-section-card"
  >
    <template #header>
      <span class="detail-section-title">{{ title }}</span>
    </template>
    <slot />
  </el-card>
</template>

<style scoped>
.detail-section-card {
  margin-bottom: 16px;
}
.detail-section-title {
  font-size: 13px;
  font-weight: 600;
}
</style>
