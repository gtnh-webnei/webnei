<script setup lang="ts">
// NEI 按钮。两种边框风格：
//   bevel   —— 立体边（面板内按钮，如帮助钮）
//   outline —— 单色描边（页头按钮，如菜单钮/数据集触发器）
// active 态统一为 emerald。
withDefaults(
  defineProps<{
    variant?: 'bevel' | 'outline'
    active?: boolean
    type?: 'button' | 'submit'
  }>(),
  {
    variant: 'bevel',
    active: false,
    type: 'button',
  },
)
</script>

<template>
  <button
    :type="type"
    class="mc-button"
    :class="[`is-${variant}`, { 'is-active': active }]"
  >
    <slot />
  </button>
</template>

<style scoped lang="scss">
@use '@/styles/mixins' as nei;

.mc-button {
  display: inline-grid;
  place-items: center;
  border-width: 2px;
  border-style: solid;
  border-radius: var(--mc-panel-radius);
  background: var(--mc-panel);
  color: var(--mc-panel-text);
  font: inherit;
  font-weight: 800;
  cursor: pointer;
}

.mc-button.is-bevel {
  @include nei.bevel(var(--mc-panel-hi), var(--mc-panel-low));
}

.mc-button.is-outline {
  border-color: var(--mc-outline);
}

.mc-button:hover,
.mc-button:focus-visible {
  background: var(--mc-surface-raised);
  color: var(--mc-ink);
  outline: none;
}

.mc-button.is-active {
  @include nei.bevel(var(--mc-emerald-hi), var(--mc-emerald-low));

  background: var(--mc-emerald);
  color: var(--mc-emerald-text);
}
</style>
