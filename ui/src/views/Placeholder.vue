<script setup lang="ts">
defineProps<{
  title: string
  hint?: string
}>()
</script>

<template>
  <el-empty :description="hint ?? '即将上线'">
    <template #image>
      <div class="placeholder-icon">🛠️</div>
    </template>
    <h2>{{ title }}</h2>
  </el-empty>
</template>

<style scoped>
.placeholder-icon {
  font-size: 48px;
}
</style>
