<script setup lang="ts">
// NEI 物品槽：凹陷立体边（上左暗、下右亮），边框用 inset 阴影不占内容空间。
// 尺寸由 size 决定，插槽内容填满边框内侧。
withDefaults(
  defineProps<{
    size?: number
  }>(),
  {
    size: 32,
  },
)
</script>

<template>
  <span class="mc-slot">
    <slot />
  </span>
</template>

<style scoped>
.mc-slot {
  display: inline-grid;
  place-items: center;
  width: v-bind('size + "px"');
  height: v-bind('size + "px"');
  padding: 2px;
  /* 凹陷槽边框：上左暗、下右亮、右上与左下交角为底色。
     图层从上到下：两个交角块 → 四条边 → 底色 */
  background-color: var(--mc-slot);
  background-repeat: no-repeat;
  background-image:
    linear-gradient(var(--mc-slot), var(--mc-slot)),
    linear-gradient(var(--mc-slot), var(--mc-slot)),
    linear-gradient(var(--mc-slot-low), var(--mc-slot-low)),
    linear-gradient(var(--mc-slot-low), var(--mc-slot-low)),
    linear-gradient(var(--mc-slot-hi), var(--mc-slot-hi)),
    linear-gradient(var(--mc-slot-hi), var(--mc-slot-hi));
  background-position:
    top right,
    bottom left,
    top left,
    top left,
    bottom right,
    top right;
  background-size:
    2px 2px,
    2px 2px,
    100% 2px,
    2px 100%,
    100% 2px,
    2px 100%;
  color: #ffffff;
  image-rendering: pixelated;
}
</style>
