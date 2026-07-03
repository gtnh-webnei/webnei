<template>
  <section class="not-found-page">
    <el-empty description="页面不存在">
      <router-link to="/">
        <el-button type="primary">
          返回首页
        </el-button>
      </router-link>
    </el-empty>
  </section>
</template>

<style scoped>
.not-found-page {
  display: grid;
  min-height: calc(100vh - 104px);
  place-items: center;
}
</style>
