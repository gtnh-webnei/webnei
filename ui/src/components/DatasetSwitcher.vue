<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRouter, useRoute } from 'vue-router'
import { useDatasetStore } from '@/stores/dataset'

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
    placeholder="选择数据集"
    style="width: 280px"
    @update:model-value="onChange"
  >
    <el-option
      v-for="d in datasets"
      :key="d.datasetId"
      :label="`${d.displayName} (${d.variant})`"
      :value="d.datasetId"
    />
  </el-select>
</template>
