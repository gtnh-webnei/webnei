<template>
  <section class="not-found-page">
    <div class="mc-panel not-found-card">
      <p>404</p>
      <router-link to="/">
        <el-button type="primary">
          返回
        </el-button>
      </router-link>
    </div>
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
  color: #202020;
  font-size: 24px;
  font-weight: 800;
}
</style>
