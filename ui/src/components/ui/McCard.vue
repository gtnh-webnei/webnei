<script setup lang="ts">
// NEI 可点卡片：凸起立体边 + 面板底 + hover 高亮。列表条目一类的可点方块用它，
// 内部布局由使用方通过默认插槽决定。
withDefaults(
  defineProps<{
    tag?: string
    clickable?: boolean
  }>(),
  {
    tag: 'article',
    clickable: true,
  },
)
</script>

<template>
  <component
    :is="tag"
    class="mc-card"
    :class="{ 'is-clickable': clickable }"
  >
    <slot />
  </component>
</template>

<style scoped lang="scss">
@use '@/styles/mixins' as nei;

.mc-card {
  @include nei.bevel(var(--mc-panel-hi), var(--mc-panel-low));

  border-width: 2px;
  background: var(--mc-panel);
  color: var(--mc-panel-text);
}

.mc-card.is-clickable {
  cursor: pointer;
  transition:
    background-color 100ms ease,
    filter 100ms ease;
}

.mc-card.is-clickable:hover {
  background: var(--mc-card-hover);
  filter: brightness(1.02);
}
</style>
