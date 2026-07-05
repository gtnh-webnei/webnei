<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import McPanel from '@/components/ui/McPanel.vue'

const { t } = useI18n()
</script>

<template>
  <section class="not-found-page">
    <McPanel class="not-found-card">
      <p>{{ t('common.notFound') }}</p>
      <router-link to="/">
        <el-button type="primary">
          {{ t('common.back') }}
        </el-button>
      </router-link>
    </McPanel>
  </section>
</template>

<style scoped>
.not-found-page {
  display: grid;
  min-height: calc(100dvh - var(--app-header-height) - var(--app-page-padding) - var(--app-page-padding));
  place-items: center;
}

.not-found-card {
  display: grid;
  gap: 12px;
  width: min(320px, 100%);
  padding: 20px;
}

p {
  margin: 0;
  color: var(--mc-panel-text);
  font-size: 24px;
  font-weight: 800;
}
</style>
