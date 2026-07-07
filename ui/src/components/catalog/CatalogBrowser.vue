<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { watchDebounced } from '@vueuse/core'
import { useI18n } from 'vue-i18n'
import CatalogGrid from './CatalogGrid.vue'
import McPanel from '@/components/ui/McPanel.vue'
import McButton from '@/components/ui/McButton.vue'
import McInput from '@/components/ui/McInput.vue'
import McTooltip from '@/components/ui/McTooltip.vue'
import { useDatasetStore } from '@/stores/dataset'
import type { CatalogEntry, CatalogKind, PageResponse } from '@/api/types'
import type { CatalogQueryParams } from '@/api/types'

const props = defineProps<{
  kind: CatalogKind
  title: string
  fetcher: (params: CatalogQueryParams) => Promise<PageResponse<CatalogEntry>>
}>()

const { t } = useI18n()
const datasetStore = useDatasetStore()
const { activeDatasetId } = storeToRefs(datasetStore)

const query = ref('')
const page = ref(0)
const size = 60
const total = ref(0)
const items = ref<CatalogEntry[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
let requestId = 0

const totalLabel = computed(() => t('catalog.resultCount', { count: total.value }))
const hasItems = computed(() => items.value.length > 0)
const searchHelpItems = computed(() => {
  const items = [
    { token: 'text', key: 'catalog.searchHelpDefault' },
    { token: '@text', key: 'catalog.searchHelpMod' },
  ]
  if (props.kind === 'item') {
    items.push(
      { token: '$text', key: 'catalog.searchHelpOre' },
      { token: '#text', key: 'catalog.searchHelpTooltip' },
    )
  }
  items.push(
    { token: '&text', key: 'catalog.searchHelpIdentifier' },
    { token: '|', key: 'catalog.searchHelpOr' },
    { token: '-text / !text', key: 'catalog.searchHelpNegate' },
    { token: '"text"', key: 'catalog.searchHelpQuote' },
  )
  return items
})

async function load() {
  const datasetId = activeDatasetId.value
  if (!datasetId) {
    items.value = []
    total.value = 0
    return
  }

  const currentRequest = ++requestId
  loading.value = true
  error.value = null
  try {
    const response = await props.fetcher({
      datasetId,
      q: query.value,
      page: page.value,
      size,
    })
    if (currentRequest !== requestId) return
    items.value = response.items
    total.value = response.total
  } catch (err) {
    if (currentRequest !== requestId) return
    error.value = err instanceof Error ? err.message : String(err)
    items.value = []
    total.value = 0
  } finally {
    if (currentRequest === requestId) {
      loading.value = false
    }
  }
}

function resetAndLoad() {
  if (page.value === 0) {
    void load()
  } else {
    page.value = 0
  }
}

function onPageChange(currentPage: number) {
  page.value = Math.max(currentPage - 1, 0)
}

watchDebounced([activeDatasetId, query], resetAndLoad, { debounce: 250, immediate: true })
watch(page, () => void load())
</script>

<template>
  <section class="catalog-page">
    <McPanel class="catalog-panel">
      <header class="catalog-toolbar">
        <div class="catalog-heading">
          <p class="catalog-kicker">
            {{ title }}
          </p>
          <p class="catalog-count">
            {{ totalLabel }}
          </p>
        </div>
        <div class="catalog-search-group">
          <McInput
            v-model="query"
            class="catalog-search"
            clearable
            :placeholder="t('catalog.searchPlaceholder')"
          />
          <McTooltip placement="bottom-end">
            <template #content>
              <div class="catalog-search-help">
                <p class="catalog-search-help-title">
                  {{ t('catalog.searchHelpTitle') }}
                </p>
                <ul class="catalog-search-help-list">
                  <li
                    v-for="item in searchHelpItems"
                    :key="item.token"
                  >
                    <span class="catalog-search-help-token">{{ item.token }}</span>
                    <span>{{ t(item.key) }}</span>
                  </li>
                </ul>
              </div>
            </template>
            <McButton
              class="catalog-search-help-button"
              :aria-label="t('catalog.searchHelpLabel')"
            >
              ?
            </McButton>
          </McTooltip>
        </div>
      </header>

      <p
        v-if="!activeDatasetId"
        class="catalog-hint"
      >
        {{ t('dataset.empty') }}
      </p>
      <p
        v-else-if="error"
        class="catalog-hint is-error"
      >
        {{ error }}
      </p>
      <div
        v-else
        class="catalog-body"
        :class="{ 'is-loading': loading && hasItems }"
      >
        <p
          v-if="loading && !hasItems"
          class="catalog-hint"
        >
          {{ t('catalog.loading') }}
        </p>
        <p
          v-else-if="!hasItems"
          class="catalog-hint"
        >
          {{ t('catalog.empty') }}
        </p>
        <CatalogGrid
          v-else
          :kind="kind"
          :items="items"
        />
      </div>

      <footer
        v-if="total > size"
        class="catalog-pagination"
      >
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="page + 1"
          :page-size="size"
          :total="total"
          @current-change="onPageChange"
        />
      </footer>
    </McPanel>
  </section>
</template>

<style scoped>
.catalog-page {
  height: calc(100dvh - var(--app-header-height) - var(--app-page-padding) - var(--app-page-padding));
  min-height: 0;
}

.catalog-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  gap: 14px;
  height: 100%;
  min-height: 0;
  padding: 16px;
  overflow: hidden;
}

