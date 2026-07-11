<script setup lang="ts">
import { computed } from 'vue'
import { useMediaQuery } from '@vueuse/core'
import { useI18n } from 'vue-i18n'
import McPanel from '@shared/ui/McPanel.vue'
import McInput from '@shared/ui/McInput.vue'
import SearchHelp, { type SearchHelpItem } from './SearchHelp.vue'

const props = withDefaults(
  defineProps<{
    title: string
    total: number
    loading: boolean
    error: string | null
    hasItems: boolean
    hasDataset: boolean
    // help 可选：不传则不显示帮助问号。
    helpItems?: SearchHelpItem[]
    // 分页可选：page/size/onPageChange 齐备且 total>size 时才渲染分页 footer；不分页的列表不传。
    page?: number
    size?: number
    onPageChange?: (currentPage: number) => void
  }>(),
  {
    helpItems: undefined,
    page: undefined,
    size: undefined,
    onPageChange: undefined,
  },
)

// query 由父组件通过 v-model:query 双向绑定到自身持有的 ref。
const queryModel = defineModel<string>('query', { required: true })

const COMPACT_PAGER_QUERY = '(max-width: 520px)'
const COMPACT_PAGER_COUNT = 5
const DEFAULT_PAGER_COUNT = 7

const { t } = useI18n()
const isCompactPager = useMediaQuery(COMPACT_PAGER_QUERY)

const totalLabel = computed(() => t('catalog.resultCount', { count: props.total }))
const pagerCount = computed(() => (isCompactPager.value ? COMPACT_PAGER_COUNT : DEFAULT_PAGER_COUNT))
const showPagination = computed(
  () =>
    props.page !== undefined &&
    props.size !== undefined &&
    props.onPageChange !== undefined &&
    props.total > props.size,
)
</script>

<template>
  <section class="search-page">
    <McPanel class="search-panel">
      <header class="search-toolbar">
        <div class="search-heading">
          <p class="search-kicker">
            {{ title }}
          </p>
          <p class="search-count">
            {{ totalLabel }}
          </p>
        </div>
        <div class="search-group">
          <McInput
            v-model="queryModel"
            class="search-input"
            clearable
            :placeholder="t('catalog.searchPlaceholder')"
          />
          <SearchHelp
            v-if="helpItems"
            :items="helpItems"
          />
        </div>
      </header>

      <div
        class="search-body mc-scroll-area"
        :class="{ 'is-loading': loading && hasItems }"
      >
        <p
          v-if="!hasDataset"
          class="search-hint"
        >
          {{ t('dataset.empty') }}
        </p>
        <p
          v-else-if="error"
          class="search-hint is-error"
        >
          {{ error }}
        </p>
        <p
          v-else-if="loading && !hasItems"
          class="search-hint"
        >
          {{ t('catalog.loading') }}
        </p>
        <p
          v-else-if="!hasItems"
          class="search-hint"
        >
          {{ t('catalog.empty') }}
        </p>
        <slot v-else />
      </div>

      <footer
        v-if="showPagination"
        class="search-pagination"
      >
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="(page ?? 0) + 1"
          :page-size="size"
          :total="total"
          :pager-count="pagerCount"
          @current-change="onPageChange"
        />
      </footer>
    </McPanel>
  </section>
</template>

<style scoped>
.search-page {
  height: calc(100dvh - var(--app-header-height) - var(--app-page-padding) - var(--app-page-padding));
  min-height: 0;
}

.search-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  gap: 14px;
  height: 100%;
  min-width: 0;
  min-height: 0;
  padding: 16px;
  overflow: hidden;
}

.search-toolbar {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  min-width: 0;
}

.search-heading {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.search-kicker,
.search-count,
.search-hint {
  margin: 0;
}

.search-kicker {
  color: var(--mc-panel-text);
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 0.04em;
}

.search-count {
  color: var(--app-surface-muted);
  font-size: 12px;
  font-weight: 700;
}

.search-group {
  display: flex;
  align-items: center;
  gap: 8px;
  width: min(460px, 100%);
  min-width: 0;
}

.search-input {
  flex: 1 1 auto;
  min-width: 0;
}

.search-body {
  min-width: 0;
  min-height: 0;
  overflow: auto;
  padding-right: 6px;
}

.search-body.is-loading {
  opacity: 0.7;
}

.search-hint {
  color: var(--mc-panel-text);
  font-size: 13px;
  font-weight: 700;
}

.search-hint.is-error {
  color: var(--mc-redstone);
}

.search-pagination {
  display: flex;
  min-width: 0;
  justify-content: center;
}

.search-pagination :deep(.el-pagination) {
  gap: 8px;
}

.search-pagination :deep(.el-pagination.is-background .btn-prev),
.search-pagination :deep(.el-pagination.is-background .btn-next),
.search-pagination :deep(.el-pagination.is-background .el-pager li) {
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

.search-pagination :deep(.el-pagination.is-background .btn-prev:hover),
.search-pagination :deep(.el-pagination.is-background .btn-next:hover),
.search-pagination :deep(.el-pagination.is-background .el-pager li:hover) {
  background: var(--mc-surface-raised);
  color: var(--mc-ink);
}

.search-pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  border-color: var(--mc-emerald-hi) var(--mc-emerald-low) var(--mc-emerald-lower) var(--mc-emerald-highest);
  background: var(--mc-emerald);
  color: var(--mc-emerald-text);
}

.search-pagination :deep(.el-pagination.is-background .btn-prev:disabled),
.search-pagination :deep(.el-pagination.is-background .btn-next:disabled) {
  border-color: var(--mc-panel-mid) var(--mc-panel-low) var(--mc-panel-low) var(--mc-panel-mid);
  background: var(--mc-panel-mid);
  color: rgb(32 32 32 / 0.35);
}

@media (max-width: 720px) {
  .search-panel {
    padding: 12px;
  }

  .search-toolbar {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    align-items: stretch;
  }

  .search-group {
    width: 100%;
  }

  .search-body {
    padding-right: 0;
  }
}
</style>
