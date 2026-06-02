<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDatasetStore } from '@/stores/dataset'
import { listItems, listMods } from '@/api/items'
import type { NeiPanelEntry } from '@/api/items.types'
import { usePagedBrowser } from '@/composables/usePagedBrowser'
import BrowserToolbar from '@/components/BrowserToolbar.vue'
import ItemCard from '@/components/ItemCard.vue'
import ItemIcon from '@/components/ItemIcon.vue'

type ViewMode = 'card' | 'icon'
const VIEW_STORAGE = 'webnei.itemBrowser.view'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const datasetStore = useDatasetStore()
const { activeDatasetId } = storeToRefs(datasetStore)

const datasetId = computed(() =>
  String(route.params.datasetId ?? activeDatasetId.value ?? ''),
)

const viewMode = ref<ViewMode>(
  (localStorage.getItem(VIEW_STORAGE) as ViewMode | null) ?? 'card',
)
watch(viewMode, (v) => localStorage.setItem(VIEW_STORAGE, v))

const browser = usePagedBrowser<NeiPanelEntry>({
  datasetId,
  storageKey: 'webnei.itemBrowser.size',
  defaultSize: 60,
  fetcher: (id, { q, secondary, page, size }) =>
    listItems(id, { q, modId: secondary, page, size }),
  optionsFetcher: listMods,
})
const { q, secondary, page, pageSize, items, total, loading, error, secondaryOptions } = browser

function openDetail(item: NeiPanelEntry) {
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: item.itemVariantId, kind: 'detail' },
  })
}

function goLookup(kind: 'recipe' | 'usage', item: NeiPanelEntry) {
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: item.itemVariantId, kind },
  })
}
</script>

<template>
  <div class="item-browser">
    <BrowserToolbar
      v-model:q="q"
      v-model:secondary="secondary"
      :secondary-options="secondaryOptions"
      :placeholder="t('item.searchPlaceholder')"
      :total="total"
    >
      <template #extra>
        <el-segmented
          v-model="viewMode"
          :options="[
            { label: t('item.viewCard'), value: 'card' },
            { label: t('item.viewCompact'), value: 'icon' },
          ]"
          size="small"
        />
      </template>
    </BrowserToolbar>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />

    <el-skeleton v-if="loading && items.length === 0" :rows="6" animated />

    <el-empty v-else-if="items.length === 0" :description="t('item.noMatch')" />

    <div
      v-else
      v-loading="loading"
      class="grid"
      :class="`grid-${viewMode}`"
    >
      <template v-if="viewMode === 'card'">
        <ItemCard
          v-for="item in items"
          :key="item.itemVariantId"
          :item="item"
          @select="openDetail"
          @lookup="goLookup"
        />
      </template>
      <template v-else>
        <ItemIcon
          v-for="item in items"
          :key="item.itemVariantId"
          :item="item"
          :size="40"
          @click="openDetail(item)"
          @lookup="(kind, it) => goLookup(kind, it)"
        />
      </template>
    </div>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[30, 60, 120, 240]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.item-browser {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 100%;
}
.grid {
  display: grid;
  gap: 10px;
  width: 100%;
  min-width: 0;
}
.grid-card {
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
}
.grid-icon {
  grid-template-columns: repeat(auto-fill, minmax(48px, 1fr));
  gap: 6px;
}
.pager {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
