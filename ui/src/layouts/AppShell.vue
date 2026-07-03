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
        <span
          class="brand-mark"
          aria-hidden="true"
        />
        <span class="brand-title">WebNEI</span>
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
  grid-template-columns: max-content minmax(0, 1fr) max-content;
  align-items: center;
  gap: 16px;
  height: 64px;
  padding: 0 18px;
  border-bottom: 3px solid #111318;
  background:
    linear-gradient(180deg, rgb(255 255 255 / 0.08), transparent 42%),
    var(--mc-deepslate);
  box-shadow: 0 3px 0 rgb(255 255 255 / 0.08) inset;
}

.brand-link {
  display: grid;
  grid-template-columns: 36px max-content;
  align-items: center;
  gap: 10px;
  color: var(--mc-text);
  text-decoration: none;
  white-space: nowrap;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border: 2px solid;
  border-color: #75d88f #1c5f34 #133f24 #a9efb8;
  background:
    linear-gradient(135deg, transparent 0 28%, rgb(255 255 255 / 0.18) 29% 42%, transparent 43%),
    linear-gradient(180deg, #44b86a 0 50%, #2f8e4d 50% 100%);
  box-shadow:
    0 0 0 2px #111318,
    0 4px 0 rgb(0 0 0 / 0.45);
}

.brand-title {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.06em;
  line-height: 1;
  text-shadow: 2px 2px 0 #000;
}

.header-spacer {
  min-width: 0;
}

.app-main {
  min-height: calc(100vh - 64px);
  padding: clamp(16px, 3vw, 36px);
  background: transparent;
}

@media (max-width: 640px) {
  .app-header {
    height: auto;
    min-height: 64px;
    padding: 12px;
  }
}
</style>