.catalog-toolbar {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  min-width: 0;
}

.catalog-heading {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.catalog-kicker,
.catalog-count,
.catalog-hint {
  margin: 0;
}

.catalog-kicker {
  color: var(--mc-panel-text);
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 0.04em;
}

.catalog-count {
  color: var(--app-surface-muted);
  font-size: 12px;
  font-weight: 700;
}

.catalog-search-group {
  display: flex;
  align-items: center;
  gap: 8px;
  width: min(460px, 100%);
  min-width: 0;
}

.catalog-search {
  flex: 1 1 auto;
  min-width: 0;
}

.catalog-search-help-button {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  font-size: 15px;
  cursor: help;
}

.catalog-search-help-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 900;
}

.catalog-search-help-list {
  display: grid;
  gap: 6px;
  margin: 0;
  padding: 0;
  font-size: 12px;
  line-height: 1.4;
  list-style: none;
}

.catalog-search-help-list li {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
}

.catalog-search-help-token {
  font-family: inherit;
  font-weight: 900;
  white-space: nowrap;
}

.catalog-body {
  min-width: 0;
  min-height: 0;
  overflow: auto;
  padding-right: 6px;
  scrollbar-color: var(--mc-scrollbar-thumb) var(--mc-scrollbar-track);
  scrollbar-width: auto;
}

.catalog-body::-webkit-scrollbar {
  width: 18px;
  height: 18px;
}

.catalog-body::-webkit-scrollbar-track {
  border: 2px solid;
  border-color: var(--mc-panel-low) var(--mc-panel-hi) var(--mc-panel-hi) var(--mc-panel-low);
  background: var(--mc-scrollbar-track);
}

.catalog-body::-webkit-scrollbar-thumb {
  min-height: 42px;
  border: 2px solid;
  border-color: var(--mc-panel-hi) var(--mc-panel-low) var(--mc-panel-low) var(--mc-panel-hi);
  background: var(--mc-scrollbar-thumb);
}

.catalog-body::-webkit-scrollbar-thumb:hover {
  background: var(--mc-scrollbar-thumb-hover);
}

.catalog-body::-webkit-scrollbar-corner {
  background: var(--mc-scrollbar-track);
}

.catalog-body::-webkit-scrollbar-button:single-button {
  display: block;
  width: 18px;
  height: 18px;
  border: 2px solid;
  border-color: var(--mc-panel-hi) var(--mc-panel-low) var(--mc-panel-low) var(--mc-panel-hi);
  background: var(--mc-scrollbar-button);
}

.catalog-body::-webkit-scrollbar-button:single-button:hover {
  background: var(--mc-scrollbar-button-hover);
}

.catalog-body.is-loading {
  opacity: 0.7;
}

.catalog-hint {
  color: var(--mc-panel-text);
  font-size: 13px;
  font-weight: 700;
}

.catalog-hint.is-error {
  color: var(--mc-redstone);
}

.catalog-pagination {
  display: flex;
  justify-content: center;
}

.catalog-pagination :deep(.el-pagination) {
  gap: 8px;
}

.catalog-pagination :deep(.el-pagination.is-background .btn-prev),
.catalog-pagination :deep(.el-pagination.is-background .btn-next),
.catalog-pagination :deep(.el-pagination.is-background .el-pager li) {
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

.catalog-pagination :deep(.el-pagination.is-background .btn-prev:hover),
.catalog-pagination :deep(.el-pagination.is-background .btn-next:hover),
.catalog-pagination :deep(.el-pagination.is-background .el-pager li:hover) {
  background: var(--mc-surface-raised);
  color: var(--mc-ink);
}

.catalog-pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  border-color: var(--mc-emerald-hi) var(--mc-emerald-low) var(--mc-emerald-lower) var(--mc-emerald-highest);
  background: var(--mc-emerald);
  color: var(--mc-emerald-text);
}

.catalog-pagination :deep(.el-pagination.is-background .btn-prev:disabled),
.catalog-pagination :deep(.el-pagination.is-background .btn-next:disabled) {
  border-color: var(--mc-panel-mid) var(--mc-panel-low) var(--mc-panel-low) var(--mc-panel-mid);
  background: var(--mc-panel-mid);
  color: rgb(32 32 32 / 0.35);
}

@media (max-width: 720px) {
  .catalog-panel {
    padding: 12px;
  }

  .catalog-toolbar {
    display: grid;
    align-items: stretch;
  }

  .catalog-search-group {
    width: 100%;
  }
}
</style>
