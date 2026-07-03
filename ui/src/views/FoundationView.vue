<template>
  <section class="foundation-page">
    <el-card
      class="foundation-card"
      shadow="never"
    >
      <p class="eyebrow">
        WebNEI Rewrite
      </p>
      <h1>基础框架已就绪</h1>
      <p class="lead">
        当前前端只保留应用壳、路由、主题和 API client。业务模块会在确认 exporter 数据契约后逐个接入。
      </p>
      <div
        class="steps"
        aria-label="后续模块接入流程"
      >
        <div class="step">
          <span class="step-index">1</span>
          <span>确认 exporter 表和真实数据</span>
        </div>
        <div class="step">
          <span class="step-index">2</span>
          <span>设计后端 read model / view / 索引</span>
        </div>
        <div class="step">
          <span class="step-index">3</span>
          <span>同步 API DTO 与前端类型</span>
        </div>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.foundation-page {
  display: grid;
  min-height: calc(100vh - 104px);
  place-items: center;
}

.foundation-card {
  width: min(720px, 100%);
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--el-color-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: clamp(28px, 5vw, 44px);
  line-height: 1.1;
}

.lead {
  margin: 16px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 16px;
  line-height: 1.7;
}

.steps {
  display: grid;
  gap: 10px;
  margin-top: 28px;
}

.step {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-fill-color-blank);
}

.step-index {
  display: inline-grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-size: 13px;
  font-weight: 700;
}
</style>
