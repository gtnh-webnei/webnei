<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useMediaQuery } from '@vueuse/core'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useDatasetStore } from '@features/dataset/store'
import EntryGrid from '@shared/components/EntryGrid.vue'
import SearchHelp, { type SearchHelpItem } from '@shared/components/SearchHelp.vue'
import { HttpError } from '@shared/api/client'
import { useRacedLoader } from '@shared/composables/useRacedLoader'
import { useSearchList } from '@shared/composables/useSearchList'
import McInput from '@shared/ui/McInput.vue'
import AspectComposition from '../components/AspectComposition.vue'
import AspectPanel from '../components/AspectPanel.vue'
import AspectSigil from '../components/AspectSigil.vue'
import AspectUsedInList from '../components/AspectUsedInList.vue'
import { getAspectDetail, listAspectItems } from '../api'
import type { AspectDetail } from '../types'
import '../styles/aspect-theme.css'

const COMPACT_PAGER_QUERY = '(max-width: 520px)'
const COMPACT_PAGER_COUNT = 5
const DEFAULT_PAGER_COUNT = 7

const route = useRoute()
const { t } = useI18n()
const { activeDatasetId } = storeToRefs(useDatasetStore())
const aspectId = computed(() => route.params.aspectId as string)
const detail = ref<AspectDetail | null>(null)
const notFound = ref(false)
const { loading, error, cancel, run } = useRacedLoader()
const isCompactPager = useMediaQuery(COMPACT_PAGER_QUERY)
const pagerCount = computed(() => (isCompactPager.value ? COMPACT_PAGER_COUNT : DEFAULT_PAGER_COUNT))

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
</script>

<template>
  <section class="aspect-research-shell">
    <div
      v-if="!activeDatasetId"
      class="aspect-page-state"
    >
      {{ t('dataset.empty') }}
    </div>
    <div
      v-else-if="loading"
      class="aspect-page-state"
    >
      {{ t('catalog.loading') }}
    </div>
    <div
      v-else-if="notFound"
      class="aspect-page-state is-error"
    >
      <strong>{{ t('aspect.notFound') }}</strong>
      <router-link to="/aspects">
        {{ t('aspect.returnToCatalog') }}
      </router-link>
    </div>
    <div
      v-else-if="detailError"
      class="aspect-page-state is-error"
    >
      <strong>{{ detailError }}</strong>
      <router-link to="/aspects">
        {{ t('aspect.returnToCatalog') }}
      </router-link>
    </div>

    <article
      v-else-if="detail"
      class="aspect-detail-page mc-scroll-area"
    >
      <router-link
        class="aspect-detail-back"
        to="/aspects"
      >
        <span aria-hidden="true">←</span>
        <span>{{ t('aspect.returnToCatalog') }}</span>
      </router-link>

      <AspectPanel>
        <div class="aspect-detail-hero">
          <AspectSigil
            :icon="detail.icon"
            :name="detail.displayName"
            :color="detail.color"
            :size="88"
          />
          <div class="aspect-detail-intro">
            <h2>{{ detail.displayName }}</h2>
            <p class="aspect-detail-summary">
              {{ detail.primal ? t('aspect.primal') : t('aspect.compound') }}
            </p>
            <p>{{ detail.description }}</p>
          </div>
        </div>
      </AspectPanel>

      <div class="aspect-detail-middle">
        <AspectPanel :title="t('aspect.composition')">
          <AspectComposition :aspect="detail" />
        </AspectPanel>

        <AspectPanel :title="t('aspect.usedBy')">
          <AspectUsedInList :items="detail.usedBy" />
        </AspectPanel>
      </div>

      <AspectPanel
        class="aspect-items-panel"
        :title="t('aspect.associatedItems')"
      >
        <div class="aspect-item-toolbar">
          <div class="aspect-item-search">
            <McInput
              v-model="itemState.query.value"
              clearable
              :placeholder="t('catalog.searchPlaceholder')"
            />
            <SearchHelp :items="itemHelpItems" />
          </div>
          <p class="aspect-item-total">
            {{ t('catalog.resultCount', { count: itemState.total.value }) }}
          </p>
        </div>

        <div
          class="aspect-item-results mc-scroll-area"
          :class="{ 'is-loading': itemState.loading.value && itemState.items.value.length }"
        >
          <p
            v-if="itemError"
            class="aspect-section-empty is-error"
          >
            {{ itemError }}
          </p>
          <p
            v-else-if="itemState.loading.value && !itemState.items.value.length"
            class="aspect-section-empty"
          >
            {{ t('catalog.loading') }}
          </p>
          <p
            v-else-if="!itemState.items.value.length"
            class="aspect-section-empty"
          >
            {{ t('aspect.itemsEmpty') }}
          </p>
          <EntryGrid
            v-else
            kind="item"
            :items="itemState.items.value"
          >
            <template #trailing="{ entry }">
              <span class="aspect-item-amount">
                <strong>{{ entry.amount }}</strong>
                <span>{{ t('aspect.amount') }}</span>
              </span>
            </template>
          </EntryGrid>
        </div>

        <footer
          v-if="showItemPagination"
          class="aspect-item-pagination"
        >
          <el-pagination
            background
            layout="prev, pager, next"
            :current-page="itemState.page.value + 1"
            :page-size="itemState.size"
            :total="itemState.total.value"
            :pager-count="pagerCount"
            @current-change="itemState.onPageChange"
          />
        </footer>
      </AspectPanel>
    </article>
  </section>
</template>
