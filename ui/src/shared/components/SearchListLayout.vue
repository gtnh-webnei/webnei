<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import McInput from '@shared/ui/McInput.vue'
import McPagination from '@shared/ui/McPagination.vue'
import CatalogPanel from './CatalogPanel.vue'
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

const { t } = useI18n()

const totalLabel = computed(() => t('catalog.resultCount', { count: props.total }))
const showPagination = computed(
  () =>
    props.page !== undefined &&
    props.size !== undefined &&
    props.onPageChange !== undefined &&
    props.total > props.size,
)
</script>

<template>
  <CatalogPanel
    class="search-page"
    :loading="loading && hasItems"
  >
    <template #header>
      <div class="search-toolbar">
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
      </div>
    </template>

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

    <template
      v-if="showPagination"
      #footer
    >
      <McPagination
        :page="page ?? 0"
        :size="size ?? 0"
        :total="total"
        @change="onPageChange"
      />
    </template>
  </CatalogPanel>
</template>

<style scoped>
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

.search-hint {
  color: var(--mc-panel-text);
  font-size: 13px;
  font-weight: 700;
}

.search-hint.is-error {
  color: var(--mc-redstone);
}

@media (max-width: 720px) {
  .search-toolbar {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    align-items: stretch;
  }

  .search-group {
    width: 100%;
  }
}
</style>
