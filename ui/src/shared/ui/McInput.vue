<script setup lang="ts">
// NEI 输入框：包 el-input，套 NEI 边框 + 灰底，focus 时边框加深、底色提亮。搜索/表单输入一律用它。
const model = defineModel<string>()

withDefaults(
  defineProps<{
    placeholder?: string
    clearable?: boolean
  }>(),
  {
    placeholder: '',
    clearable: false,
  },
)
</script>

<template>
  <el-input
    v-model="model"
    class="mc-input"
    :placeholder="placeholder"
    :clearable="clearable"
  />
</template>

<style scoped>
.mc-input :deep(.el-input__wrapper) {
  border: 2px solid var(--mc-panel-low);
  border-radius: var(--mc-panel-radius);
  background: var(--mc-input-bg);
  box-shadow: none;
}

.mc-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--mc-bedrock);
  background: var(--mc-input-bg-focus);
  box-shadow: none;
}

.mc-input :deep(.el-input__inner) {
  color: var(--mc-panel-text);
  font-weight: 700;
}

.mc-input :deep(.el-input__inner::placeholder) {
  color: var(--mc-placeholder);
  font-weight: 700;
}
</style>
