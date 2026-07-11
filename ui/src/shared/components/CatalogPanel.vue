<script setup lang="ts">
// 全站列表/详情共用的 McPanel 页面壳：header / body / footer 三区。
import McPanel from '@shared/ui/McPanel.vue'

withDefaults(
  defineProps<{
    loading?: boolean
  }>(),
  {
    loading: false,
  },
)
</script>

<template>
  <section class="catalog-page">
    <McPanel class="catalog-panel">
      <header
        v-if="$slots.header"
        class="catalog-panel-header"
      >
        <slot name="header" />
      </header>

      <div
        class="catalog-panel-body mc-scroll-area"
        :class="{ 'is-loading': loading }"
      >
        <slot />
      </div>

      <footer
        v-if="$slots.footer"
        class="catalog-panel-footer"
      >
        <slot name="footer" />
      </footer>
    </McPanel>
  </section>
</template>

<style scoped>
.catalog-page {
  height: calc(100dvh - var(--app-header-height) - var(--app-page-padding) - var(--app-page-padding));
  min-height: 0;
}

.catalog-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
  min-width: 0;
  min-height: 0;
  padding: 16px;
  overflow: hidden;
}

.catalog-panel-header,
.catalog-panel-footer {
  flex: 0 0 auto;
  min-width: 0;
}

.catalog-panel-body {
  flex: 1 1 auto;
  min-width: 0;
  min-height: 0;
  overflow: auto;
  padding-right: 6px;
}

.catalog-panel-body.is-loading {
  opacity: 0.7;
}

@media (max-width: 720px) {
  .catalog-panel {
    padding: 12px;
  }

  .catalog-panel-body {
    padding-right: 0;
  }
}
</style>
