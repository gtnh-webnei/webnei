<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { Check, Close } from '@element-plus/icons-vue'
import { useDatasetStore } from '@/stores/dataset'
import { listModsPage } from '@/api/datasets'
import type { ModSummary } from '@/api/types'
import { usePagedBrowser } from '@/composables/usePagedBrowser'
import BrowserToolbar from '@/components/BrowserToolbar.vue'

const { t } = useI18n()

const PAGE_SIZE_KEY = 'webnei.modBrowser.pageSize'

const route = useRoute()
const datasetStore = useDatasetStore()
const { activeDatasetId, datasets } = storeToRefs(datasetStore)

const datasetId = computed(() =>
  String(route.params.datasetId ?? activeDatasetId.value ?? ''),
)

const datasetName = computed(
  () => datasets.value.find((d) => d.datasetId === datasetId.value)?.displayName ?? '',
)

const sortField = ref<string>('modId')
const sortDesc = ref<boolean>(false)
const sortExtras = computed(() => ({ sort: sortField.value, desc: sortDesc.value }))

const browser = usePagedBrowser<ModSummary, { sort: string; desc: boolean }>({
  datasetId,
  fetcher: (id, params) =>
    listModsPage(id, {
      q: params.q,
      sort: params.sort,
      desc: params.desc,
      page: params.page,
      size: params.size,
    }),
  extras: sortExtras,
  storageKey: PAGE_SIZE_KEY,
  defaultSize: 50,
})
const { q, page, pageSize, items, total, loading, error } = browser

function onSortChange({ prop, order }: { prop: string | null; order: 'ascending' | 'descending' | null }) {
  if (!prop || !order) {
    sortField.value = 'modId'
    sortDesc.value = false
    return
  }
  sortField.value = prop
  sortDesc.value = order === 'descending'
}
</script>

<template>
  <div class="mod-browser">
    <header class="header">
      <h1>{{ t('mod.pageTitle') }}</h1>
      <p class="lead">
        <span v-if="datasetName">{{ datasetName }} · </span>
        {{ t('common.totalCount') }} {{ total }} {{ t('mod.totalLabel') }}
      </p>
    </header>

    <BrowserToolbar
      v-model:q="q"
      :placeholder="t('mod.searchPlaceholder')"
      :show-secondary="false"
      :total="items.length"
      :total-label="t('common.showing')"
      :total-suffix="`/ ${total}`"
    />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading && items.length === 0" :rows="6" animated />

    <el-table
      v-else
      v-loading="loading"
      :data="items"
      stripe
      :default-sort="{ prop: 'modId', order: 'ascending' }"
      class="mod-table"
      @sort-change="onSortChange"
    >
      <el-table-column prop="modId" label="Mod ID" min-width="170" sortable="custom">
        <template #default="{ row }">
          <code class="mod-id">{{ row.modId }}</code>
        </template>
      </el-table-column>
      <el-table-column prop="name" :label="t('mod.colName')" min-width="200" sortable="custom" show-overflow-tooltip />
      <el-table-column prop="version" :label="t('mod.colVersion')" min-width="140" sortable="custom">
        <template #default="{ row }">
          <el-tag size="small" type="info" effect="plain">{{ row.version }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sourceFileName" :label="t('mod.colFile')" min-width="280" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="file">{{ row.sourceFileName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sourceType" :label="t('mod.colSource')" min-width="100" sortable="custom">
        <template #default="{ row }">
          <el-tag size="small" :type="row.sourceType === 'file' ? '' : 'warning'" effect="plain">
            {{ row.sourceType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" :label="t('mod.colEnabled')" width="80" align="center" sortable="custom">
        <template #default="{ row }">
          <el-icon v-if="row.enabled" color="var(--el-color-success)"><Check /></el-icon>
          <el-icon v-else color="var(--el-color-danger)"><Close /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="sourceSha256" label="SHA256" min-width="120">
        <template #default="{ row }">
          <el-tooltip :content="row.sourceSha256" placement="top">
            <code class="sha">{{ row.sourceSha256.slice(0, 8) }}…</code>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[25, 50, 100, 200]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>
  </div>
</template>

<script lang="ts">
export default { name: 'ModBrowser' }
</script>

<style scoped>
.mod-browser {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.header h1 {
  margin: 0 0 4px 0;
  font-size: 22px;
}
.lead {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.mod-table {
  width: 100%;
}
.mod-id {
  font-size: 12px;
}
.file {
  font-size: 12px;
  color: var(--el-text-color-regular);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.sha {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.pager {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
