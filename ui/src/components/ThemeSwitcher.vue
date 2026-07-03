<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

const store = useThemeStore()
const { effective } = storeToRefs(store)

const isDark = computed(() => effective.value === 'dark')
const title = '切换主题'

function setDark(value: boolean | string | number) {
  store.setMode(value ? 'dark' : 'light')
}
</script>

<template>
  <el-switch
    class="theme-switch"
    size="large"
    :model-value="isDark"
    :title="title"
    :aria-label="title"
    :active-action-icon="Moon"
    :inactive-action-icon="Sunny"
    @change="setDark"
  />
</template>

<style scoped>
.theme-switch {
  display: inline-grid;
  align-items: center;
  width: 58px;
  height: 32px;
  --el-switch-on-color: #111318;
  --el-switch-off-color: #8d929c;
  --el-switch-border-color: #111318;
}

.theme-switch :deep(.el-switch__core) {
  border: 2px solid #111318;
  border-radius: 2px;
  box-shadow: 0 2px 0 rgb(0 0 0 / 0.4);
}

.theme-switch :deep(.el-switch__action) {
  border-radius: 1px;
  color: #202020;
}

@media (max-width: 640px) {
  .theme-switch {
    width: 52px;
    height: 30px;
  }
}
</style>
