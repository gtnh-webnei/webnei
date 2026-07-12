<script setup lang="ts">
import { computed, ref, watch, type CSSProperties } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useDatasetStore } from '@features/dataset/store'
import CatalogPanel from '@shared/components/CatalogPanel.vue'
import EntryGrid from '@shared/components/EntryGrid.vue'
import SearchHelp, { type SearchHelpItem } from '@shared/components/SearchHelp.vue'
import { HttpError } from '@shared/api/client'
import { useRacedLoader } from '@shared/composables/useRacedLoader'
import { useSearchList } from '@shared/composables/useSearchList'
import McCard from '@shared/ui/McCard.vue'
import McInput from '@shared/ui/McInput.vue'
import McPagination from '@shared/ui/McPagination.vue'
import AspectComposition from '../components/AspectComposition.vue'
import AspectSigil from '../components/AspectSigil.vue'
import AspectUsedInList from '../components/AspectUsedInList.vue'
import { getAspectDetail, listAspectItems } from '../api'
import type { AspectDetail } from '../types'
import '../styles/aspect-theme.css'

const route = useRoute()
const { t } = useI18n()
const { activeDatasetId } = storeToRefs(useDatasetStore())
const aspectId = computed(() => route.params.aspectId as string)
const detail = ref<AspectDetail | null>(null)
const notFound = ref(false)
const { loading, error, cancel, run } = useRacedLoader()

watch(
  [activeDatasetId, aspectId],
  ([datasetId, nextAspectId]) => {
    cancel()
    detail.value = null
    notFound.value = false
    if (!datasetId) return

    void run(
      () => getAspectDetail(datasetId, nextAspectId),
      (response) => {
        detail.value = response
      },
      (loadError) => {
        notFound.value = loadError instanceof HttpError && loadError.status === 404
      },
    )
  },
  { immediate: true },
)

const itemDatasetId = computed(() => (detail.value ? activeDatasetId.value : null))
const itemState = useSearchList(
  itemDatasetId,
  ({ datasetId, q, page, size }) =>
    listAspectItems({
      datasetId,
      aspectId: aspectId.value,
      q,
      page,
      size,
    }),
)

const itemHelpItems: SearchHelpItem[] = [
  { token: 'text', key: 'catalog.searchHelpDefault' },
  { token: '@text', key: 'catalog.searchHelpMod' },
  { token: '$text', key: 'catalog.searchHelpOre' },
  { token: '#text', key: 'catalog.searchHelpTooltip' },
  { token: '&text', key: 'catalog.searchHelpIdentifier' },
  { token: '|', key: 'catalog.searchHelpOr' },
  { token: '-text / !text', key: 'catalog.searchHelpNegate' },
  { token: '"text"', key: 'catalog.searchHelpQuote' },
]

const showItemPagination = computed(() => itemState.total.value > itemState.size)
const detailError = computed(() => (error.value ? t('aspect.detailLoadError') : null))
const itemError = computed(() => (itemState.error.value ? t('aspect.itemsLoadError') : null))
const typeLabel = computed(() => {
  if (!detail.value) return ''
  return detail.value.primal ? t('aspect.primal') : t('aspect.compound')
})
const accentStyle = computed<CSSProperties | undefined>(() => {
  if (!detail.value) return undefined
  return {
    '--aspect-color': `#${(detail.value.color & 0xffffff).toString(16).padStart(6, '0')}`,
  }
})
const pageLoading = computed(
  () => loading.value || (itemState.loading.value && itemState.items.value.length > 0),
)
</script>

