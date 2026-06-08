<script setup lang="ts">
defineProps<{
  loading: boolean;
  error: string | null;
  hasContent: boolean;
  notFoundText: string;
  skeletonRows?: number;
  contentClass?: string;
}>();
</script>

<template>
  <div class="entity-detail">
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
    />
    <el-skeleton
      v-if="loading"
      :rows="skeletonRows ?? 6"
      animated
    />

    <div
      v-else-if="hasContent"
      class="entity-detail-content"
      :class="contentClass"
    >
      <slot />
    </div>

    <el-empty
      v-else-if="!loading"
      :description="notFoundText"
    />
  </div>
</template>

<style scoped>
.entity-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.entity-detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
