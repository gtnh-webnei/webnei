<script setup lang="ts">
defineProps<{
  title: string;
  subtitle: string;
  assetUrl?: string | null;
  fallbackText?: string;
  fallbackClass?: string;
  iconClass?: string;
  imageAlt?: string;
}>();

const emit = defineEmits<{
  (e: 'imageError'): void;
}>();
</script>

<template>
  <header class="detail-hero-card">
    <div
      class="detail-hero-icon-wrap"
      :class="iconClass"
    >
      <img
        v-if="assetUrl"
        :src="assetUrl"
        :alt="imageAlt ?? title"
        class="detail-hero-icon"
        @error="emit('imageError')"
      >
      <div
        v-else-if="fallbackText"
        class="detail-hero-fallback"
        :class="fallbackClass"
      >
        {{ fallbackText }}
      </div>
    </div>
    <div class="detail-hero-title-block">
      <h1 class="detail-hero-title">
        {{ title }}
      </h1>
      <div class="detail-hero-subtitle">
        <el-tag
          size="default"
          type="info"
          effect="light"
          round
        >
          {{ subtitle }}
        </el-tag>
        <slot name="tags" />
      </div>
    </div>
  </header>
</template>

<style scoped>
.detail-hero-card {
  display: flex;
  gap: 16px;
  align-items: center;
  background: var(--el-bg-color);
  padding: 12px 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}
.detail-hero-icon-wrap {
  width: 64px;
  height: 64px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.detail-hero-icon-wrap.fluid {
  background: rgba(33, 130, 230, 0.12);
  border-color: rgba(33, 130, 230, 0.4);
}
.detail-hero-icon-wrap.gaseous {
  background: rgba(245, 158, 11, 0.12);
  border-color: rgba(245, 158, 11, 0.5);
}
.detail-hero-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.detail-hero-title-block {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.detail-hero-title {
  margin: 0;
  font-size: 20px;
  line-height: 1.3;
  word-break: break-word;
}
.detail-hero-subtitle {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.detail-hero-fallback {
  font-size: 28px;
  font-weight: 700;
  color: rgba(33, 130, 230, 0.9);
}
.detail-hero-fallback.gaseous {
  color: rgba(245, 158, 11, 0.95);
}
</style>