<template>
  <CatalogPanel
    class="aspect-detail-page"
    :loading="pageLoading"
    :style="accentStyle"
  >
    <template
      v-if="detail"
      #header
    >
      <div class="aspect-detail-header">
        <router-link
          class="aspect-detail-back"
          to="/aspects"
        >
          <el-icon class="aspect-detail-back-icon">
            <ArrowLeft />
          </el-icon>
          <span>{{ t('aspect.returnToCatalog') }}</span>
        </router-link>

        <div class="aspect-detail-identity">
          <div class="aspect-detail-seal">
            <AspectSigil
              :icon="detail.icon"
              :name="detail.displayName"
              :color="detail.color"
              :size="48"
            />
          </div>
          <div class="aspect-detail-copy">
            <h1 class="aspect-detail-title">
              {{ detail.displayName }}
            </h1>
            <p class="aspect-detail-meta">
              <span>{{ typeLabel }}</span>
              <span
                class="aspect-detail-meta-divider"
                aria-hidden="true"
              >·</span>
              <span class="aspect-detail-id">{{ detail.id }}</span>
            </p>
            <p class="aspect-detail-desc">
              {{ detail.description }}
            </p>
          </div>
        </div>
      </div>
    </template>

    <div
      v-if="!activeDatasetId"
      class="aspect-detail-state"
    >
      {{ t('dataset.empty') }}
    </div>
    <div
      v-else-if="loading"
      class="aspect-detail-state"
    >
      {{ t('catalog.loading') }}
    </div>
    <div
      v-else-if="notFound"
      class="aspect-detail-state is-error"
    >
      <strong>{{ t('aspect.notFound') }}</strong>
      <router-link
        class="aspect-detail-state-link"
        to="/aspects"
      >
        {{ t('aspect.returnToCatalog') }}
      </router-link>
    </div>
    <div
      v-else-if="detailError"
      class="aspect-detail-state is-error"
    >
      <strong>{{ detailError }}</strong>
      <router-link
        class="aspect-detail-state-link"
        to="/aspects"
      >
        {{ t('aspect.returnToCatalog') }}
      </router-link>
    </div>

    <div
      v-else-if="detail"
      class="aspect-detail-body"
    >
      <!-- 关系区：一个 NEI 凹陷井，内部固定双栏 -->
      <McCard
        class="aspect-relation-well"
        tone="inset"
        :clickable="false"
      >
        <section class="aspect-relation-pane">
          <header class="aspect-block-head">
            <h2>{{ t('aspect.composition') }}</h2>
          </header>
          <AspectComposition :aspect="detail" />
        </section>

        <section class="aspect-relation-pane">
          <header class="aspect-block-head">
            <h2>{{ t('aspect.usedBy') }}</h2>
            <span class="aspect-block-count">
              {{ t('catalog.resultCount', { count: detail.usedBy.length }) }}
            </span>
          </header>
          <AspectUsedInList :items="detail.usedBy" />
        </section>
      </McCard>

      <!-- 物品区：与物品列表同一套 raised 卡片语言 -->
      <section class="aspect-items-block">
        <header class="aspect-items-toolbar">
          <div class="aspect-block-head">
            <h2>{{ t('aspect.associatedItems') }}</h2>
            <span class="aspect-block-count">
              {{ t('catalog.resultCount', { count: itemState.total.value }) }}
            </span>
          </div>
          <div class="aspect-item-search">
            <McInput
              v-model="itemState.query.value"
              class="aspect-item-input"
              clearable
              :placeholder="t('catalog.searchPlaceholder')"
            />
            <SearchHelp :items="itemHelpItems" />
          </div>
        </header>

        <p
          v-if="itemError"
          class="aspect-empty is-error"
        >
          {{ itemError }}
        </p>
        <p
          v-else-if="itemState.loading.value && !itemState.items.value.length"
          class="aspect-empty"
        >
          {{ t('catalog.loading') }}
        </p>
        <p
          v-else-if="!itemState.items.value.length"
          class="aspect-empty"
        >
          {{ t('aspect.itemsEmpty') }}
        </p>
        <EntryGrid
          v-else
          kind="item"
          layout="list"
          tone="raised"
          :items="itemState.items.value"
        >
          <template #trailing="{ entry }">
            <span class="aspect-item-amount">
              <strong>{{ entry.amount }}</strong>
              <span>{{ t('aspect.amount') }}</span>
            </span>
          </template>
        </EntryGrid>
      </section>
    </div>

    <template
      v-if="detail && showItemPagination"
      #footer
    >
      <McPagination
        :page="itemState.page.value"
        :size="itemState.size"
        :total="itemState.total.value"
        @change="itemState.onPageChange"
      />
    </template>
  </CatalogPanel>
</template>
