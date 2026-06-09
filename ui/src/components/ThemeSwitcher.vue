<script setup lang="ts">
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useI18n } from 'vue-i18n';
import { Sunny, Moon } from '@element-plus/icons-vue';
import { useThemeStore } from '@/stores/theme';

const { t } = useI18n();
const store = useThemeStore();
const { effective } = storeToRefs(store);

const isDark = computed(() => effective.value === 'dark');
const title = computed(() => (isDark.value ? t('theme.light') : t('theme.dark')));

function setDark(value: boolean | string | number) {
  store.setMode(value ? 'dark' : 'light');
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
  display: inline-flex;
  align-items: center;
  width: 56px;
  height: 32px;
  --el-switch-on-color: #2e333b;
  --el-switch-off-color: #e9ecef;
  --el-switch-border-color: var(--el-border-color);
}
.theme-switch :deep(.el-switch__action) {
  color: #3c3c43;
}
</style>
