<script setup lang="ts">
// NEI 卡片表面：raised 用于主列表；inset 用于面板内嵌内容，避免与 McPanel 双重凸起。
withDefaults(
  defineProps<{
    tag?: string
    clickable?: boolean
    tone?: 'raised' | 'inset'
  }>(),
  {
    tag: 'article',
    clickable: true,
    tone: 'raised',
  },
)
</script>

<template>
  <component
    :is="tag"
    class="mc-card"
    :class="[`is-${tone}`, { 'is-clickable': clickable }]"
  >
    <slot />
  </component>
</template>

<style scoped lang="scss">
@use '@/styles/mixins' as nei;

.mc-card {
  border-width: 2px;
  border-radius: var(--mc-panel-radius);
  color: var(--mc-panel-text);
}

.mc-card.is-raised {
  @include nei.bevel(var(--mc-panel-hi), var(--mc-panel-low));

  background: var(--mc-panel);
}

.mc-card.is-inset {
  @include nei.bevel(var(--mc-panel-hi), var(--mc-panel-low), sunken);

  background: color-mix(in srgb, var(--mc-slot) 42%, var(--mc-panel));
}

.mc-card.is-clickable {
  cursor: pointer;
  transition:
    background-color 100ms ease,
    filter 100ms ease;
}

.mc-card.is-raised.is-clickable:hover {
  background: var(--mc-card-hover);
  filter: brightness(1.02);
}

.mc-card.is-inset.is-clickable:hover {
  background: color-mix(in srgb, var(--mc-panel-hi) 28%, var(--mc-panel));
  filter: brightness(1.01);
}
</style>
