<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useDatasetStore } from '@/stores/dataset'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const store = useDatasetStore()
const { datasets, activeDatasetId, loading } = storeToRefs(store)

function onChange(value: string) {
  store.setActive(value)
  const routeDatasetId = route.params.datasetId
  if (routeDatasetId && routeDatasetId !== value) {
    const next = { ...route, params: { ...route.params, datasetId: value } }
    router.replace(next)
  }
}
</script>

<template>
  <el-select
    :model-value="activeDatasetId ?? ''"
    :loading="loading"
    :placeholder="t('dataset.selectDataset')"
    style="width: 280px"
    @update:model-value="onChange"
  >
    <el-option
      v-for="d in datasets"
      :key="d.datasetId"
      :label="`${d.displayName} (${d.variant}/${d.language})`"
      :value="d.datasetId"
    />
  </el-select>
</template>
