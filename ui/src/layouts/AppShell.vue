<script setup lang="ts">
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <router-link
        to="/"
        class="brand-link"
      >
        WebNEI
      </router-link>
      <div class="header-spacer" />
      <ThemeSwitcher />
    </el-header>

    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-header {
  display: grid;
  grid-template-columns: max-content 1fr max-content;
  align-items: center;
  gap: 16px;
  height: 56px;
  padding: 0 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}

.brand-link {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.02em;
  text-decoration: none;
  white-space: nowrap;
}

.header-spacer {
  min-width: 0;
}

.app-main {
  min-height: calc(100vh - 56px);
  padding: 24px;
  background: var(--el-bg-color-page);
}
</style>
