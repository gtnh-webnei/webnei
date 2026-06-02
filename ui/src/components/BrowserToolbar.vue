<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const q = defineModel<string>('q', { default: '' });
const secondary = defineModel<string>('secondary', { default: '' });

const props = withDefaults(
  defineProps<{
    placeholder?: string;
    secondaryPlaceholder?: string;
    secondaryOptions?: string[];
    showSecondary?: boolean;
    total?: number;
    totalLabel?: string;
    totalSuffix?: string;
  }>(),
  {
    placeholder: '',
    secondaryPlaceholder: '',
    secondaryOptions: () => [],
    showSecondary: true,
    totalLabel: '',
    totalSuffix: '',
  },
);

const displayPlaceholder = computed(() => props.placeholder || '');
const displaySecondaryPlaceholder = computed(
  () => props.secondaryPlaceholder || t('common.allMod'),
);
const displayTotalLabel = computed(() => props.totalLabel || t('common.totalCount'));
const displayTotalSuffix = computed(() => props.totalSuffix || t('common.totalSuffix'));
</script>

<template>
  <div class="browser-toolbar">
    <el-input v-model="q" :placeholder="displayPlaceholder" clearable class="search-input" />
    <el-select
      v-if="showSecondary"
      v-model="secondary"
      :placeholder="displaySecondaryPlaceholder"
      clearable
      filterable
      class="secondary-select"
    >
      <el-option v-for="o in secondaryOptions" :key="o" :label="o" :value="o" />
    </el-select>
    <slot name="extra" />
    <div class="spacer" />
    <div v-if="typeof total === 'number'" class="total">
      {{ displayTotalLabel }} <strong>{{ total.toLocaleString() }}</strong> {{ displayTotalSuffix }}
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
