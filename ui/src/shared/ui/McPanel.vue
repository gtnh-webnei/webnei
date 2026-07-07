<script setup lang="ts">
// NEI 灰面板：凸起立体边 + 像素阴影。业务面板一律用它，不再手写 .mc-panel。
</script>

<template>
  <div class="mc-panel">
    <slot />
  </div>
</template>

<style scoped lang="scss">
@use '@/styles/mixins' as nei;

.mc-panel {
  @include nei.bevel(var(--nei-panel-border-top), var(--nei-panel-border-right));
  @include nei.pixel-shadow;

  border-width: 2px;
  border-radius: var(--mc-panel-radius);
  background: var(--nei-panel-bg);
  color: var(--mc-panel-text);
}
</style>
