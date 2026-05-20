<script setup lang="ts">
const q = defineModel<string>('q', { default: '' })
const secondary = defineModel<string>('secondary', { default: '' })

withDefaults(
  defineProps<{
    placeholder?: string
    secondaryPlaceholder?: string
    secondaryOptions?: string[]
    showSecondary?: boolean
    total?: number
    totalLabel?: string
    totalSuffix?: string
  }>(),
  {
    placeholder: '搜索',
    secondaryPlaceholder: '全部 Mod',
    secondaryOptions: () => [],
    showSecondary: true,
    totalLabel: '共',
    totalSuffix: '项',
  },
)
</script>

<template>
  <div class="browser-toolbar">
    <el-input
      v-model="q"
      :placeholder="placeholder"
      clearable
      class="search-input"
    />
    <el-select
      v-if="showSecondary"
      v-model="secondary"
      :placeholder="secondaryPlaceholder"
      clearable
      filterable
      class="secondary-select"
    >
      <el-option
        v-for="o in secondaryOptions"
        :key="o"
        :label="o"
        :value="o"
      />
    </el-select>
    <slot name="extra" />
    <div class="spacer" />
    <div v-if="typeof total === 'number'" class="total">
      {{ totalLabel }} <strong>{{ total.toLocaleString() }}</strong> {{ totalSuffix }}
    </div>
  </div>
</template>

<style scoped>
.browser-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.search-input {
  width: 280px;
  max-width: 100%;
  flex-shrink: 0;
}
.secondary-select {
  width: 200px;
  flex-shrink: 0;
}
.spacer {
  flex: 1;
}
.total {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}
.total strong {
  color: var(--el-text-color-primary);
  font-weight: 600;
}
</style>
