<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import CatalogBrowser from '@/components/catalog/CatalogBrowser.vue'
import { listFluids } from '@/api/fluid'
import type { CatalogEntry, CatalogQueryParams, PageResponse } from '@/api/types'

const { t } = useI18n()

async function fetchFluids(params: CatalogQueryParams): Promise<PageResponse<CatalogEntry>> {
  return listFluids(params)
}
</script>

<template>
  <CatalogBrowser
    kind="fluid"
    :title="t('catalog.fluids')"
    :fetcher="fetchFluids"
  />
</template>
