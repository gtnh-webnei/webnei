<script setup lang="ts">
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useI18n } from 'vue-i18n';
import { Sunny, Moon, MagicStick } from '@element-plus/icons-vue';
import { useThemeStore, type ThemeMode } from '@/stores/theme';

const { t } = useI18n();
const store = useThemeStore();
const { mode } = storeToRefs(store);

const options = computed<{ value: ThemeMode; label: string; icon: typeof Sunny }[]>(() => [
  { value: 'light', label: t('theme.light'), icon: Sunny },
  { value: 'dark', label: t('theme.dark'), icon: Moon },
  { value: 'auto', label: t('theme.auto'), icon: MagicStick },
]);
</script>

<template>
  <el-dropdown trigger="click" @command="(c: ThemeMode) => store.setMode(c)">
    <el-button circle :title="options.find((o) => o.value === mode)?.label">
      <el-icon>
        <Sunny v-if="mode === 'light'" />
        <Moon v-else-if="mode === 'dark'" />
        <MagicStick v-else />
      </el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="o in options"
          :key="o.value"
          :command="o.value"
          :class="{ active: o.value === mode }"
        >
          <el-icon class="mr"><component :is="o.icon" /></el-icon>
          {{ o.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped>
.mr {
  margin-right: 6px;
}
:global(.el-dropdown-menu__item.active) {
  color: var(--el-color-primary);
  font-weight: 600;
}
</style>
