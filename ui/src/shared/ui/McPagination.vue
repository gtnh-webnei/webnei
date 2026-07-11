<script setup lang="ts">
import { computed } from 'vue'
import { useMediaQuery } from '@vueuse/core'

const props = defineProps<{
  page: number
  size: number
  total: number
}>()

const emit = defineEmits<{
  'update:page': [page: number]
  change: [page: number]
}>()

const COMPACT_PAGER_QUERY = '(max-width: 520px)'
const COMPACT_PAGER_COUNT = 5
const DEFAULT_PAGER_COUNT = 7

const isCompactPager = useMediaQuery(COMPACT_PAGER_QUERY)
const pagerCount = computed(() => (isCompactPager.value ? COMPACT_PAGER_COUNT : DEFAULT_PAGER_COUNT))
const currentPage = computed(() => props.page + 1)

function onCurrentChange(nextPage: number) {
  const zeroBased = nextPage - 1
  emit('update:page', zeroBased)
  emit('change', zeroBased)
}
</script>

<template>
  <div class="mc-pagination">
    <el-pagination
      background
      layout="prev, pager, next"
      :current-page="currentPage"
      :page-size="size"
      :total="total"
      :pager-count="pagerCount"
      @current-change="onCurrentChange"
    />
  </div>
</template>

<style scoped>
.mc-pagination {
  display: flex;
  min-width: 0;
  justify-content: center;
}

.mc-pagination :deep(.el-pagination) {
  gap: 8px;
}

.mc-pagination :deep(.el-pagination.is-background .btn-prev),
.mc-pagination :deep(.el-pagination.is-background .btn-next),
.mc-pagination :deep(.el-pagination.is-background .el-pager li) {
  min-width: 34px;
  height: 34px;
  margin: 0;
  border: 2px solid;
  border-color: var(--mc-panel-hi) var(--mc-panel-low) var(--mc-panel-low) var(--mc-panel-hi);
  border-radius: var(--mc-panel-radius);
  background: var(--mc-panel);
  color: var(--mc-panel-text);
  font-weight: 800;
  line-height: 30px;
}

.mc-pagination :deep(.el-pagination.is-background .btn-prev:hover),
.mc-pagination :deep(.el-pagination.is-background .btn-next:hover),
.mc-pagination :deep(.el-pagination.is-background .el-pager li:hover) {
  background: var(--mc-surface-raised);
  color: var(--mc-ink);
}

.mc-pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  border-color: var(--mc-emerald-hi) var(--mc-emerald-low) var(--mc-emerald-lower) var(--mc-emerald-highest);
  background: var(--mc-emerald);
  color: var(--mc-emerald-text);
}

.mc-pagination :deep(.el-pagination.is-background .btn-prev:disabled),
.mc-pagination :deep(.el-pagination.is-background .btn-next:disabled) {
  border-color: var(--mc-panel-mid) var(--mc-panel-low) var(--mc-panel-low) var(--mc-panel-mid);
  background: var(--mc-panel-mid);
  color: rgb(32 32 32 / 0.35);
}

@media (max-width: 520px) {
  .mc-pagination :deep(.el-pagination) {
    gap: 3px;
  }
}
</style>
