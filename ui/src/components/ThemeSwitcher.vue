<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { Sunny, Moon, MagicStick } from '@element-plus/icons-vue'
import { useThemeStore, type ThemeMode } from '@/stores/theme'

const store = useThemeStore()
const { mode } = storeToRefs(store)

const options: { value: ThemeMode; label: string; icon: typeof Sunny }[] = [
  { value: 'light', label: '浅色', icon: Sunny },
  { value: 'dark', label: '深色', icon: Moon },
  { value: 'auto', label: '跟随系统', icon: MagicStick },
]
</script>

<template>
  <el-dropdown trigger="click" @command="(c: ThemeMode) => store.setMode(c)">
    <el-button circle :title="`主题: ${options.find((o) => o.value === mode)?.label}`">
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
